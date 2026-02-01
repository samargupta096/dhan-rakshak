package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * UPI Account entity for managing linked UPI IDs and balance tracking.
 * Supports multiple UPI apps: GPay, PhonePe, Paytm, BHIM, etc.
 */
@Entity(tableName = "upi_accounts")
public class UpiAccount {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String upiId; // example@okaxis, example@ybl
    private String bankName;
    private String accountHolderName;
    private String accountNumberLast4; // Last 4 digits only for privacy

    // Linked bank account in app (optional)
    private Long linkedBankAccountId;

    // UPI App info
    private String upiApp; // GPAY, PHONEPE, PAYTM, BHIM, AMAZONPAY, WHATSAPP
    private String upiHandle; // @okaxis, @ybl, @paytm, @upi

    // Balance (updated via SMS parsing after balance check)
    private double lastKnownBalance;
    private long lastBalanceCheckTime;
    private boolean isBalanceStale;

    // Status
    private boolean isPrimary;
    private boolean isActive;
    private int usageCount; // Track which UPI is used most

    // Limits
    private double dailyLimit;
    private double monthlyLimit;
    private double dailyUsed;
    private double monthlyUsed;

    private long createdAt;
    private long updatedAt;

    public UpiAccount(String upiId, String bankName, String upiApp) {
        this.upiId = upiId.toLowerCase();
        this.bankName = bankName;
        this.upiApp = upiApp;
        this.upiHandle = extractHandle(upiId);
        this.isActive = true;
        this.isPrimary = false;
        this.usageCount = 0;
        this.dailyLimit = 100000; // Default ₹1L
        this.monthlyLimit = 500000; // Default ₹5L
        this.dailyUsed = 0;
        this.monthlyUsed = 0;
        this.isBalanceStale = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    private String extractHandle(String upiId) {
        if (upiId != null && upiId.contains("@")) {
            return upiId.substring(upiId.indexOf("@"));
        }
        return "@upi";
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
        this.upiHandle = extractHandle(upiId);
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountNumberLast4() {
        return accountNumberLast4;
    }

    public void setAccountNumberLast4(String accountNumberLast4) {
        this.accountNumberLast4 = accountNumberLast4;
    }

    public Long getLinkedBankAccountId() {
        return linkedBankAccountId;
    }

    public void setLinkedBankAccountId(Long linkedBankAccountId) {
        this.linkedBankAccountId = linkedBankAccountId;
    }

    public String getUpiApp() {
        return upiApp;
    }

    public void setUpiApp(String upiApp) {
        this.upiApp = upiApp;
    }

    public String getUpiHandle() {
        return upiHandle;
    }

    public void setUpiHandle(String upiHandle) {
        this.upiHandle = upiHandle;
    }

    public double getLastKnownBalance() {
        return lastKnownBalance;
    }

    public void setLastKnownBalance(double lastKnownBalance) {
        this.lastKnownBalance = lastKnownBalance;
        this.lastBalanceCheckTime = System.currentTimeMillis();
        this.isBalanceStale = false;
    }

    public long getLastBalanceCheckTime() {
        return lastBalanceCheckTime;
    }

    public void setLastBalanceCheckTime(long lastBalanceCheckTime) {
        this.lastBalanceCheckTime = lastBalanceCheckTime;
    }

    public boolean isBalanceStale() {
        return isBalanceStale;
    }

    public void setBalanceStale(boolean balanceStale) {
        isBalanceStale = balanceStale;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getDailyUsed() {
        return dailyUsed;
    }

    public void setDailyUsed(double dailyUsed) {
        this.dailyUsed = dailyUsed;
    }

    public double getMonthlyUsed() {
        return monthlyUsed;
    }

    public void setMonthlyUsed(double monthlyUsed) {
        this.monthlyUsed = monthlyUsed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getUpiAppDisplayName() {
        switch (upiApp) {
            case "GPAY":
                return "Google Pay";
            case "PHONEPE":
                return "PhonePe";
            case "PAYTM":
                return "Paytm";
            case "BHIM":
                return "BHIM";
            case "AMAZONPAY":
                return "Amazon Pay";
            case "WHATSAPP":
                return "WhatsApp Pay";
            default:
                return upiApp;
        }
    }

    public String getUpiAppPackage() {
        switch (upiApp) {
            case "GPAY":
                return "com.google.android.apps.nbu.paisa.user";
            case "PHONEPE":
                return "com.phonepe.app";
            case "PAYTM":
                return "net.one97.paytm";
            case "BHIM":
                return "in.org.npci.upiapp";
            case "AMAZONPAY":
                return "in.amazon.mShop.android.shopping";
            case "WHATSAPP":
                return "com.whatsapp";
            default:
                return null;
        }
    }

    public double getRemainingDailyLimit() {
        return Math.max(0, dailyLimit - dailyUsed);
    }

    public double getRemainingMonthlyLimit() {
        return Math.max(0, monthlyLimit - monthlyUsed);
    }

    public boolean isBalanceCheckNeeded() {
        // Consider stale if older than 6 hours
        return isBalanceStale ||
                (System.currentTimeMillis() - lastBalanceCheckTime > 6 * 60 * 60 * 1000);
    }

    public String getMaskedUpiId() {
        if (upiId == null)
            return "***";
        int atIndex = upiId.indexOf("@");
        if (atIndex <= 2)
            return upiId;
        return upiId.substring(0, 2) + "***" + upiId.substring(atIndex);
    }

    public void incrementUsage(double amount) {
        this.usageCount++;
        this.dailyUsed += amount;
        this.monthlyUsed += amount;
        this.updatedAt = System.currentTimeMillis();
    }

    public void resetDailyUsage() {
        this.dailyUsed = 0;
    }

    public void resetMonthlyUsage() {
        this.monthlyUsed = 0;
        this.dailyUsed = 0;
    }
}
