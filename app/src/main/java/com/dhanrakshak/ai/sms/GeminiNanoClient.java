package com.dhanrakshak.ai.sms;

import android.content.Context;
import android.util.Log;

import com.google.ai.edge.aicore.Content;
import com.google.ai.edge.aicore.GenerativeModel;
import com.google.ai.edge.aicore.GenerativeModelFutures;
import com.google.ai.edge.aicore.GenerateContentResponse;
import com.google.ai.edge.aicore.java.GenerativeModelFutures;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Gemini Nano Client for on-device AI inference.
 * Uses Google AI Edge SDK to run Gemini Nano on NPU/GPU.
 * 
 * Supported devices (2024):
 * - Google Pixel 8/8 Pro, Pixel 9 series
 * - Samsung Galaxy S24 series
 * - Realme GT6 (Snapdragon 8s Gen 3)
 * - Other devices with AI Core support
 * 
 * Privacy: All inference runs locally on device - no data sent to cloud.
 */
@Singleton
public class GeminiNanoClient {

    private static final String TAG = "GeminiNanoClient";

    private final Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private GenerativeModel generativeModel;
    private GenerativeModelFutures modelFutures;
    private boolean isAvailable = false;
    private boolean initializationAttempted = false;

    // Prompt template for SMS parsing
    private static final String SMS_PARSING_PROMPT = """
            You are a financial transaction parser for Indian bank SMS messages.
            Parse the following SMS and extract transaction details as JSON.

            SMS: %s

            Extract these fields (use null if not found):
            - transactionType: "CREDIT" or "DEBIT"
            - amount: number (in INR)
            - balance: number (remaining balance if mentioned)
            - merchant: string (payee/payer name)
            - accountLastFour: string (last 4 digits of account)
            - referenceNumber: string (UPI ref, transaction ID)
            - transactionMode: "UPI", "NEFT", "IMPS", "ATM", "POS", "CARD", or "OTHER"

            Respond ONLY with valid JSON, no explanation.
            Example: {"transactionType":"DEBIT","amount":500.00,"balance":12500.50,"merchant":"Swiggy","accountLastFour":"1234","referenceNumber":"123456789012","transactionMode":"UPI"}
            """;

    @Inject
    public GeminiNanoClient(Context context) {
        this.context = context.getApplicationContext();
        initializeModel();
    }

    /**
     * Initialize Gemini Nano model.
     * This checks if on-device AI is available and loads the model.
     */
    private void initializeModel() {
        if (initializationAttempted)
            return;
        initializationAttempted = true;

        try {
            // Check if AI Core is available on this device
            // AI Core requires Android 14+ and compatible hardware
            if (android.os.Build.VERSION.SDK_INT < 34) {
                Log.w(TAG, "AI Core requires Android 14+");
                isAvailable = false;
                return;
            }

            // Initialize GenerativeModel with Gemini Nano
            generativeModel = new GenerativeModel(
                    /* modelName */ "gemini-nano",
                    /* context */ context);

            modelFutures = GenerativeModelFutures.from(generativeModel);
            isAvailable = true;
            Log.i(TAG, "Gemini Nano initialized successfully - on-device AI ready!");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Gemini Nano: " + e.getMessage());
            Log.w(TAG, "Falling back to regex-based parsing");
            isAvailable = false;
        }
    }

