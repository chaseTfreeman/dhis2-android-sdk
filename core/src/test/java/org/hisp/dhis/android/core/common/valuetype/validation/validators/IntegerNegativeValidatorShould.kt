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

package org.hisp.dhis.android.core.common.valuetype.validation.validators

import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerNegativeFailure
import org.junit.Test

class IntegerNegativeValidatorShould : ValidatorShouldHelper<IntegerNegativeFailure>(IntegerNegativeValidator) {

    @Test
    fun `Should success when passing valid values`() {
        valueShouldSuccess("-15")
        valueShouldSuccess(Integer.MIN_VALUE.toString())
    }

    @Test
    fun `Should fail with a value is positive exception when value is positive`() {
        valueShouldFail("5", IntegerNegativeFailure.ValueIsPositive)
    }

    @Test
    fun `Should fail with a value is zero exception when value is zero`() {
        valueShouldFail("0", IntegerNegativeFailure.ValueIsZero)
    }

    @Test
    fun `Should fail with a integer overflow exception when value is too big`() {
        valueShouldFail("2147483648", IntegerNegativeFailure.IntegerOverflow)
        valueShouldFail("-2147483649", IntegerNegativeFailure.IntegerOverflow)
    }

    @Test
    fun `Should fail with a number format exception when value is malformed`() {
        valueShouldFail("5fe2", IntegerNegativeFailure.NumberFormatException)
    }
}
