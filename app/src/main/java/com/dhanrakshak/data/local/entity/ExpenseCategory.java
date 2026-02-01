package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Expense Category entity for transaction categorization.
 * Supports both system-defined and user-defined categories.
 */
@Entity(tableName = "expense_categories")
public class ExpenseCategory {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Category name (e.g., Food, Shopping, Transport)
     */
    private String name;

    /**
     * Icon identifier (Material Icons name)
     */
    private String icon;

    /**
     * Color hex code for display
     */
    private String color;

    /**
     * Whether this is a user-created custom category
     */
    private boolean isCustom = false;

    /**
     * Parent category ID for sub-categories (0 if top-level)
     */
    private long parentId = 0;

    /**
     * Display order for sorting
     */
    private int displayOrder = 0;

    /**
     * Budget limit for this category (0 = no limit)
     */
    private double budgetLimit = 0;

    // Constructors
    public ExpenseCategory() {
    }

    public ExpenseCategory(String name, String icon, String color, boolean isCustom) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.isCustom = isCustom;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }
}
