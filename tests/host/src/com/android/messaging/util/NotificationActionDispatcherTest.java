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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class NotificationActionDispatcherTest {
    @Test
    public void dispatchesEachConversationOnceAndSkipsEmptyIds() {
        final List<String> markedAsRead = new ArrayList<>();
        NotificationActionDispatcher.dispatchMarkAsRead("one||two|one|", markedAsRead::add);
        assertEquals(Arrays.asList("one", "two"), markedAsRead);
    }

    @Test
    public void ignoresMissingIdsOrHandler() {
        final List<String> markedAsRead = new ArrayList<>();
        NotificationActionDispatcher.dispatchMarkAsRead(null, markedAsRead::add);
        NotificationActionDispatcher.dispatchMarkAsRead("", markedAsRead::add);
        NotificationActionDispatcher.dispatchMarkAsRead("one", null);
        assertEquals(0, markedAsRead.size());
    }

    @Test
    public void copiesOnlyValidVerificationCode() {
        final List<String> copiedCodes = new ArrayList<>();
        assertTrue(NotificationActionDispatcher.dispatchCopyVerificationCode("A1B2C3",
                copiedCodes::add));
        assertEquals(Arrays.asList("A1B2C3"), copiedCodes);
        assertFalse(NotificationActionDispatcher.dispatchCopyVerificationCode("not-a-code",
                copiedCodes::add));
        assertFalse(NotificationActionDispatcher.dispatchCopyVerificationCode("123456", null));
        assertEquals(1, copiedCodes.size());
    }
}
