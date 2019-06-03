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

package org.hisp.dhis.android.core.dataapproval;

import org.hisp.dhis.android.core.arch.api.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.handlers.internal.SyncHandler;
import org.hisp.dhis.android.core.arch.call.factories.internal.QueryCallFactoryImpl;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.CallFetcher;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.ListNoResourceCallFetcher;
import org.hisp.dhis.android.core.arch.call.processors.internal.CallProcessor;
import org.hisp.dhis.android.core.arch.call.processors.internal.TransactionalNoResourceSyncCallProcessor;
import org.hisp.dhis.android.core.arch.call.internal.GenericCallData;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.Reusable;

import static org.hisp.dhis.android.core.utils.Utils.commaSeparatedCollectionValues;

@Reusable
final class DataApprovalCallFactory extends QueryCallFactoryImpl<DataApproval,
        DataApprovalQuery> {

    private final SyncHandler<DataApproval> handler;
    private final DataApprovalService service;

    private final SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Inject
    DataApprovalCallFactory(GenericCallData genericCallData,
                                           APICallExecutor apiCallExecutor,
                                           SyncHandler<DataApproval> handler,
                                           DataApprovalService service) {
        super(genericCallData, apiCallExecutor);
        this.handler = handler;
        this.service = service;
    }

    @Override
    protected CallFetcher<DataApproval> fetcher(
            final DataApprovalQuery dataApprovalQuery) {

        return new ListNoResourceCallFetcher<DataApproval>(apiCallExecutor) {
            @Override
            protected retrofit2.Call<List<DataApproval>> getCall() {

                return service.getDataApprovals(
                        DataApprovalFields.allFields,
                        commaSeparatedCollectionValues(dataApprovalQuery.workflowsUids()),
                        simpleDateFormat.format(dataApprovalQuery.startDate()),
                        simpleDateFormat.format(dataApprovalQuery.endDate()),
                        commaSeparatedCollectionValues(dataApprovalQuery.organisationUnistUids()),
                        commaSeparatedCollectionValues(dataApprovalQuery.attributeOptionCombosUids())
                );
            }
        };
    }

    @Override
    protected CallProcessor<DataApproval> processor(DataApprovalQuery dataApprovalQuery) {
        return new TransactionalNoResourceSyncCallProcessor<>(data.databaseAdapter(), handler);
    }
}
