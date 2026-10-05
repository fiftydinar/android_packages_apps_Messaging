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
package com.android.messaging.receiver;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.test.InstrumentationTestCase;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.ConversationIdSet;

import java.util.Locale;

public final class NotificationReceiverTest extends InstrumentationTestCase {
    public void testMarkAsReadIntentCarriesConversationSetAndIsUnique() {
        final Context context = getInstrumentation().getTargetContext();
        final ConversationIdSet ids = new ConversationIdSet();
        ids.add("conversation-a");
        ids.add("conversation-b");

        final Intent intent = NotificationReceiver.createMarkAsReadIntent(context, ids);
        assertNotNull(intent);
        assertEquals(NotificationReceiver.ACTION_MARK_AS_READ, intent.getAction());
        assertEquals(ids.getDelimitedString(), intent.getStringExtra(
                UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID_SET));
        assertNotNull(intent.getData());
        final PendingIntent pendingIntent =
                NotificationReceiver.getPendingIntentForMarkAsRead(context, ids);
        assertNotNull(pendingIntent);
        pendingIntent.cancel();

        final ConversationIdSet otherIds = new ConversationIdSet();
        otherIds.add("conversation-c");
        final Intent otherIntent = NotificationReceiver.createMarkAsReadIntent(context, otherIds);
        assertFalse(intent.getData().equals(otherIntent.getData()));
    }

    public void testCopyActionCarriesCodeWithoutPuttingItInPendingIntentIdentity() {
        final Context context = getInstrumentation().getTargetContext();
        final Intent intent = NotificationReceiver.createCopyVerificationCodeIntent(context,
                "conversation-a", "message-1", "482913");
        assertNotNull(intent);
        assertEquals(NotificationReceiver.ACTION_COPY_VERIFICATION_CODE, intent.getAction());
        assertEquals("482913", intent.getStringExtra(
                NotificationReceiver.EXTRA_VERIFICATION_CODE));
        assertFalse(intent.getData().toString().contains("482913"));
        final PendingIntent pendingIntent = NotificationReceiver
                .getPendingIntentForCopyVerificationCode(context, "conversation-a", "message-1",
                        "482913");
        assertNotNull(pendingIntent);
        pendingIntent.cancel();
    }

    public void testCopyActionRejectsMalformedCode() {
        final Context context = getInstrumentation().getTargetContext();
        assertNull(NotificationReceiver.createCopyVerificationCodeIntent(context,
                "conversation-a", "message-1", "not-a-code"));
    }

    public void testClipboardClipContainsOnlyCode() {
        final Context context = getInstrumentation().getTargetContext();
        final ClipData clip = NotificationReceiver.createVerificationCodeClip(context, "A1B2C3");
        assertNotNull(clip);
        assertEquals(context.getString(R.string.verification_code_clip_label),
                clip.getDescription().getLabel());
        assertEquals("A1B2C3", clip.getItemAt(0).getText().toString());
        assertNull(NotificationReceiver.createVerificationCodeClip(context, "not-a-code"));
    }

    public void testSerbianLatinLocaleUsesLatinStrings() {
        final Context context = getInstrumentation().getTargetContext();
        final Configuration configuration =
                new Configuration(context.getResources().getConfiguration());
        configuration.setLocales(new LocaleList(Locale.forLanguageTag("sr-Latn-RS")));
        final Context latinContext = context.createConfigurationContext(configuration);

        assertEquals("Razmena poruka", latinContext.getString(R.string.app_name));
        assertEquals("Označi kao pročitano",
                latinContext.getString(R.string.notification_mark_as_read));
        assertEquals("Kopiraj kod",
                latinContext.getString(R.string.notification_copy_verification_code));
    }
}
