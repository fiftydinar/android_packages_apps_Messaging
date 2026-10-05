/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class VerificationCodeDetectorTest {
    @Test
    public void findsNumericCodeWithKeywordBeforeOrAfter() {
        assertEquals("482913", VerificationCodeDetector.findCode(
                "Your verification code is 482913. Do not share it."));
        assertEquals("482913", VerificationCodeDetector.findCode(
                "482913 is your one-time password."));
    }

    @Test
    public void findsAlphanumericCode() {
        assertEquals("A1B2C3", VerificationCodeDetector.findCode(
                "Use code A1B2C3 to verify your account."));
    }

    @Test
    public void choosesCodeNearestToKeywordInsteadOfExpiryYear() {
        assertEquals("482913", VerificationCodeDetector.findCode(
                "Verification code: 482913. This code expires in 2028."));
    }

    @Test
    public void ignoresNumbersWithoutVerificationWording() {
        assertNull(VerificationCodeDetector.findCode(
                "Call 5551234567 about order 482913."));
    }

    @Test
    public void rejectsShortAndLongNumericTokens() {
        assertNull(VerificationCodeDetector.findCode("Your code is 123."));
        assertNull(VerificationCodeDetector.findCode("Your code is 1234567890."));
    }

    @Test
    public void rejectsAmbiguousEquallyNearCodes() {
        assertNull(VerificationCodeDetector.findCode("1234 code 5678"));
    }

    @Test
    public void acceptsDuplicateOccurrenceOfSameCode() {
        assertEquals("123456", VerificationCodeDetector.findCode(
                "Code 123456; repeat the code 123456 if asked."));
    }

    @Test
    public void rejectsNullEmptyAndCodeWithoutDigits() {
        assertNull(VerificationCodeDetector.findCode(null));
        assertNull(VerificationCodeDetector.findCode(""));
        assertNull(VerificationCodeDetector.findCode("Your verification code is ABCDEF."));
        assertFalse(VerificationCodeDetector.isValidCode("ABCD"));
        assertTrue(VerificationCodeDetector.isValidCode("A1B2"));
    }
}
