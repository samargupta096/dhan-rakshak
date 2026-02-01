package com.dhanrakshak.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for Stock Quote API response.
 */
public class StockQuoteDto {

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("lastPrice")
    private double lastPrice;

    @SerializedName("open")
    private double open;

    @SerializedName("high")
    private double high;

    @SerializedName("low")
    private double low;

    @SerializedName("previousClose")
    private double previousClose;

    @SerializedName("change")
    private double change;

    @SerializedName("pChange")
    private double percentChange;

    @SerializedName("totalTradedVolume")
    private long volume;

    @SerializedName("companyName")
    private String companyName;

    // Getters
    public String getSymbol() {
        return symbol;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getPreviousClose() {
        return previousClose;
    }

    public double getChange() {
        return change;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public long getVolume() {
        return volume;
    }

    public String getCompanyName() {
        return companyName;
    }
}
