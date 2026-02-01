package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Asset entity representing any financial asset.
 * Supports: Stocks, Mutual Funds, Gold, EPF, PPF, Bonds, FD, RD
 */
@Entity(tableName = "assets")
public class Asset {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Type of asset: STOCK, MUTUAL_FUND, GOLD, EPF, PPF, BOND, FD, RD
     */
    private String assetType;

    /**
     * Display name of the asset
     */
    private String name;

    /**
     * Unique identifier (e.g., ticker symbol, ISIN, scheme code)
     */
    private String identifier;

    /**
     * Quantity held (units for MF, shares for stocks)
     */
    private double quantity;

    /**
     * Average purchase price per unit
     */
    private double avgPurchasePrice;

    /**
     * Current price per unit (from API)
     */
    private double currentPrice;

    /**
     * Current total value (quantity * currentPrice)
     */
    private double currentValue;

    /**
     * Total invested amount
     */
    private double investedAmount;

    /**
     * Currency code (default: INR)
     */
    private String currency = "INR";

    /**
     * Last updated timestamp
     */
    private long lastUpdated;

    /**
     * Notes or additional info
     */
    private String notes;

    // Constructors
    public Asset() {
    }

    public Asset(String assetType, String name, String identifier, double quantity, double avgPurchasePrice) {
        this.assetType = assetType;
        this.name = name;
        this.identifier = identifier;
        this.quantity = quantity;
        this.avgPurchasePrice = avgPurchasePrice;
        this.investedAmount = quantity * avgPurchasePrice;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getAvgPurchasePrice() {
        return avgPurchasePrice;
    }

    public void setAvgPurchasePrice(double avgPurchasePrice) {
        this.avgPurchasePrice = avgPurchasePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        this.currentValue = this.quantity * currentPrice;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(double investedAmount) {
        this.investedAmount = investedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Calculate profit/loss
     */
    public double getProfitLoss() {
        return currentValue - investedAmount;
    }

    /**
     * Calculate profit/loss percentage
     */
    public double getProfitLossPercentage() {
        if (investedAmount == 0)
            return 0;
        return ((currentValue - investedAmount) / investedAmount) * 100;
    }
}
