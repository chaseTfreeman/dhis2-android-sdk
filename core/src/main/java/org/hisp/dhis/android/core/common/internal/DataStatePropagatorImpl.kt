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
package org.hisp.dhis.android.core.common.internal

import dagger.Reusable
import java.util.*
import javax.inject.Inject
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentStore
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventTableInfo
import org.hisp.dhis.android.core.event.internal.EventStore
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.relationship.RelationshipConstraintType
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.internal.RelationshipStore
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceStore

@Reusable
@Suppress("TooManyFunctions")
internal class DataStatePropagatorImpl @Inject internal constructor(
    private val trackedEntityInstanceStore: TrackedEntityInstanceStore,
    private val enrollmentStore: EnrollmentStore,
    private val eventStore: EventStore,
    private val relationshipStore: RelationshipStore
) : DataStatePropagator {

    override fun propagateTrackedEntityInstanceUpdate(tei: TrackedEntityInstance?) {
        tei?.let {
            refreshTrackedEntityInstanceAggregatedSyncState(it.uid())
            refreshTrackedEntityInstanceLastUpdated(it.uid())
        }
    }

    override fun propagateEnrollmentUpdate(enrollment: Enrollment?) {
        enrollment?.let {
            refreshEnrollmentAggregatedSyncState(it.uid())
            refreshEnrollmentLastUpdated(it.uid())

            val tei = trackedEntityInstanceStore.selectByUid(it.trackedEntityInstance()!!)
            propagateTrackedEntityInstanceUpdate(tei)
        }
    }

    override fun propagateEventUpdate(event: Event?) {
        event?.let {
            refreshEventAggregatedSyncState(it.uid())
            refreshEventLastUpdated(it.uid())

            it.enrollment()?.let { enrollmentUid ->
                val enrollment = enrollmentStore.selectByUid(enrollmentUid)
                propagateEnrollmentUpdate(enrollment)
            }
        }
    }

    override fun propagateTrackedEntityDataValueUpdate(dataValue: TrackedEntityDataValue?) {
        setEventSyncState(dataValue!!.event()!!, getStateForUpdate)
    }

    override fun propagateTrackedEntityAttributeUpdate(trackedEntityAttributeValue: TrackedEntityAttributeValue?) {
        setTeiSyncState(trackedEntityAttributeValue!!.trackedEntityInstance(), getStateForUpdate)
    }

    override fun propagateNoteCreation(note: Note?) {
        if (note!!.noteType() == Note.NoteType.ENROLLMENT_NOTE) {
            setEnrollmentSyncState(note.enrollment()!!, getStateForUpdate)
        } else if (note.noteType() == Note.NoteType.EVENT_NOTE) {
            setEventSyncState(note.event()!!, getStateForUpdate)
        }
    }

    override fun propagateRelationshipUpdate(item: RelationshipItem?) {
        if (item != null) {
            if (item.hasTrackedEntityInstance()) {
                setTeiSyncState(item.trackedEntityInstance()!!.trackedEntityInstance(), getStateForUpdate)
            } else if (item.hasEnrollment()) {
                setEnrollmentSyncState(item.enrollment()!!.enrollment(), getStateForUpdate)
            } else if (item.hasEvent()) {
                setEventSyncState(item.event()!!.event(), getStateForUpdate)
            }
        }
    }

    private fun setTeiSyncState(trackedEntityInstanceUid: String?, getState: (State?) -> State) {
        trackedEntityInstanceStore.selectByUid(trackedEntityInstanceUid!!)?.let { instance ->
            trackedEntityInstanceStore.setSyncState(trackedEntityInstanceUid, getState(instance.syncState()))
            propagateTrackedEntityInstanceUpdate(instance)
        }
    }

    private fun setEnrollmentSyncState(enrollmentUid: String, getState: (State?) -> State) {
        enrollmentStore.selectByUid(enrollmentUid)?.let { enrollment ->
            enrollmentStore.setSyncState(enrollmentUid, getState(enrollment.syncState()))
            propagateEnrollmentUpdate(enrollment)
        }
    }

    private fun setEventSyncState(eventUid: String, getState: (State?) -> State) {
        eventStore.selectByUid(eventUid)?.let { event ->
            eventStore.setSyncState(eventUid, getState(event.syncState()))
            propagateEventUpdate(event)
        }
    }

    private fun refreshEventLastUpdated(eventUid: String) {
        eventStore.selectByUid(eventUid)?.let { event ->
            val now = Date()
            val updatedEvent = event.toBuilder()
                .lastUpdated(getMaxDate(event.lastUpdated(), now))
                .lastUpdatedAtClient(getMaxDate(event.lastUpdatedAtClient(), now))
                .build()
            eventStore.update(updatedEvent)
        }
    }

    private fun refreshEnrollmentLastUpdated(enrollmentUid: String) {
        enrollmentStore.selectByUid(enrollmentUid)?.let { enrollment ->
            val now = Date()
            val updatedEnrollment = enrollment.toBuilder()
                .lastUpdated(getMaxDate(enrollment.lastUpdated(), now))
                .lastUpdatedAtClient(getMaxDate(enrollment.lastUpdatedAtClient(), now))
                .build()
            enrollmentStore.update(updatedEnrollment)
        }
    }

    private fun refreshTrackedEntityInstanceLastUpdated(trackedEntityInstanceUid: String) {
        trackedEntityInstanceStore.selectByUid(trackedEntityInstanceUid)?.let { instance ->
            val now = Date()
            val updatedInstance = instance.toBuilder()
                .lastUpdated(getMaxDate(instance.lastUpdated(), now))
                .lastUpdatedAtClient(getMaxDate(instance.lastUpdatedAtClient(), now))
                .build()
            trackedEntityInstanceStore.update(updatedInstance)
        }
    }

    override fun resetUploadingEnrollmentAndEventStates(trackedEntityInstanceUid: String?) {
        if (trackedEntityInstanceUid == null) {
            return
        }
        val whereClause = WhereClauseBuilder()
            .appendKeyStringValue(EnrollmentTableInfo.Columns.TRACKED_ENTITY_INSTANCE, trackedEntityInstanceUid)
            .build()
        val enrollments = enrollmentStore.selectWhere(whereClause)
        for (enrollment in enrollments) {
            if (State.UPLOADING == enrollment.syncState()) {
                enrollmentStore.setSyncState(enrollment.uid(), State.TO_UPDATE)
                resetUploadingEventStates(enrollment.uid())
            }
        }
    }

    override fun resetUploadingEventStates(enrollmentUid: String?) {
        if (enrollmentUid == null) {
            return
        }
        val whereClause = WhereClauseBuilder()
            .appendKeyStringValue(EventTableInfo.Columns.ENROLLMENT, enrollmentUid)
            .build()
        val events = eventStore.selectWhere(whereClause)
        for (event in events) {
            if (State.UPLOADING == event.syncState()) {
                eventStore.setSyncState(event.uid(), State.TO_UPDATE)
            }
        }
    }

    private fun getMaxDate(existing: Date?, today: Date?): Date? {
        return if (existing == null) {
            today
        } else if (today == null || existing.after(today)) {
            existing
        } else {
            today
        }
    }

    private val getStateForUpdate = { existingState: State? ->
        if (State.TO_POST == existingState || State.RELATIONSHIP == existingState) {
            existingState
        } else {
            State.TO_UPDATE
        }
    }

    override fun refreshTrackedEntityInstanceAggregatedSyncState(trackedEntityInstanceUid: String) {
        trackedEntityInstanceStore.selectByUid(trackedEntityInstanceUid)?.let { instance ->
            val whereClause = WhereClauseBuilder()
                .appendKeyStringValue(EnrollmentTableInfo.Columns.TRACKED_ENTITY_INSTANCE, trackedEntityInstanceUid)
                .build()
            val enrollmentStates = enrollmentStore.selectAggregatedSyncStateWhere(whereClause)

            val relationships = relationshipStore.getRelationshipsByItem(
                RelationshipHelper.teiItem(trackedEntityInstanceUid),
                RelationshipConstraintType.FROM
            )
            val relationshipStates = relationships.map { it.syncState()!! }

            val teiAggregatedSyncState =
                getAggregatedSyncState(enrollmentStates + relationshipStates + instance.syncState()!!)
            trackedEntityInstanceStore.setAggregatedSyncState(trackedEntityInstanceUid, teiAggregatedSyncState)
        }
    }

    override fun refreshEnrollmentAggregatedSyncState(enrollmentUid: String) {
        enrollmentStore.selectByUid(enrollmentUid)?.let { enrollment ->
            val whereClause = WhereClauseBuilder()
                .appendKeyStringValue(EventTableInfo.Columns.ENROLLMENT, enrollmentUid)
                .build()
            val eventStates = eventStore.selectAggregatedSyncStateWhere(whereClause)

            val relationships = relationshipStore.getRelationshipsByItem(
                RelationshipHelper.enrollmentItem(enrollmentUid),
                RelationshipConstraintType.FROM
            )
            val relationshipStates = relationships.map { it.syncState()!! }

            val enrollmentAggregatedSyncState =
                getAggregatedSyncState(eventStates + relationshipStates + enrollment.syncState()!!)
            enrollmentStore.setAggregatedSyncState(enrollmentUid, enrollmentAggregatedSyncState)
        }
    }

    override fun refreshEventAggregatedSyncState(eventUid: String) {
        eventStore.selectByUid(eventUid)?.let { event ->
            val relationships = relationshipStore.getRelationshipsByItem(
                RelationshipHelper.eventItem(eventUid),
                RelationshipConstraintType.FROM
            )
            val relationshipStates = relationships.map { it.syncState()!! }

            val eventAggregatedSyncState = getAggregatedSyncState(relationshipStates + event.syncState()!!)
            eventStore.setAggregatedSyncState(eventUid, eventAggregatedSyncState)
        }
    }

    override fun refreshAggregatedSyncStatesCausedBy(
        trackedEntityInstanceUids: List<String>,
        enrollmentUids: List<String>,
        eventUids: List<String>
    ) {
        val enrollmentsFromEvents = eventStore.selectByUids(eventUids).mapNotNull { it.enrollment() }

        val enrollments = enrollmentStore.selectByUids(enrollmentUids + enrollmentsFromEvents)
        enrollments.forEach {
            refreshEnrollmentAggregatedSyncState(it.uid())
        }

        val teiUids = trackedEntityInstanceUids + enrollments.mapNotNull { it.trackedEntityInstance() }
        teiUids.forEach {
            refreshTrackedEntityInstanceAggregatedSyncState(it)
        }
    }

    private fun getAggregatedSyncState(states: List<State>): State {
        return when {
            states.contains(State.RELATIONSHIP) -> State.RELATIONSHIP
            states.contains(State.ERROR) -> State.ERROR
            states.contains(State.WARNING) -> State.WARNING
            states.contains(State.UPLOADING) ||
                states.contains(State.SENT_VIA_SMS) ||
                states.contains(State.SYNCED_VIA_SMS) ||
                states.contains(State.TO_POST) ||
                states.contains(State.TO_UPDATE) -> State.TO_UPDATE
            else -> State.SYNCED
        }
    }
}
