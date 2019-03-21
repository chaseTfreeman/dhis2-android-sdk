/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.android.core.trackedentity;

import org.hisp.dhis.android.core.arch.repositories.children.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyNameableCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.BooleanFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.EnumFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.IntegerFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.StringFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Map;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public final class TrackedEntityAttributeCollectionRepository
        extends ReadOnlyNameableCollectionRepositoryImpl<TrackedEntityAttribute,
        TrackedEntityAttributeCollectionRepository> {

    @Inject
    TrackedEntityAttributeCollectionRepository(final IdentifiableObjectStore<TrackedEntityAttribute> store,
                                               final Map<String, ChildrenAppender<TrackedEntityAttribute>>
                                                       childrenAppenders,
                                               final RepositoryScope scope) {
        super(store, childrenAppenders, scope, new FilterConnectorFactory<>(scope,
                s -> new TrackedEntityAttributeCollectionRepository(store, childrenAppenders, s)));
    }

    public StringFilterConnector<TrackedEntityAttributeCollectionRepository> byPattern() {
        return cf.string(TrackedEntityAttributeFields.PATTERN);
    }

    public IntegerFilterConnector<TrackedEntityAttributeCollectionRepository> bySortOrderInListNoProgram() {
        return cf.integer(TrackedEntityAttributeFields.SORT_ORDER_IN_LIST_NO_PROGRAM);
    }

    public StringFilterConnector<TrackedEntityAttributeCollectionRepository> byOptionSetUid() {
        return cf.string(TrackedEntityAttributeFields.OPTION_SET);
    }

    public EnumFilterConnector<TrackedEntityAttributeCollectionRepository, ValueType> byValueType() {
        return cf.enumC(TrackedEntityAttributeFields.VALUE_TYPE);
    }

    public StringFilterConnector<TrackedEntityAttributeCollectionRepository> byExpression() {
        return cf.string(TrackedEntityAttributeFields.EXPRESSION);
    }

    public EnumFilterConnector<TrackedEntityAttributeCollectionRepository,
            TrackedEntityAttributeSearchScope> bySearchScope() {
        return cf.enumC(TrackedEntityAttributeFields.SEARCH_SCOPE);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byProgramScope() {
        return cf.bool(TrackedEntityAttributeFields.PROGRAM_SCOPE);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byDisplayInListNoProgram() {
        return cf.bool(TrackedEntityAttributeFields.DISPLAY_IN_LIST_NO_PROGRAM);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byGenerated() {
        return cf.bool(TrackedEntityAttributeFields.GENERATED);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byDisplayOnVisitSchedule() {
        return cf.bool(TrackedEntityAttributeFields.DISPLAY_ON_VISIT_SCHEDULE);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byOrgUnitScope() {
        return cf.bool(TrackedEntityAttributeFields.ORG_UNIT_SCOPE);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byUnique() {
        return cf.bool(TrackedEntityAttributeTableInfo.Columns.UNIQUE);
    }

    public BooleanFilterConnector<TrackedEntityAttributeCollectionRepository> byInherit() {
        return cf.bool(TrackedEntityAttributeFields.INHERIT);
    }

    public StringFilterConnector<TrackedEntityAttributeCollectionRepository> byFormName() {
        return cf.string(TrackedEntityAttributeFields.FORM_NAME);
    }
}
