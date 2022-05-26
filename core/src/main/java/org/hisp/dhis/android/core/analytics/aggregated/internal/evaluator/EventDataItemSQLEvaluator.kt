/*
 *  Copyright (c) 2004-2022, University of Oslo
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.analytics.aggregated.internal.evaluator

import org.hisp.dhis.android.core.analytics.AnalyticsException
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.analytics.aggregated.internal.AnalyticsServiceEvaluationItem
import org.hisp.dhis.android.core.analytics.aggregated.internal.evaluator.AnalyticsEvaluatorHelper.getItemsByDimension
import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo
import org.hisp.dhis.android.core.event.EventTableInfo
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueTableInfo
import javax.inject.Inject

internal class EventDataItemSQLEvaluator @Inject constructor(
    private val databaseAdapter: DatabaseAdapter
) : AnalyticsEvaluator {

    override fun evaluate(
        evaluationItem: AnalyticsServiceEvaluationItem,
        metadata: Map<String, MetadataItem>
    ): String? {
        val sqlQuery = getSql(evaluationItem, metadata)

        return databaseAdapter.rawQuery(sqlQuery)?.use { c ->
            c.moveToFirst()
            c.getString(0)
        }
    }

    override fun getSql(
        evaluationItem: AnalyticsServiceEvaluationItem,
        metadata: Map<String, MetadataItem>
    ): String {
        val items = getItemsByDimension(evaluationItem)

        val whereClause = WhereClauseBuilder().apply {
            items.entries.forEach { entry ->
                when (entry.key) {
                    is Dimension.Data -> appendDataWhereClause(entry.value, this)
                    is Dimension.Period -> appendPeriodWhereClause(entry.value, this, metadata)
                    is Dimension.OrganisationUnit -> appendOrgunitWhereClause(entry.value, this, metadata)
                    is Dimension.Category -> appendCategoryWhereClause(entry.value, this)
                }
            }
            appendKeyNumberValue("$eventAlias.${EventTableInfo.Columns.DELETED}", 0)
        }.build()

        val eventDataItem = getEventDataItem(evaluationItem)
        val aggregator = getAggregator(eventDataItem, metadata)

        return when (eventDataItem) {
            is DimensionItem.DataItem.EventDataItem.DataElement ->
                "SELECT $aggregator(${TrackedEntityDataValueTableInfo.Columns.VALUE}) " +
                        "FROM ${TrackedEntityDataValueTableInfo.TABLE_INFO.name()} $dataValueAlias " +
                        "INNER JOIN ${EventTableInfo.TABLE_INFO.name()} $eventAlias " +
                        "ON $dataValueAlias.${TrackedEntityDataValueTableInfo.Columns.EVENT} = " +
                        "$eventAlias.${EventTableInfo.Columns.UID} " +
                        "WHERE $whereClause"

            is DimensionItem.DataItem.EventDataItem.Attribute ->
                "SELECT $aggregator(${TrackedEntityAttributeValueTableInfo.Columns.VALUE}) " +
                        "FROM ${TrackedEntityAttributeValueTableInfo.TABLE_INFO.name()} $attAlias " +
                        "INNER JOIN ${EnrollmentTableInfo.TABLE_INFO.name()} $enrollmentAlias " +
                        "ON $attAlias.${TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_INSTANCE} = " +
                        "$enrollmentAlias.${EnrollmentTableInfo.Columns.TRACKED_ENTITY_INSTANCE} " +
                        "INNER JOIN ${EventTableInfo.TABLE_INFO.name()} $eventAlias " +
                        "ON $enrollmentAlias.${EnrollmentTableInfo.Columns.UID} = " +
                        "$eventAlias.${EventTableInfo.Columns.ENROLLMENT} " +
                        "WHERE $whereClause"
        }
    }

    private fun appendDataWhereClause(
        items: List<DimensionItem>,
        builder: WhereClauseBuilder
    ): WhereClauseBuilder {
        val innerClause = items.map { it as DimensionItem.DataItem }
            .foldRight(WhereClauseBuilder()) { item, innerBuilder ->
                when (item) {
                    is DimensionItem.DataItem.EventDataItem.DataElement -> {
                        val operandClause = WhereClauseBuilder()
                            .appendKeyStringValue(
                                "$dataValueAlias.${TrackedEntityDataValueTableInfo.Columns.DATA_ELEMENT}",
                                item.dataElement
                            )
                            .appendKeyStringValue(
                                "$eventAlias.${EventTableInfo.Columns.PROGRAM}",
                                item.program
                            )
                            .build()
                        innerBuilder.appendOrComplexQuery(operandClause)
                    }
                    is DimensionItem.DataItem.EventDataItem.Attribute -> {
                        val operandClause = WhereClauseBuilder()
                            .appendKeyStringValue(
                                "$attAlias.${TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_ATTRIBUTE}",
                                item.attribute
                            )
                            .appendKeyStringValue(
                                "$eventAlias.${EventTableInfo.Columns.PROGRAM}",
                                item.program
                            )
                            .build()
                        innerBuilder.appendOrComplexQuery(operandClause)
                    }
                    else ->
                        throw AnalyticsException.InvalidArguments(
                            "Invalid arguments: unexpected " +
                                    "dataItem ${item.javaClass.name} in EventDataItem Evaluator."
                        )
                }
            }.build()

        return builder.appendComplexQuery(innerClause)
    }

    private fun appendPeriodWhereClause(
        items: List<DimensionItem>,
        builder: WhereClauseBuilder,
        metadata: Map<String, MetadataItem>
    ): WhereClauseBuilder {
        val reportingPeriods = AnalyticsEvaluatorHelper.getReportingPeriods(items, metadata)

        return if (reportingPeriods.isEmpty()) {
            builder
        } else {
            val eventDateColumn = "$eventAlias.${EventTableInfo.Columns.EVENT_DATE}"

            val innerClause = reportingPeriods.joinToString(" OR ") {
                "(${
                    AnalyticsEvaluatorHelper.getPeriodWhereClause(
                        columnStart = eventDateColumn,
                        columnEnd = eventDateColumn,
                        period = it
                    )
                })"
            }

            builder.appendComplexQuery(innerClause)
        }
    }

    private fun appendOrgunitWhereClause(
        items: List<DimensionItem>,
        builder: WhereClauseBuilder,
        metadata: Map<String, MetadataItem>
    ): WhereClauseBuilder {
        return AnalyticsEvaluatorHelper.appendOrgunitWhereClause(
            columnName = "${eventAlias}.${EventTableInfo.Columns.ORGANISATION_UNIT}",
            items = items,
            builder = builder,
            metadata = metadata
        )
    }

    private fun appendCategoryWhereClause(
        items: List<DimensionItem>,
        builder: WhereClauseBuilder
    ): WhereClauseBuilder {
        return AnalyticsEvaluatorHelper.appendCategoryWhereClause(
            columnName = "${eventAlias}.${EventTableInfo.Columns.ATTRIBUTE_OPTION_COMBO}",
            items = items,
            builder = builder
        )
    }

    private fun getEventDataItem(
        evaluationItem: AnalyticsServiceEvaluationItem,
    ): DimensionItem.DataItem.EventDataItem {
        return AnalyticsEvaluatorHelper.getSingleItemByDimension(evaluationItem)
    }

    private fun getAggregator(
        item: DimensionItem.DataItem.EventDataItem,
        metadata: Map<String, MetadataItem>
    ): String {
        val metadataItem = metadata[item.id]

        val aggregationType = when (metadataItem) {
            is MetadataItem.EventDataElementItem ->
                metadataItem.item.aggregationType()
            is MetadataItem.EventAttributeItem ->
                // TODO ANDROSDK-1547
                null
            else ->
                throw AnalyticsException.InvalidArguments("Invalid arguments: invalid event data item ${item.id}.")
        }

        return AnalyticsEvaluatorHelper.getElementAggregator(aggregationType)
    }

    companion object {
        private const val eventAlias = "ev"
        private const val dataValueAlias = "tdv"
        private const val attAlias = "av"
        private const val enrollmentAlias = "en"
    }
}
