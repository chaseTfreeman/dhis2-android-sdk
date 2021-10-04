/*
 *  Copyright (c) 2004-2021, University of Oslo
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
package org.hisp.dhis.android.core.event.internal

import dagger.Reusable
import io.reactivex.Observable
import javax.inject.Inject
import kotlin.math.min
import org.hisp.dhis.android.core.arch.api.executors.internal.RxAPICallExecutor
import org.hisp.dhis.android.core.arch.api.paging.internal.ApiPagingEngine
import org.hisp.dhis.android.core.arch.api.paging.internal.Paging
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.arch.call.executors.internal.D2CallExecutor
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.internal.ProgramDataDownloadParams
import org.hisp.dhis.android.core.systeminfo.internal.SystemInfoModuleDownloader

@Reusable
class EventDownloadCall @Inject internal constructor(
    private val systemInfoModuleDownloader: SystemInfoModuleDownloader,
    private val d2CallExecutor: D2CallExecutor,
    private val rxCallExecutor: RxAPICallExecutor,
    private val eventQueryBundleFactory: EventQueryBundleFactory,
    private val endpointCallFactory: EventEndpointCallFactory,
    private val persistenceCallFactory: EventPersistenceCallFactory,
    private val lastUpdatedManager: EventLastUpdatedManager
) {

    fun downloadSingleEvents(params: ProgramDataDownloadParams): Observable<D2Progress> {

        val progressManager = D2ProgressManager(2)
        return Observable.merge(
            systemInfoModuleDownloader.downloadWithProgressManager(progressManager),
            downloadEventsInternal(params, progressManager)
        )
    }

    private fun downloadEventsInternal(
        params: ProgramDataDownloadParams,
        progressManager: D2ProgressManager
    ): Observable<D2Progress> {

        return Observable.create { emitter ->
            val iterables = BundleIterables(
                0, true, 0, mutableMapOf(), mutableListOf(), mutableListOf(), mutableListOf()
            )
            val bundles: List<EventQueryBundle> = eventQueryBundleFactory.getQueries(params)

            for (bundle in bundles) {
                iterables.eventsCount = 0
                iterables.bundleOrgUnitPrograms = mutableMapOf()
                iterables.orgUnitsBundleToDownload = bundle.orgUnits().toMutableList()
                bundle.orgUnits()
                    .ifEmpty { listOf(null) }
                    .forEach { orgUnit ->
                        iterables.bundleOrgUnitPrograms[orgUnit] = when (orgUnit) {
                            null -> listOf(EventsByProgramCount(null, 0))
                            else ->
                                bundle.commonParams().programs
                                    .map { EventsByProgramCount(it, 0) }
                        }.toMutableList()
                    }

                do {
                    iterateBundle(bundle, params, iterables)
                } while (
                    params.limitByProgram() != true &&
                    iterables.eventsCount < bundle.commonParams().limit &&
                    iterables.orgUnitsBundleToDownload.isNotEmpty()
                )

                if (params.uids().isEmpty()) {
                    lastUpdatedManager.update(bundle)
                }
            }
            emitter.onNext(progressManager.increaseProgress(Event::class.java, true))
            emitter.onComplete()
        }
    }

    private fun iterateBundle(
        bundle: EventQueryBundle,
        params: ProgramDataDownloadParams,
        iterables: BundleIterables
    ) {
        for (orgUnitUid in iterables.bundleOrgUnitPrograms.keys) {
            iterables.bundlePrograms = iterables.bundleOrgUnitPrograms[orgUnitUid]!!
            iterables.emptyOrCorruptedPrograms = emptyList<String?>().toMutableList()
            iterables.bundleLimit = getBundleLimit(bundle, params, iterables)

            if (iterables.eventsCount >= bundle.commonParams().limit || iterables.bundleLimit <= 0) {
                iterables.orgUnitsBundleToDownload = (iterables.orgUnitsBundleToDownload - orgUnitUid).toMutableList()
                break
            }

            iterateBundleProgram(orgUnitUid, bundle, params, iterables)

            iterables.bundleOrgUnitPrograms[orgUnitUid] = iterables.bundleOrgUnitPrograms[orgUnitUid]!!.filter {
                !iterables.emptyOrCorruptedPrograms.contains(it.program)
            }.toMutableList()
        }
    }

    private fun iterateBundleProgram(
        orgUnitUid: String?,
        bundle: EventQueryBundle,
        params: ProgramDataDownloadParams,
        iterables: BundleIterables
    ) {
        for (bundleProgram in iterables.bundlePrograms) {
            if (iterables.eventsCount >= bundle.commonParams().limit) {
                break
            }
            val eventQueryBuilder = EventQuery.builder()
                .commonParams(
                    bundle.commonParams().copy(
                        program = bundleProgram.program,
                        limit = iterables.bundleLimit
                    )
                )
                .lastUpdatedStr(lastUpdatedManager.getLastUpdatedStr(bundle.commonParams()))
                .orgUnit(orgUnitUid)
                .uids(params.uids())

            val result = getEventsForOrgUnitProgramCombination(
                eventQueryBuilder,
                iterables.bundleLimit,
                bundleProgram.eventCount
            )

            iterables.eventsCount += result.eventCount
            bundleProgram.eventCount += result.eventCount
            iterables.successfulSync = iterables.successfulSync && result.successfulSync

            if (result.emptyProgram || !result.successfulSync) {
                iterables.emptyOrCorruptedPrograms = (iterables.emptyOrCorruptedPrograms + bundleProgram.program)
                    .toMutableList()
            }
        }
    }

    private fun getBundleLimit(
        bundle: EventQueryBundle,
        params: ProgramDataDownloadParams,
        iterables: BundleIterables
    ): Int {
        return when {
            params.uids().isNotEmpty() -> params.uids().size
            params.limitByProgram() != true && iterables.bundlePrograms.isNotEmpty() ->
                (bundle.commonParams().limit - iterables.eventsCount)
                    .div(iterables.bundleOrgUnitPrograms.keys.size * iterables.bundlePrograms.size)
            params.limitByProgram() != true && iterables.bundlePrograms.isEmpty() -> 0
            else -> bundle.commonParams().limit - iterables.eventsCount
        }
    }

    private fun getEventsForOrgUnitProgramCombination(
        eventQueryBuilder: EventQuery.Builder,
        combinationLimit: Int,
        downloadedEvents: Int
    ): EventsWithPagingResult {

        var result = EventsWithPagingResult(0, successfulSync = true, emptyProgram = false)

        try {
            result = getEventsWithPaging(eventQueryBuilder, combinationLimit, downloadedEvents)
        } catch (ignored: D2Error) {
            result.successfulSync = false
        }

        return result
    }

    @Throws(D2Error::class)
    private fun getEventsWithPaging(
        eventQueryBuilder: EventQuery.Builder,
        combinationLimit: Int,
        downloadedEvents: Int
    ): EventsWithPagingResult {

        var downloadedEventsForCombination = 0
        var emptyProgram = false
        val baseQuery = eventQueryBuilder.build()

        val pagingList = ApiPagingEngine.getPaginationList(baseQuery.pageSize(), combinationLimit, downloadedEvents)

        for (paging in pagingList) {
            eventQueryBuilder.pageSize(paging.pageSize())
            eventQueryBuilder.page(paging.page())

            val pageEvents = d2CallExecutor.executeD2Call(
                endpointCallFactory.getCall(eventQueryBuilder.build()), true
            )

            val eventsToPersist = getEventsToPersist(paging, pageEvents)

            rxCallExecutor.run {
                wrapCompletableTransactionally(
                    persistenceCallFactory.persistEvents(eventsToPersist, null), true
                )
                    .blockingGet()
            }

            downloadedEventsForCombination += eventsToPersist.size

            if (pageEvents.size < paging.pageSize()) {
                emptyProgram = true
                break
            }
        }

        return EventsWithPagingResult(downloadedEventsForCombination, true, emptyProgram)
    }

    private fun getEventsToPersist(paging: Paging, pageEvents: List<Event>): List<Event> {

        return if (fullPage(paging) && pageEvents.size > paging.previousItemsToSkipCount()) {
            val toIndex = min(
                pageEvents.size,
                paging.pageSize() - paging.posteriorItemsToSkipCount()
            )
            pageEvents.subList(paging.previousItemsToSkipCount(), toIndex)
        } else {
            pageEvents
        }
    }

    private fun fullPage(paging: Paging): Boolean {
        return paging.isLastPage || paging.previousItemsToSkipCount() > 0 || paging.posteriorItemsToSkipCount() > 0
    }

    private class EventsWithPagingResult(var eventCount: Int, var successfulSync: Boolean, var emptyProgram: Boolean)

    private class EventsByProgramCount(val program: String?, var eventCount: Int)

    private class BundleIterables(
        var eventsCount: Int,
        var successfulSync: Boolean,
        var bundleLimit: Int,
        var bundleOrgUnitPrograms: MutableMap<String?, MutableList<EventsByProgramCount>>,
        var orgUnitsBundleToDownload: MutableList<String?>,
        var bundlePrograms: MutableList<EventsByProgramCount>,
        var emptyOrCorruptedPrograms: MutableList<String?>
    )
}
