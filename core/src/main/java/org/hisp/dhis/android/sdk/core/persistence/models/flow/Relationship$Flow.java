/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.sdk.core.persistence.models.flow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.hisp.dhis.android.sdk.core.persistence.models.common.meta.DbDhis;
import org.hisp.dhis.android.sdk.models.relationship.Relationship;

import java.util.ArrayList;
import java.util.List;

@Table(databaseName = DbDhis.NAME, uniqueColumnGroups = {
        @UniqueGroup(groupNumber = Relationship$Flow.UNIQUE_RELATIONSHIP_GROUP, uniqueConflict = ConflictAction.FAIL)
})
public final class Relationship$Flow extends BaseModel {

    static final int UNIQUE_RELATIONSHIP_GROUP = 93;

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    @Unique(unique = true, uniqueGroups = {UNIQUE_RELATIONSHIP_GROUP})
    String relationship;

    @Column
    long trackedEntityInstanceAId;

    @Column
    @Unique(unique = true, uniqueGroups = {UNIQUE_RELATIONSHIP_GROUP})
    String trackedEntityInstanceA;

    @Column
    long trackedEntityInstanceBId;

    @Column
    @Unique(unique = true, uniqueGroups = {UNIQUE_RELATIONSHIP_GROUP})
    String trackedEntityInstanceB;

    @Column
    String displayName;

    @Column
    org.hisp.dhis.android.sdk.models.state.Action action;

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public long getTrackedEntityInstanceAId() {
        return trackedEntityInstanceAId;
    }

    public void setTrackedEntityInstanceAId(long trackedEntityInstanceAId) {
        this.trackedEntityInstanceAId = trackedEntityInstanceAId;
    }

    public long getTrackedEntityInstanceBId() {
        return trackedEntityInstanceBId;
    }

    public void setTrackedEntityInstanceBId(long trackedEntityInstanceBId) {
        this.trackedEntityInstanceBId = trackedEntityInstanceBId;
    }

    public String getTrackedEntityInstanceA() {
        return trackedEntityInstanceA;
    }

    public void setTrackedEntityInstanceA(String trackedEntityInstanceA) {
        this.trackedEntityInstanceA = trackedEntityInstanceA;
    }

    public String getTrackedEntityInstanceB() {
        return trackedEntityInstanceB;
    }

    public void setTrackedEntityInstanceB(String trackedEntityInstanceB) {
        this.trackedEntityInstanceB = trackedEntityInstanceB;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public org.hisp.dhis.android.sdk.models.state.Action getAction() {
        return action;
    }

    public void setAction(org.hisp.dhis.android.sdk.models.state.Action action) {
        this.action = action;
    }

    public Relationship$Flow() {
        // empty constructor
    }

    public static Relationship toModel(Relationship$Flow relationshipFlow) {
        if (relationshipFlow == null) {
            return null;
        }

        Relationship relationship = new Relationship();
        relationship.setRelationship(relationshipFlow.getRelationship());
        relationship.setTrackedEntityInstanceA(relationshipFlow.getTrackedEntityInstanceA());
        // relationship.setTrackedEntityInstanceAId(relationshipFlow.getTrackedEntityInstanceAId());
        relationship.setTrackedEntityInstanceB(relationshipFlow.getTrackedEntityInstanceB());
        // relationship.setTrackedEntityInstanceBId(relationshipFlow.getTrackedEntityInstanceBId());
        relationship.setDisplayName(relationshipFlow.getDisplayName());
        // relationship.setAction(relationshipFlow.getAction());
        return relationship;
    }

    public static Relationship$Flow fromModel(Relationship relationship) {
        if (relationship == null) {
            return null;
        }

        Relationship$Flow relationshipFlow = new Relationship$Flow();
        relationshipFlow.setRelationship(relationship.getRelationship());
        // relationshipFlow.setTrackedEntityInstanceA(relationship.getTrackedEntityInstanceA());
        // relationshipFlow.setTrackedEntityInstanceAId(relationship.getTrackedEntityInstanceAId());
        // relationshipFlow.setTrackedEntityInstanceB(relationship.getTrackedEntityInstanceB());
        // relationshipFlow.setTrackedEntityInstanceBId(relationship.getTrackedEntityInstanceBId());
        relationshipFlow.setDisplayName(relationship.getDisplayName());
        // relationshipFlow.setAction(relationship.getAction());
        return relationshipFlow;
    }

    public static List<Relationship> toModels(List<Relationship$Flow> relationshipFlows) {
        List<Relationship> relationships = new ArrayList<>();

        if (relationshipFlows != null && !relationshipFlows.isEmpty()) {
            for (Relationship$Flow relationshipFlow : relationshipFlows) {
                relationships.add(toModel(relationshipFlow));
            }
        }

        return relationships;
    }

    public static List<Relationship$Flow> fromModels(List<Relationship> relationships) {
        List<Relationship$Flow> relationshipFlows = new ArrayList<>();

        if (relationships != null && !relationships.isEmpty()) {
            for (Relationship relationship: relationships) {
                relationshipFlows.add(fromModel(relationship));
            }
        }

        return relationshipFlows;
    }
}
