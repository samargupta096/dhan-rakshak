package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Budget entity for monthly budget planning.
 */
@Entity(tableName = "budgets", foreignKeys = @ForeignKey(entity = ExpenseCategory.class, parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("categoryId"), @Index(value = { "categoryId", "month", "year" }, unique = true) })
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long categoryId;
    private double budgetAmount;
    private double spentAmount;
    private int month; // 1-12
    private int year;
    private boolean alertEnabled;
    private int alertThresholdPercent; // Alert when spending exceeds this % of budget
    private long createdAt;
    private long updatedAt;

    public Budget(long categoryId, double budgetAmount, int month, int year) {
        this.categoryId = categoryId;
        this.budgetAmount = budgetAmount;
        this.month = month;
        this.year = year;
        this.spentAmount = 0;
        this.alertEnabled = true;
        this.alertThresholdPercent = 80;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public int getAlertThresholdPercent() {
        return alertThresholdPercent;
    }

    public void setAlertThresholdPercent(int alertThresholdPercent) {
        this.alertThresholdPercent = alertThresholdPercent;
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
    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }

    public double getSpentPercentage() {
        if (budgetAmount <= 0)
            return 0;
        return (spentAmount / budgetAmount) * 100;
    }

    public boolean isOverBudget() {
        return spentAmount > budgetAmount;
    }

    public boolean shouldAlert() {
        return alertEnabled && getSpentPercentage() >= alertThresholdPercent;
    }
}
