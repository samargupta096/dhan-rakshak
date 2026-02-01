package com.dhanrakshak.ai.sms;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.SmsTransaction;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * WorkManager worker for processing bank SMS in background.
 * Uses Gemini Nano for AI parsing with regex fallback.
 */
@HiltWorker
public class SmsParseWorker extends Worker {

    private static final String TAG = "SmsParseWorker";

    public static final String KEY_SMS_BODY = "sms_body";
    public static final String KEY_SENDER_ID = "sender_id";
    public static final String KEY_TIMESTAMP = "timestamp";

    private final GeminiNanoClient geminiClient;
    private final SmsTransactionDao smsTransactionDao;
    private final BankAccountDao bankAccountDao;

    @AssistedInject
    public SmsParseWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            GeminiNanoClient geminiClient,
            SmsTransactionDao smsTransactionDao,
            BankAccountDao bankAccountDao) {
        super(context, params);
        this.geminiClient = geminiClient;
        this.smsTransactionDao = smsTransactionDao;
        this.bankAccountDao = bankAccountDao;
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();

        String smsBody = inputData.getString(KEY_SMS_BODY);
        String senderId = inputData.getString(KEY_SENDER_ID);
        long timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis());

        if (smsBody == null || smsBody.isEmpty()) {
            Log.w(TAG, "Empty SMS body, skipping");
            return Result.failure();
        }

        try {
            // Parse SMS using Gemini Nano (with regex fallback)
            ParsedSmsTransaction parsed = geminiClient.parseSms(smsBody, senderId);

            if (parsed == null || !parsed.isParseSuccess()) {
                Log.w(TAG, "Failed to parse SMS");
                return Result.failure();
            }

            // Skip spam messages
            if (parsed.isSpam()) {
                Log.d(TAG, "Spam message detected, skipping");
                return Result.success();
            }

            // Find or create bank account
            long bankAccountId = getOrCreateBankAccount(parsed);

            // Create SMS transaction entity
            SmsTransaction transaction = new SmsTransaction(
                    bankAccountId,
                    smsBody,
                    parsed.getAmount(),
                    parsed.getType(),
                    parsed.getMerchant(),
                    parsed.getBalance(),
                    timestamp);
            transaction.setSmsSenderId(senderId);
            transaction.setReferenceId(parsed.getReferenceId());

            // Save to database
            smsTransactionDao.insert(transaction).blockingAwait();

            // Update bank account balance if available
            if (parsed.getBalance() > 0) {
                bankAccountDao.updateBalance(bankAccountId, parsed.getBalance(), timestamp)
                        .blockingAwait();
            }

            Log.d(TAG, "SMS transaction saved: " + parsed.getType() + " ₹" + parsed.getAmount());
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS", e);
            return Result.retry();
        }
    }

    /**
     * Find existing bank account or create new one based on parsed data.
     */
    private long getOrCreateBankAccount(ParsedSmsTransaction parsed) {
        String bankName = parsed.getBankName();
        String accountLast4 = parsed.getAccountLast4();

        if (bankName == null || bankName.equals("UNKNOWN")) {
            bankName = "Unknown Bank";
        }

        if (accountLast4 == null) {
            accountLast4 = "0000";
        }

        try {
            // Try to find existing account
            BankAccount existing = bankAccountDao
                    .findByBankAndLast4(bankName, accountLast4)
                    .blockingGet();

            if (existing != null) {
                return existing.getId();
            }
        } catch (Exception e) {
            // Account doesn't exist, create new one
            Log.d(TAG, "Creating new bank account for " + bankName);
        }

        // Create new bank account
        BankAccount newAccount = new BankAccount(bankName, "SAVINGS", accountLast4);
        newAccount.setBalance(parsed.getBalance());

        bankAccountDao.insert(newAccount).blockingAwait();

        // Get the inserted account ID
        try {
            BankAccount inserted = bankAccountDao
                    .findByBankAndLast4(bankName, accountLast4)
                    .blockingGet();
            return inserted != null ? inserted.getId() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
