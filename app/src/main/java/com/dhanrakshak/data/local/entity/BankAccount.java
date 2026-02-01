package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Bank Account entity for tracking bank balances.
 * Updated via SMS parsing with Gemini Nano.
 */
@Entity(tableName = "bank_accounts")
public class BankAccount {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Bank name: HDFC, ICICI, AXIS, SBI, STANDARD_CHARTERED, etc.
     */
    private String bankName;

    /**
     * Account type: SAVINGS, CURRENT
     */
    private String accountType;

    /**
     * Last 4 digits of account number (for display/matching)
     */
    private String accountNumberLast4;

    /**
     * Full account number (encrypted in DB via SQLCipher)
     */
    private String accountNumber;

    /**
     * Current balance (updated from SMS)
     */
    private double balance;

    /**
     * Account holder name
     */
    private String holderName;

    /**
     * IFSC code
     */
    private String ifscCode;

    /**
     * Last updated timestamp
     */
    private long lastUpdated;

    /**
     * Whether this account is active
     */
    private boolean isActive = true;

    /**
     * Color for UI display
     */
    private String displayColor;

    // Constructors
    public BankAccount() {
    }

    public BankAccount(String bankName, String accountType, String accountNumberLast4) {
        this.bankName = bankName;
        this.accountType = accountType;
        this.accountNumberLast4 = accountNumberLast4;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumberLast4() {
        return accountNumberLast4;
    }

    public void setAccountNumberLast4(String accountNumberLast4) {
        this.accountNumberLast4 = accountNumberLast4;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        if (accountNumber != null && accountNumber.length() >= 4) {
            this.accountNumberLast4 = accountNumber.substring(accountNumber.length() - 4);
        }
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDisplayColor() {
        return displayColor;
    }

    public void setDisplayColor(String displayColor) {
        this.displayColor = displayColor;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return bankName + " •••• " + accountNumberLast4;
    }
}