    /**
     * Check if on-device AI is available.
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Parse SMS using Gemini Nano on-device AI.
     * All processing happens locally on the device's NPU.
     * 
     * @param smsBody The raw SMS text
     * @return Parsed transaction or null if parsing failed
     */
    public Single<ParsedSmsTransaction> parseSms(String smsBody) {
        if (!isAvailable || modelFutures == null) {
            return Single.error(new IllegalStateException("Gemini Nano not available"));
        }

        return Single.create(emitter -> {
            try {
                String prompt = String.format(SMS_PARSING_PROMPT, smsBody);

                Content content = new Content.Builder()
                        .addText(prompt)
                        .build();

                ListenableFuture<GenerateContentResponse> future = modelFutures.generateContent(content);

                Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse response) {
                        try {
                            String responseText = response.getText();
                            Log.d(TAG, "AI Response: " + responseText);

                            ParsedSmsTransaction transaction = parseJsonResponse(responseText, smsBody);
                            if (transaction != null) {
                                emitter.onSuccess(transaction);
                            } else {
                                emitter.onError(new Exception("Failed to parse AI response"));
                            }
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e(TAG, "AI inference failed", t);
                        emitter.onError(t);
                    }
                }, executor);

            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Parse JSON response from AI into ParsedSmsTransaction.
     */
    private ParsedSmsTransaction parseJsonResponse(String jsonStr, String originalSms) {
        try {
            // Clean up response - AI might add markdown formatting
            jsonStr = jsonStr.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            JSONObject json = new JSONObject(jsonStr);

            ParsedSmsTransaction tx = new ParsedSmsTransaction();
            tx.setOriginalMessage(originalSms);
            tx.setParsingMethod("GEMINI_NANO");
            tx.setConfidenceScore(0.95); // AI has high confidence

            if (json.has("transactionType") && !json.isNull("transactionType")) {
                tx.setTransactionType(json.getString("transactionType"));
            }

            if (json.has("amount") && !json.isNull("amount")) {
                tx.setAmount(json.getDouble("amount"));
            }

            if (json.has("balance") && !json.isNull("balance")) {
                tx.setBalance(json.getDouble("balance"));
            }

            if (json.has("merchant") && !json.isNull("merchant")) {
                tx.setMerchant(json.getString("merchant"));
            }

            if (json.has("accountLastFour") && !json.isNull("accountLastFour")) {
                tx.setAccountLastFour(json.getString("accountLastFour"));
            }

            if (json.has("referenceNumber") && !json.isNull("referenceNumber")) {
                tx.setReferenceNumber(json.getString("referenceNumber"));
            }

            if (json.has("transactionMode") && !json.isNull("transactionMode")) {
                tx.setTransactionMode(json.getString("transactionMode"));
            }

            // Validate required fields
            if (tx.getTransactionType() != null && tx.getAmount() > 0) {
                return tx;
            }

            return null;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate financial insights using on-device AI.
     * 
     * @param portfolioSummary Summary of user's portfolio
     * @return AI-generated insights
     */
    public Single<String> generateInsights(String portfolioSummary) {
        if (!isAvailable || modelFutures == null) {
            return Single.just("AI insights unavailable. Please add investments to see analysis.");
        }

        return Single.create(emitter -> {
            try {
                String prompt = String.format("""
                        You are a personal finance advisor for India.
                        Analyze this portfolio and provide 3-4 actionable insights:

                        %s

                        Consider:
                        - Asset allocation (equity/debt/gold ratio)
                        - Risk level for the investor
                        - Tax-saving opportunities (80C, ELSS, NPS)
                        - Emergency fund adequacy

                        Keep response concise, practical, and specific to Indian context.
                        """, portfolioSummary);

                Content content = new Content.Builder()
                        .addText(prompt)
                        .build();

                ListenableFuture<GenerateContentResponse> future = modelFutures.generateContent(content);

                Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse response) {
                        emitter.onSuccess(response.getText());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        emitter.onError(t);
                    }
                }, executor);

            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Categorize a transaction using AI.
     */
    public Single<String> categorizeTransaction(String merchant, String description) {
        if (!isAvailable || modelFutures == null) {
            return Single.just("Others");
        }

        return Single.create(emitter -> {
            try {
                String prompt = String.format("""
                        Categorize this transaction into ONE of these categories:
                        Food & Dining, Shopping, Transportation, Utilities, Health, Entertainment,
                        Investment, Insurance, EMI & Loans, Education, Rent, Transfer, Others

                        Merchant: %s
                        Description: %s

                        Respond with ONLY the category name, nothing else.
                        """, merchant, description);

                Content content = new Content.Builder()
                        .addText(prompt)
                        .build();

                ListenableFuture<GenerateContentResponse> future = modelFutures.generateContent(content);

                Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse response) {
                        emitter.onSuccess(response.getText().trim());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        emitter.onSuccess("Others"); // Fallback
                    }
                }, executor);

            } catch (Exception e) {
                emitter.onSuccess("Others");
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Get device AI capabilities info.
     */
    public String getAiCapabilitiesInfo() {
        StringBuilder info = new StringBuilder();
        info.append("On-Device AI Status:\n");
        info.append("• Model: Gemini Nano\n");
        info.append("• Available: ").append(isAvailable ? "Yes ✓" : "No").append("\n");
        info.append("• Android Version: ").append(android.os.Build.VERSION.SDK_INT).append("\n");
        info.append("• Device: ").append(android.os.Build.MODEL).append("\n");

        if (isAvailable) {
            info.append("• Acceleration: NPU/GPU\n");
            info.append("• Privacy: All inference runs locally\n");
        } else {
            info.append("• Fallback: Regex-based parsing\n");
            info.append("• Reason: Device may not support AI Core\n");
        }

        return info.toString();
    }
}
