/*
 * Copyright (C) 2015 The Android Open Source Project
 * Copyright (C) 2025-2026 The LineageOS Project
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
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.action.MarkAsSeenAction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.ConversationIdSet;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.NotificationActionDispatcher;
import com.android.messaging.util.VerificationCodeDetector;

// NotificationReceiver is used to handle delete intents from notifications. When a user
// clears all notifications or swipes a bugle notification away, the intent we pass in as
// the delete intent will get handled here.
public class NotificationReceiver extends BroadcastReceiver {
    // Logging
    public static final String TAG = LogUtil.BUGLE_TAG;
    public static final String ACTION_MARK_AS_READ = "com.android.messaging.mark_as_read";
    public static final String ACTION_COPY_VERIFICATION_CODE =
            "com.android.messaging.copy_verification_code";
    public static final String EXTRA_VERIFICATION_CODE = "verification_code";

    public static Intent createMarkAsReadIntent(final Context context,
            final ConversationIdSet conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return null;
        }
        final String serializedIds = conversationIds.getDelimitedString();
        final Uri data = new Uri.Builder()
                .scheme("bugle")
                .authority("notification-action")
                .appendPath("mark-as-read")
                .appendPath(serializedIds)
                .build();
        return new Intent(context, NotificationReceiver.class)
                .setAction(ACTION_MARK_AS_READ)
                .setData(data)
                .putExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID_SET, serializedIds);
    }

    public static PendingIntent getPendingIntentForMarkAsRead(final Context context,
            final ConversationIdSet conversationIds) {
        final Intent intent = createMarkAsReadIntent(context, conversationIds);
        if (intent == null) {
            return null;
        }
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static Intent createCopyVerificationCodeIntent(final Context context,
            final String conversationId, final String messageId, final String code) {
        if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messageId)
                || !VerificationCodeDetector.isValidCode(code)) {
            return null;
        }
        final Uri data = new Uri.Builder()
                .scheme("bugle")
                .authority("notification-action")
                .appendPath("copy-verification-code")
                .appendPath(conversationId)
                .appendPath(messageId)
                .build();
        return new Intent(context, NotificationReceiver.class)
                .setAction(ACTION_COPY_VERIFICATION_CODE)
                .setData(data)
                .putExtra(EXTRA_VERIFICATION_CODE, code);
    }

    public static PendingIntent getPendingIntentForCopyVerificationCode(final Context context,
            final String conversationId, final String messageId, final String code) {
        final Intent intent = createCopyVerificationCodeIntent(context, conversationId, messageId,
                code);
        if (intent == null) {
            return null;
        }
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static ClipData createVerificationCodeClip(final Context context, final String code) {
        if (!VerificationCodeDetector.isValidCode(code)) {
            return null;
        }
        return ClipData.newPlainText(context.getString(R.string.verification_code_clip_label),
                code);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        // Do not log intent extras: copy-code actions contain the one-time code.
        LogUtil.v(TAG, "NotificationReceiver.onReceive: action " + action);
        if (ACTION_MARK_AS_READ.equals(action)) {
            final String conversationIds = intent.getStringExtra(
                    UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID_SET);
            NotificationActionDispatcher.dispatchMarkAsRead(conversationIds,
                    BugleNotifications::markMessagesAsRead);
        } else if (ACTION_COPY_VERIFICATION_CODE.equals(action)) {
            copyVerificationCode(context, intent.getStringExtra(EXTRA_VERIFICATION_CODE));
        } else if (UIIntents.ACTION_RESET_NOTIFICATIONS.equals(action)) {
            final String conversationIdSetString =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID_SET);
            final int notificationTargets = intent.getIntExtra(
                    UIIntents.UI_INTENT_EXTRA_NOTIFICATIONS_UPDATE, BugleNotifications.UPDATE_ALL);
            if (conversationIdSetString == null) {
                BugleNotifications.markAllMessagesAsSeen();
            } else {
                for (final String conversationId :
                        ConversationIdSet.createSet(conversationIdSetString)) {
                    MarkAsSeenAction.markAsSeen(conversationId);
                    BugleNotifications.resetLastMessageDing(conversationId);
                }
            }
        }
    }

    private static void copyVerificationCode(final Context context, final String code) {
        final ClipboardManager clipboard = context.getSystemService(ClipboardManager.class);
        if (clipboard == null) {
            return;
        }
        final boolean copied = NotificationActionDispatcher.dispatchCopyVerificationCode(code,
                value -> clipboard.setPrimaryClip(createVerificationCodeClip(context, value)));
        if (copied) {
            Toast.makeText(context, R.string.verification_code_copied, Toast.LENGTH_SHORT).show();
        }
    }
}
