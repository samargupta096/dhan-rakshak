package com.dhanrakshak.ai.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * BroadcastReceiver for intercepting incoming SMS messages.
 * Filters for bank transaction SMS and queues for AI parsing.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsBroadcastReceiver";
    public static final String WORK_NAME = "sms_parse_work";

    // Bank sender ID keywords
    private static final String[] BANK_SENDER_KEYWORDS = {
            "HDFC", "ICICI", "AXIS", "SCB", "SBI", "KOTAK", "IDFC",
            "INDUS", "YES", "BOB", "PNB", "CANARA", "UNION", "FEDERAL"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        try {
            // Get SMS messages from intent
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            if (messages == null || messages.length == 0) {
                return;
            }

            // Combine multi-part SMS
            StringBuilder fullMessage = new StringBuilder();
            String senderId = null;

            for (SmsMessage sms : messages) {
                if (sms != null) {
                    fullMessage.append(sms.getMessageBody());
                    if (senderId == null) {
                        senderId = sms.getOriginatingAddress();
                    }
                }
            }

            String messageBody = fullMessage.toString();

            // Check if this is a bank SMS
            if (!isBankSms(senderId, messageBody)) {
                Log.d(TAG, "Not a bank SMS, ignoring");
                return;
            }

            Log.d(TAG, "Bank SMS detected from: " + senderId);

            // Queue for background processing via WorkManager
            queueForProcessing(context, messageBody, senderId);

        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS", e);
        }
    }

    /**
     * Check if SMS is from a bank.
     */
    private boolean isBankSms(String senderId, String messageBody) {
        if (senderId == null) {
            return false;
        }

        String upperSenderId = senderId.toUpperCase();

        // Check sender ID for bank keywords
        for (String keyword : BANK_SENDER_KEYWORDS) {
            if (upperSenderId.contains(keyword)) {
                return true;
            }
        }

        // Additional check for transaction keywords in message
        String lowerBody = messageBody.toLowerCase();
        boolean hasTransactionKeyword = lowerBody.contains("debited") ||
                lowerBody.contains("credited") ||
                lowerBody.contains("withdrawn") ||
                lowerBody.contains("transferred");

        boolean hasAmountIndicator = lowerBody.contains("rs.") ||
                lowerBody.contains("rs ") ||
                lowerBody.contains("inr") ||
                messageBody.contains("₹");

        return hasTransactionKeyword && hasAmountIndicator;
    }

    /**
     * Queue SMS for background processing using WorkManager.
     */
    private void queueForProcessing(Context context, String messageBody, String senderId) {
        Data inputData = new Data.Builder()
                .putString(SmsParseWorker.KEY_SMS_BODY, messageBody)
                .putString(SmsParseWorker.KEY_SENDER_ID, senderId)
                .putLong(SmsParseWorker.KEY_TIMESTAMP, System.currentTimeMillis())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SmsParseWorker.class)
                .setInputData(inputData)
                .addTag("sms_parse")
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        WORK_NAME + "_" + System.currentTimeMillis(),
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        workRequest);

        Log.d(TAG, "SMS queued for processing");
    }
}
