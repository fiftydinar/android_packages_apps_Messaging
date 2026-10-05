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

import java.util.LinkedHashSet;
import java.util.Set;

/** Parses a notification's serialized conversation ids and dispatches read actions once each. */
public final class NotificationActionDispatcher {
    public interface MarkAsReadHandler {
        void markAsRead(String conversationId);
    }

    public interface CopyVerificationCodeHandler {
        void copyCode(String code);
    }

    private NotificationActionDispatcher() {
    }

    public static void dispatchMarkAsRead(final String serializedIds,
            final MarkAsReadHandler handler) {
        if (serializedIds == null || serializedIds.isEmpty() || handler == null) {
            return;
        }
        final String[] ids = serializedIds.split("\\|", -1);
        final Set<String> uniqueIds = new LinkedHashSet<>();
        for (String id : ids) {
            if (!id.isEmpty()) {
                uniqueIds.add(id);
            }
        }
        for (String id : uniqueIds) {
            handler.markAsRead(id);
        }
    }

    public static boolean dispatchCopyVerificationCode(final String code,
            final CopyVerificationCodeHandler handler) {
        if (handler == null || !VerificationCodeDetector.isValidCode(code)) {
            return false;
        }
        handler.copyCode(code);
        return true;
    }
}
