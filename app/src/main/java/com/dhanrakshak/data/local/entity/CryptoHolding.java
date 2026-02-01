package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Crypto holding entity for tracking cryptocurrency investments.
 */
@Entity(tableName = "crypto_holdings")
public class CryptoHolding {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String symbol; // BTC, ETH, SOL, etc.
    private String name; // Bitcoin, Ethereum, Solana
    private String coinGeckoId; // For API calls

    private double quantity;
    private double averageBuyPrice; // In INR
    private double currentPriceInr;
    private double currentPriceUsd;

    private double investedAmount;
    private double currentValue;

    private String exchange; // WazirX, CoinDCX, Binance, etc.
    private String walletAddress; // Optional

    private long lastPriceUpdate;
    private long createdAt;
    private long updatedAt;

    public CryptoHolding(String symbol, String name, String coinGeckoId,
            double quantity, double averageBuyPrice) {
        this.symbol = symbol.toUpperCase();
        this.name = name;
        this.coinGeckoId = coinGeckoId;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.investedAmount = quantity * averageBuyPrice;
        this.currentPriceInr = averageBuyPrice;
        this.currentValue = investedAmount;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoinGeckoId() {
        return coinGeckoId;
    }

    public void setCoinGeckoId(String coinGeckoId) {
        this.coinGeckoId = coinGeckoId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        updateValues();
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(double averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
        updateValues();
    }

    public double getCurrentPriceInr() {
        return currentPriceInr;
    }

    public void setCurrentPriceInr(double currentPriceInr) {
        this.currentPriceInr = currentPriceInr;
        this.lastPriceUpdate = System.currentTimeMillis();
        updateValues();
    }

    public double getCurrentPriceUsd() {
        return currentPriceUsd;
    }

    public void setCurrentPriceUsd(double currentPriceUsd) {
        this.currentPriceUsd = currentPriceUsd;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(double investedAmount) {
        this.investedAmount = investedAmount;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public long getLastPriceUpdate() {
        return lastPriceUpdate;
    }

    public void setLastPriceUpdate(long lastPriceUpdate) {
        this.lastPriceUpdate = lastPriceUpdate;
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

    private void updateValues() {
        this.investedAmount = quantity * averageBuyPrice;
        this.currentValue = quantity * currentPriceInr;
        this.updatedAt = System.currentTimeMillis();
    }

    // Helper methods
    public double getProfitLoss() {
        return currentValue - investedAmount;
    }

    public double getProfitLossPercentage() {
        if (investedAmount <= 0)
            return 0;
        return ((currentValue - investedAmount) / investedAmount) * 100;
    }

    public boolean isPriceStale() {
        // Consider price stale if older than 5 minutes
        return System.currentTimeMillis() - lastPriceUpdate > 5 * 60 * 1000;
    }

    /**
     * Get icon URL for the crypto coin.
     */
    public String getIconUrl() {
        return "https://assets.coingecko.com/coins/images/" + coinGeckoId + "/small";
    }
}
