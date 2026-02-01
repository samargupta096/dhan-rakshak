package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Bill Reminder entity for tracking recurring bills and subscriptions.
 */
@Entity(tableName = "bill_reminders")
public class BillReminder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String category; // UTILITY, SUBSCRIPTION, EMI, RENT, INSURANCE, OTHER
    private String provider; // Netflix, Jio, etc.

    private double amount;
    private String frequency; // MONTHLY, QUARTERLY, YEARLY, ONCE

    private int dueDayOfMonth; // 1-31
    private long nextDueDate;
    private long lastPaidDate;

    private boolean isAutoPay;
    private boolean reminderEnabled;
    private int reminderDaysBefore; // Remind X days before due

    private String linkedBankAccount; // Account from which payment is made
    private String notes;

    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    public BillReminder(String name, String category, double amount,
            String frequency, int dueDayOfMonth) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.frequency = frequency;
        this.dueDayOfMonth = dueDayOfMonth;
        this.reminderEnabled = true;
        this.reminderDaysBefore = 3;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();

        calculateNextDueDate();
    }

    public void calculateNextDueDate() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int currentDay = cal.get(java.util.Calendar.DAY_OF_MONTH);

        if (currentDay >= dueDayOfMonth) {
            // Move to next period
            switch (frequency) {
                case "MONTHLY":
                    cal.add(java.util.Calendar.MONTH, 1);
                    break;
                case "QUARTERLY":
                    cal.add(java.util.Calendar.MONTH, 3);
                    break;
                case "YEARLY":
                    cal.add(java.util.Calendar.YEAR, 1);
                    break;
            }
        }

        cal.set(java.util.Calendar.DAY_OF_MONTH,
                Math.min(dueDayOfMonth, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)));
        this.nextDueDate = cal.getTimeInMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDueDayOfMonth() {
        return dueDayOfMonth;
    }

    public void setDueDayOfMonth(int dueDayOfMonth) {
        this.dueDayOfMonth = dueDayOfMonth;
    }

    public long getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(long nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public long getLastPaidDate() {
        return lastPaidDate;
    }

    public void setLastPaidDate(long lastPaidDate) {
        this.lastPaidDate = lastPaidDate;
    }

    public boolean isAutoPay() {
        return isAutoPay;
    }

    public void setAutoPay(boolean autoPay) {
        isAutoPay = autoPay;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public int getReminderDaysBefore() {
        return reminderDaysBefore;
    }

    public void setReminderDaysBefore(int reminderDaysBefore) {
        this.reminderDaysBefore = reminderDaysBefore;
    }

    public String getLinkedBankAccount() {
        return linkedBankAccount;
    }

    public void setLinkedBankAccount(String linkedBankAccount) {
        this.linkedBankAccount = linkedBankAccount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
    public long getDaysUntilDue() {
        return (nextDueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000);
    }

    public boolean isDueSoon() {
        return getDaysUntilDue() <= reminderDaysBefore;
    }

    public boolean isOverdue() {
        return System.currentTimeMillis() > nextDueDate && lastPaidDate < nextDueDate;
    }

    public double getYearlyAmount() {
        switch (frequency) {
            case "MONTHLY":
                return amount * 12;
            case "QUARTERLY":
                return amount * 4;
            case "YEARLY":
                return amount;
            default:
                return amount;
        }
    }

    /**
     * Mark bill as paid and calculate next due date.
     */
    public void markAsPaid() {
        this.lastPaidDate = System.currentTimeMillis();
        calculateNextDueDate();
        this.updatedAt = System.currentTimeMillis();
    }
}
