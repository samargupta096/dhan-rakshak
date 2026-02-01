package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Financial Goal entity for goal-based planning.
 * Supports goals like House, Car, Retirement, Child Education, etc.
 */
@Entity(tableName = "financial_goals")
public class FinancialGoal {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String category; // HOUSE, CAR, RETIREMENT, EDUCATION, WEDDING, TRAVEL, EMERGENCY, CUSTOM
    private String iconName;

    private double targetAmount;
    private double currentAmount;
    private double monthlySipRequired;

    private long targetDate;
    private long createdAt;
    private long updatedAt;

    private double expectedReturnRate; // Annual return rate %
    private String linkedAssetIds; // Comma-separated asset IDs linked to this goal

    private int priority; // 1 = High, 2 = Medium, 3 = Low
    private boolean isCompleted;
    private String notes;

    public FinancialGoal(String name, String category, double targetAmount, long targetDate) {
        this.name = name;
        this.category = category;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.currentAmount = 0;
        this.expectedReturnRate = 12.0; // Default 12% for equity
        this.priority = 2;
        this.isCompleted = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();

        // Set default icon based on category
        switch (category) {
            case "HOUSE":
                this.iconName = "home";
                break;
            case "CAR":
                this.iconName = "directions_car";
                break;
            case "RETIREMENT":
                this.iconName = "beach_access";
                break;
            case "EDUCATION":
                this.iconName = "school";
                break;
            case "WEDDING":
                this.iconName = "favorite";
                break;
            case "TRAVEL":
                this.iconName = "flight";
                break;
            case "EMERGENCY":
                this.iconName = "medical_services";
                break;
            default:
                this.iconName = "flag";
                break;
        }

        calculateRequiredSip();
    }

    /**
     * Calculate monthly SIP required to reach the goal.
     * Uses future value formula with compound interest.
     */
    public void calculateRequiredSip() {
        if (targetDate <= System.currentTimeMillis()) {
            this.monthlySipRequired = targetAmount - currentAmount;
            return;
        }

        double remainingAmount = targetAmount - currentAmount;
        if (remainingAmount <= 0) {
            this.monthlySipRequired = 0;
            this.isCompleted = true;
            return;
        }

        // Calculate months remaining
        long monthsRemaining = (targetDate - System.currentTimeMillis()) / (30L * 24 * 60 * 60 * 1000);
        if (monthsRemaining <= 0)
            monthsRemaining = 1;

        // Monthly return rate
        double monthlyRate = expectedReturnRate / 100.0 / 12.0;

        // SIP formula: FV = P * [(1+r)^n - 1] / r * (1+r)
        // Solving for P: P = FV * r / [(1+r)^n - 1] / (1+r)
        if (monthlyRate > 0) {
            double factor = Math.pow(1 + monthlyRate, monthsRemaining);
            this.monthlySipRequired = remainingAmount * monthlyRate / (factor - 1);
        } else {
            this.monthlySipRequired = remainingAmount / monthsRemaining;
        }
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
        calculateRequiredSip();
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
        calculateRequiredSip();
    }

    public double getMonthlySipRequired() {
        return monthlySipRequired;
    }

    public void setMonthlySipRequired(double monthlySipRequired) {
        this.monthlySipRequired = monthlySipRequired;
    }

    public long getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(long targetDate) {
        this.targetDate = targetDate;
        calculateRequiredSip();
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

    public double getExpectedReturnRate() {
        return expectedReturnRate;
    }

    public void setExpectedReturnRate(double expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
        calculateRequiredSip();
    }

    public String getLinkedAssetIds() {
        return linkedAssetIds;
    }

    public void setLinkedAssetIds(String linkedAssetIds) {
        this.linkedAssetIds = linkedAssetIds;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public double getProgressPercentage() {
        if (targetAmount <= 0)
            return 0;
        return Math.min(100.0, (currentAmount / targetAmount) * 100);
    }

    public long getDaysRemaining() {
        return Math.max(0, (targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000));
    }

    public long getMonthsRemaining() {
        return Math.max(0, (targetDate - System.currentTimeMillis()) / (30L * 24 * 60 * 60 * 1000));
    }

    public boolean isOnTrack() {
        // Check if current progress is ahead of linear projection
        long totalDuration = targetDate - createdAt;
        long elapsed = System.currentTimeMillis() - createdAt;
        if (totalDuration <= 0)
            return currentAmount >= targetAmount;

        double expectedProgress = (double) elapsed / totalDuration;
        double actualProgress = currentAmount / targetAmount;
        return actualProgress >= expectedProgress * 0.9; // 90% tolerance
    }
}
