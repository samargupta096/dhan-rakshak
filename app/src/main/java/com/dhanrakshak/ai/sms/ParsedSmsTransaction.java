package com.dhanrakshak.ai.sms;

/**
 * Parsed SMS transaction result.
 * Extracted from bank SMS using Gemini Nano or Regex fallback.
 */
public class ParsedSmsTransaction {

    private double amount;
    private String type; // DEBIT or CREDIT
    private String merchant;
    private double balance;
    private boolean isSpam;
    private String bankName;
    private String accountLast4;
    private String referenceId;
    private long timestamp;
    private String rawSms;
    private boolean parseSuccess;
    private String parseMethod; // AI or REGEX

    // Default constructor
    public ParsedSmsTransaction() {
        this.isSpam = false;
        this.parseSuccess = false;
    }

    // Builder pattern for clean construction
    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setSpam(boolean spam) {
        isSpam = spam;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountLast4() {
        return accountLast4;
    }

    public void setAccountLast4(String accountLast4) {
        this.accountLast4 = accountLast4;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRawSms() {
        return rawSms;
    }

    public void setRawSms(String rawSms) {
        this.rawSms = rawSms;
    }

    public boolean isParseSuccess() {
        return parseSuccess;
    }

    public void setParseSuccess(boolean parseSuccess) {
        this.parseSuccess = parseSuccess;
    }

    public String getParseMethod() {
        return parseMethod;
    }

    public void setParseMethod(String parseMethod) {
        this.parseMethod = parseMethod;
    }

    public boolean isDebit() {
        return "DEBIT".equalsIgnoreCase(type);
    }

    public boolean isCredit() {
        return "CREDIT".equalsIgnoreCase(type);
    }

    /**
     * Builder class for ParsedSmsTransaction
     */
    public static class Builder {
        private final ParsedSmsTransaction transaction;

        public Builder() {
            transaction = new ParsedSmsTransaction();
        }

        public Builder amount(double amount) {
            transaction.setAmount(amount);
            return this;
        }

        public Builder type(String type) {
            transaction.setType(type);
            return this;
        }

        public Builder merchant(String merchant) {
            transaction.setMerchant(merchant);
            return this;
        }

        public Builder balance(double balance) {
            transaction.setBalance(balance);
            return this;
        }

        public Builder isSpam(boolean isSpam) {
            transaction.setSpam(isSpam);
            return this;
        }

        public Builder bankName(String bankName) {
            transaction.setBankName(bankName);
            return this;
        }

        public Builder accountLast4(String accountLast4) {
            transaction.setAccountLast4(accountLast4);
            return this;
        }

        public Builder referenceId(String referenceId) {
            transaction.setReferenceId(referenceId);
            return this;
        }

        public Builder timestamp(long timestamp) {
            transaction.setTimestamp(timestamp);
            return this;
        }

        public Builder rawSms(String rawSms) {
            transaction.setRawSms(rawSms);
            return this;
        }

        public Builder parseSuccess(boolean success) {
            transaction.setParseSuccess(success);
            return this;
        }

        public Builder parseMethod(String method) {
            transaction.setParseMethod(method);
            return this;
        }

        public ParsedSmsTransaction build() {
            return transaction;
        }
    }
}
