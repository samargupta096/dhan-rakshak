package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Transaction entity for tracking buy/sell/SIP transactions.
 * Linked to Asset for portfolio tracking and XIRR calculation.
 */
@Entity(tableName = "transactions", foreignKeys = @ForeignKey(entity = Asset.class, parentColumns = "id", childColumns = "assetId", onDelete = ForeignKey.CASCADE), indices = @Index("assetId"))
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Foreign key to Asset
     */
    private long assetId;

    /**
     * Transaction type: BUY, SELL, DIVIDEND, SIP
     */
    private String type;

    /**
     * Amount in currency
     */
    private double amount;

    /**
     * Number of units bought/sold
     */
    private double units;

    /**
     * Price per unit at transaction time
     */
    private double pricePerUnit;

    /**
     * Transaction date as timestamp
     */
    private long date;

    /**
     * Description or notes
     */
    private String description;

    /**
     * Platform where transaction occurred (e.g., Zerodha, Groww)
     */
    private String platform;

    // Constructors
    public Transaction() {
    }

    public Transaction(long assetId, String type, double amount, double units, double pricePerUnit, long date) {
        this.assetId = assetId;
        this.type = type;
        this.amount = amount;
        this.units = units;
        this.pricePerUnit = pricePerUnit;
        this.date = date;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
