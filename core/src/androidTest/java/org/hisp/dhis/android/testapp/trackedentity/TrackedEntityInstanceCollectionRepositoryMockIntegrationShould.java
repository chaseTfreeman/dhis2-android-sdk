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

package org.hisp.dhis.android.testapp.trackedentity;

import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestFullDispatcher;
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(D2JunitRunner.class)
public class TrackedEntityInstanceCollectionRepositoryMockIntegrationShould extends BaseMockIntegrationTestFullDispatcher {

    @Test
    public void allow_access_to_all_teis_without_children() {
        List<TrackedEntityInstance> trackedEntityInstances = d2.trackedEntityModule().trackedEntityInstances().blockingGet();
        assertThat(trackedEntityInstances.size(), is(2));

        TrackedEntityInstance trackedEntityInstance = trackedEntityInstances.get(0);
        assertThat(trackedEntityInstance.uid(), is("nWrB0TfWlvh"));
        assertThat(trackedEntityInstance.organisationUnit(), is("DiszpKrYNg8"));
        assertThat(trackedEntityInstance.trackedEntityAttributeValues() == null, is(true));

    }

    @Test
    public void allow_access_to_one_tei_without_children() {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances().uid("nWrB0TfWlvh").blockingGet();
        assertThat(tei.uid(), is("nWrB0TfWlvh"));
        assertThat(tei.organisationUnit(), is("DiszpKrYNg8"));
        assertThat(tei.trackedEntityAttributeValues() == null, is(true));
    }

    @Test
    public void include_tracked_entity_attribute_values_as_children() {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances()
                .withTrackedEntityAttributeValues().uid("nWrB0TfWlvD").blockingGet();

        List<TrackedEntityAttributeValue> tea = d2.trackedEntityModule().trackedEntityAttributeValues().blockingGet();

        assertThat(tei.trackedEntityAttributeValues().size(), is(2));


        assertThat(tei.trackedEntityAttributeValues().get(0).trackedEntityAttribute(), is("aejWyOfXge6"));
        assertThat(tei.trackedEntityAttributeValues().get(0).value(), is("123456"));
    }

    @Test
    public void add_tracked_entity_instances_to_the_repository() throws D2Error {
        List<TrackedEntityInstance> trackedEntityInstances1 = d2.trackedEntityModule().trackedEntityInstances().blockingGet();
        assertThat(trackedEntityInstances1.size(), is(2));

        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(
                TrackedEntityInstanceCreateProjection.create("DiszpKrYNg8", "nEenWmSyUEp"));

        List<TrackedEntityInstance> trackedEntityInstances2 = d2.trackedEntityModule().trackedEntityInstances().blockingGet();
        assertThat(trackedEntityInstances2.size(), is(3));

        TrackedEntityInstance trackedEntityInstance = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet();
        assertThat(trackedEntityInstance.uid(), is(teiUid));

        d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingDelete();
    }
}