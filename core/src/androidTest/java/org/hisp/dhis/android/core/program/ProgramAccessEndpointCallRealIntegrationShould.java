package org.hisp.dhis.android.core.program;

import android.support.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.calls.Call;
import org.hisp.dhis.android.core.common.D2Factory;
import org.hisp.dhis.android.core.common.GenericCallData;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.data.database.AbsStoreTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import retrofit2.Response;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class ProgramAccessEndpointCallRealIntegrationShould extends AbsStoreTestCase {
    private D2 d2;
    private Call<Response<Payload<Program>>> programAccessCall;

    @Before
    @Override
    public void setUp() throws IOException {
        super.setUp();
        d2 = D2Factory.create("https://play.dhis2.org/android-current/api/", databaseAdapter());
        programAccessCall = createCall();
    }

    private Call<Response<Payload<Program>>> createCall() {
        GenericCallData data = GenericCallData.create(databaseAdapter(), d2.retrofit());
        return ProgramAccessEndpointCall.FACTORY.create(data);
    }

    @Test
    public void download_programs() throws Exception {
        if (!d2.isUserLoggedIn().call()) {
            retrofit2.Response loginResponse = d2.logIn("android", "Android123").call();
            assertThat(loginResponse.isSuccessful()).isTrue();
        }

        retrofit2.Response programResponse = programAccessCall.call();
        assertThat(programResponse.isSuccessful()).isTrue();
    }
}
