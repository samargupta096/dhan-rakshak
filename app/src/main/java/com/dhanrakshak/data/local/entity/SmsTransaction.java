package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * SMS Transaction entity for transactions parsed from bank SMS.
 * Uses Gemini Nano for AI-powered parsing on Realme GT6.
 */
@Entity(tableName = "sms_transactions", foreignKeys = @ForeignKey(entity = BankAccount.class, parentColumns = "id", childColumns = "bankAccountId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("bankAccountId"),
        @Index("timestamp"),
        @Index("category")
})
public class SmsTransaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Foreign key to BankAccount
     */
    private long bankAccountId;

    /**
     * Original SMS text (for debugging/re-parsing)
     */
    private String rawSms;

    /**
     * Transaction amount
     */
    private double amount;

    /**
     * Transaction type: DEBIT, CREDIT
     */
    private String type;

    /**
     * Merchant name (extracted by AI)
     */
    private String merchant;

    /**
     * Account balance after transaction
     */
    private double balanceAfter;

    /**
     * Transaction timestamp
     */
    private long timestamp;

    /**
     * Expense category (auto or manual)
     */
    private String category;

    /**
     * Whether category was manually set by user
     */
    private boolean categoryManual = false;

    /**
     * UPI/Reference ID if available
     */
    private String referenceId;

    /**
     * Sender ID from SMS (e.g., HDFC-Bank)
     */
    private String smsSenderId;

    /**
     * Whether this is marked as spam/ignored
     */
    private boolean isSpam = false;

    /**
     * User notes
     */
    private String notes;

    // Constructors
    public SmsTransaction() {
    }

    public SmsTransaction(long bankAccountId, String rawSms, double amount, String type,
            String merchant, double balanceAfter, long timestamp) {
        this.bankAccountId = bankAccountId;
        this.rawSms = rawSms;
        this.amount = amount;
        this.type = type;
        this.merchant = merchant;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public String getRawSms() {
        return rawSms;
    }

    public void setRawSms(String rawSms) {
        this.rawSms = rawSms;
    }

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

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCategoryManual() {
        return categoryManual;
    }

    public void setCategoryManual(boolean categoryManual) {
        this.categoryManual = categoryManual;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getSmsSenderId() {
        return smsSenderId;
    }

    public void setSmsSenderId(String smsSenderId) {
        this.smsSenderId = smsSenderId;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setSpam(boolean spam) {
        isSpam = spam;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Check if this is a debit transaction
     */
    public boolean isDebit() {
        return "DEBIT".equalsIgnoreCase(type);
    }

    /**
     * Check if this is a credit transaction
     */
    public boolean isCredit() {
        return "CREDIT".equalsIgnoreCase(type);
    }
}
