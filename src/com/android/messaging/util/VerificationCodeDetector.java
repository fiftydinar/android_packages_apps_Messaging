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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds a single verification code only when the message labels it as one. */
public final class VerificationCodeDetector {
    private static final Pattern CODE_TOKEN = Pattern.compile("[A-Za-z0-9]{4,10}");
    private static final Pattern CODE_CANDIDATE = Pattern.compile(
            "(?<![A-Za-z0-9])([A-Za-z0-9]{4,10})(?![A-Za-z0-9])");
    private static final Pattern CODE_KEYWORD = Pattern.compile(
            "(?i)(?<![A-Za-z0-9])(?:verification(?:\\s+|-)?code|security(?:\\s+|-)?code|"
                    + "one[- ]time\\s+(?:password|passcode|code)|passcode|pass\\s+code|"
                    + "otp|pin|code)(?![A-Za-z0-9])");
    private static final int MAX_KEYWORD_DISTANCE = 56;
    private static final int MAX_NUMERIC_CODE_LENGTH = 8;

    private VerificationCodeDetector() {
    }

    /**
     * Returns the nearest valid code in a message with nearby verification-code wording.
     * Ambiguous equally-near candidates are rejected rather than copying an arbitrary number.
     */
    public static String findCode(final String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        String closestCode = null;
        int closestDistance = Integer.MAX_VALUE;
        boolean ambiguous = false;
        final Matcher candidateMatcher = CODE_CANDIDATE.matcher(message);
        while (candidateMatcher.find()) {
            final String candidate = candidateMatcher.group(1);
            if (!isValidCode(candidate)) {
                continue;
            }
            final int distance = distanceToNearestKeyword(message, candidateMatcher.start(1),
                    candidateMatcher.end(1));
            if (distance > MAX_KEYWORD_DISTANCE) {
                continue;
            }
            if (distance < closestDistance) {
                closestCode = candidate;
                closestDistance = distance;
                ambiguous = false;
            } else if (distance == closestDistance && !candidate.equals(closestCode)) {
                ambiguous = true;
            }
        }
        return closestCode == null || ambiguous ? null : closestCode;
    }

    /** Checks that a copied value is a code token, not arbitrary text. */
    public static boolean isValidCode(final String code) {
        if (code == null || !CODE_TOKEN.matcher(code).matches()) {
            return false;
        }
        boolean hasDigit = false;
        boolean allDigits = true;
        for (int i = 0; i < code.length(); i++) {
            final char character = code.charAt(i);
            hasDigit |= character >= '0' && character <= '9';
            allDigits &= character >= '0' && character <= '9';
        }
        return hasDigit && (!allDigits || code.length() <= MAX_NUMERIC_CODE_LENGTH);
    }

    private static int distanceToNearestKeyword(final String message, final int codeStart,
            final int codeEnd) {
        int distance = Integer.MAX_VALUE;
        final Matcher keywordMatcher = CODE_KEYWORD.matcher(message);
        while (keywordMatcher.find()) {
            final int keywordDistance;
            if (keywordMatcher.end() <= codeStart) {
                keywordDistance = codeStart - keywordMatcher.end();
            } else if (keywordMatcher.start() >= codeEnd) {
                keywordDistance = keywordMatcher.start() - codeEnd;
            } else {
                keywordDistance = 0;
            }
            distance = Math.min(distance, keywordDistance);
        }
        return distance;
    }
}
