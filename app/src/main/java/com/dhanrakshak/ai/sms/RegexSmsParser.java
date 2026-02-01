package com.dhanrakshak.ai.sms;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based SMS parser for Indian bank transaction messages.
 * Supports: HDFC, ICICI, Standard Chartered, Axis Bank
 * 
 * Used as fallback when Gemini Nano is unavailable or for faster processing.
 */
public class RegexSmsParser {

    private static final String TAG = "RegexSmsParser";

    // Bank identification patterns
    private static final Map<String, Pattern> BANK_PATTERNS = new HashMap<>();

    // Amount patterns
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE);

    // Balance patterns
    private static final Pattern BALANCE_PATTERN = Pattern.compile(
            "(?:bal(?:ance)?|avl\\.? bal|available)[:\\s]*(?:Rs\\.?|INR|₹)?\\s*([\\d,]+\\.?\\d*)",
            Pattern.CASE_INSENSITIVE);

    // Account number patterns (last 4-6 digits)
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile(
            "(?:a/c|ac|acct|account)[\\s:]*(?:no\\.?)?[\\s:]*[xX*]*?(\\d{4,6})",
            Pattern.CASE_INSENSITIVE);

    // UPI reference pattern
    private static final Pattern UPI_REF_PATTERN = Pattern.compile(
            "(?:UPI(?:\\s*Ref)?|Ref(?:\\.? ?No\\.?)?)[:\\s]*([A-Z0-9]+)",
            Pattern.CASE_INSENSITIVE);

    static {
        // HDFC Bank patterns
        BANK_PATTERNS.put("HDFC", Pattern.compile(
                "(?:HDFC|HDFCBK)", Pattern.CASE_INSENSITIVE));

        // ICICI Bank patterns
        BANK_PATTERNS.put("ICICI", Pattern.compile(
                "(?:ICICI|ICICIB)", Pattern.CASE_INSENSITIVE));

        // Standard Chartered patterns
        BANK_PATTERNS.put("STANDARD_CHARTERED", Pattern.compile(
                "(?:SCB|STANDARD\\s*CHARTERED|SCBANK)", Pattern.CASE_INSENSITIVE));

        // Axis Bank patterns
        BANK_PATTERNS.put("AXIS", Pattern.compile(
                "(?:AXIS|AXISB)", Pattern.CASE_INSENSITIVE));

        // SBI patterns (bonus)
        BANK_PATTERNS.put("SBI", Pattern.compile(
                "(?:SBI|SBIINB|STATE\\s*BANK)", Pattern.CASE_INSENSITIVE));

        // Kotak patterns (bonus)
        BANK_PATTERNS.put("KOTAK", Pattern.compile(
                "(?:KOTAK|KOTAKB)", Pattern.CASE_INSENSITIVE));
    }

    /**
     * Parse SMS message and extract transaction details.
     * 
     * @param smsBody  The SMS message body
     * @param senderId The sender ID (e.g., "HDFCBK", "ICICIB")
     * @return ParsedSmsTransaction or null if not a valid transaction
     */
    public ParsedSmsTransaction parse(String smsBody, String senderId) {
        if (smsBody == null || smsBody.isEmpty()) {
            return null;
        }

        // Check if this is a transaction SMS (contains money-related keywords)
        if (!isTransactionSms(smsBody)) {
            return null;
        }

        ParsedSmsTransaction.Builder builder = ParsedSmsTransaction.builder()
                .rawSms(smsBody)
                .timestamp(System.currentTimeMillis())
                .parseMethod("REGEX");

        // Identify bank
        String bankName = identifyBank(smsBody, senderId);
        builder.bankName(bankName);

        // Extract transaction type (DEBIT or CREDIT)
        String type = extractTransactionType(smsBody);
        builder.type(type);

        // Extract amount
        Double amount = extractAmount(smsBody, type);
        if (amount != null) {
            builder.amount(amount);
        } else {
            return null; // Cannot parse without amount
        }

        // Extract balance
        Double balance = extractBalance(smsBody);
        if (balance != null) {
            builder.balance(balance);
        }

        // Extract account number (last 4 digits)
        String accountLast4 = extractAccountNumber(smsBody);
        builder.accountLast4(accountLast4);

        // Extract merchant/description
        String merchant = extractMerchant(smsBody, type);
        builder.merchant(merchant);

        // Extract reference ID
        String refId = extractReferenceId(smsBody);
        builder.referenceId(refId);

        // Check if spam/promotional
        boolean isSpam = isSpamMessage(smsBody);
        builder.isSpam(isSpam);

        builder.parseSuccess(true);

        return builder.build();
    }

    /**
     * Check if SMS is a transaction message.
     */
    private boolean isTransactionSms(String smsBody) {
        String lowerBody = smsBody.toLowerCase();
        return (lowerBody.contains("debited") ||
                lowerBody.contains("credited") ||
                lowerBody.contains("withdrawn") ||
                lowerBody.contains("deposited") ||
                lowerBody.contains("transferred") ||
                lowerBody.contains("payment") ||
                lowerBody.contains("purchase") ||
                lowerBody.contains("spent") ||
                lowerBody.contains("received")) &&
                (lowerBody.contains("rs") ||
                        lowerBody.contains("inr") ||
                        smsBody.contains("₹"));
    }

    /**
     * Identify the bank from SMS content or sender ID.
     */
    private String identifyBank(String smsBody, String senderId) {
        // First try sender ID
        if (senderId != null) {
            for (Map.Entry<String, Pattern> entry : BANK_PATTERNS.entrySet()) {
                if (entry.getValue().matcher(senderId).find()) {
                    return entry.getKey();
                }
            }
        }

        // Then try SMS body
        for (Map.Entry<String, Pattern> entry : BANK_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(smsBody).find()) {
                return entry.getKey();
            }
        }

        return "UNKNOWN";
    }

    /**
     * Extract transaction type (DEBIT or CREDIT).
     */
    private String extractTransactionType(String smsBody) {
        String lowerBody = smsBody.toLowerCase();

        // Debit indicators
        if (lowerBody.contains("debited") ||
                lowerBody.contains("withdrawn") ||
                lowerBody.contains("spent") ||
                lowerBody.contains("paid") ||
                lowerBody.contains("purchase") ||
                lowerBody.contains("sent to") ||
                lowerBody.contains("transferred to")) {
            return "DEBIT";
        }

        // Credit indicators
        if (lowerBody.contains("credited") ||
                lowerBody.contains("deposited") ||
                lowerBody.contains("received") ||
                lowerBody.contains("refund") ||
                lowerBody.contains("cashback") ||
                lowerBody.contains("transferred from")) {
            return "CREDIT";
        }

        return "UNKNOWN";
    }

    /**
     * Extract transaction amount.
     */
    private Double extractAmount(String smsBody, String type) {
        Matcher matcher = AMOUNT_PATTERN.matcher(smsBody);

        // For DEBIT, take first amount (transaction amount)
        // For CREDIT, also take first amount
        if (matcher.find()) {
            try {
                String amountStr = matcher.group(1).replace(",", "");
                return Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse amount", e);
            }
        }

        return null;
    }

    /**
     * Extract account balance after transaction.
     */
    private Double extractBalance(String smsBody) {
        Matcher matcher = BALANCE_PATTERN.matcher(smsBody);

        if (matcher.find()) {
            try {
                String balanceStr = matcher.group(1).replace(",", "");
                return Double.parseDouble(balanceStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse balance", e);
            }
        }

        return null;
    }

    /**
     * Extract account number (last 4-6 digits).
     */
    private String extractAccountNumber(String smsBody) {
        Matcher matcher = ACCOUNT_PATTERN.matcher(smsBody);

        if (matcher.find()) {
            String account = matcher.group(1);
            // Return last 4 digits
            if (account.length() > 4) {
                return account.substring(account.length() - 4);
            }
            return account;
        }

        return null;
    }

    /**
     * Extract merchant or transaction description.
     */
    private String extractMerchant(String smsBody, String type) {
        String lowerBody = smsBody.toLowerCase();

        // UPI transaction patterns
        Pattern upiPattern = Pattern.compile(
                "(?:to|from|at|@)\\s+([A-Za-z0-9@._\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher upiMatcher = upiPattern.matcher(smsBody);

        if (upiMatcher.find()) {
            String merchant = upiMatcher.group(1).trim();
            // Clean up UPI handles
            if (merchant.contains("@")) {
                merchant = merchant.split("@")[0];
            }
            return merchant;
        }

        // ATM withdrawal
        if (lowerBody.contains("atm")) {
            return "ATM Withdrawal";
        }

        // POS transaction
        if (lowerBody.contains("pos")) {
            Pattern posPattern = Pattern.compile(
                    "pos[\\s-]*([A-Za-z0-9\\s]+?)(?:on|dated|rs|inr|₹)",
                    Pattern.CASE_INSENSITIVE);
            Matcher posMatcher = posPattern.matcher(smsBody);
            if (posMatcher.find()) {
                return posMatcher.group(1).trim();
            }
            return "POS Transaction";
        }

        // Net banking / NEFT / IMPS
        if (lowerBody.contains("neft") || lowerBody.contains("imps") ||
                lowerBody.contains("rtgs")) {
            return "Bank Transfer";
        }

        return "Transaction";
    }

    /**
     * Extract UPI or transaction reference ID.
     */
    private String extractReferenceId(String smsBody) {
        Matcher matcher = UPI_REF_PATTERN.matcher(smsBody);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Check if message is spam/promotional.
     */
    private boolean isSpamMessage(String smsBody) {
        String lowerBody = smsBody.toLowerCase();
        return lowerBody.contains("offer") ||
                lowerBody.contains("win") ||
                lowerBody.contains("reward points") ||
                lowerBody.contains("cashback offer") ||
                lowerBody.contains("congratulations") ||
                lowerBody.contains("limited period") ||
                lowerBody.contains("apply now");
    }

    /**
     * Bank-specific parsing for HDFC
     */
    public ParsedSmsTransaction parseHdfcSms(String smsBody) {
        // HDFC specific format: "Rs. xxx debited from a/c **1234 on dd-mm-yy"
        return parse(smsBody, "HDFCBK");
    }

    /**
     * Bank-specific parsing for ICICI
     */
    public ParsedSmsTransaction parseIciciSms(String smsBody) {
        // ICICI specific format
        return parse(smsBody, "ICICIB");
    }

    /**
     * Bank-specific parsing for Standard Chartered
     */
    public ParsedSmsTransaction parseStandardCharteredSms(String smsBody) {
        // SCB specific format
        return parse(smsBody, "SCB");
    }

    /**
     * Bank-specific parsing for Axis Bank
     */
    public ParsedSmsTransaction parseAxisSms(String smsBody) {
        // Axis specific format
        return parse(smsBody, "AXISB");
    }
}
