package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Mutual Fund Scheme Master data from AMFI.
 * Cached locally for autocomplete and NAV lookup.
 */
@Entity(tableName = "mf_schemes")
public class MutualFundScheme {

    @PrimaryKey
    private long schemeCode;

    /**
     * Full scheme name
     */
    private String schemeName;

    /**
     * ISIN code (for dividends: Div Payout/Reinvest, for growth: Growth)
     */
    private String isin;

    /**
     * AMC (Asset Management Company) name
     */
    private String amcName;

    /**
     * Scheme type: EQUITY, DEBT, HYBRID, SOLUTION_ORIENTED, OTHER
     */
    private String schemeType;

    /**
     * Scheme category (e.g., Large Cap, Mid Cap, Short Duration)
     */
    private String schemeCategory;

    /**
     * Latest NAV
     */
    private double latestNav;

    /**
     * NAV date
     */
    private long navDate;

    /**
     * Last updated timestamp
     */
    private long lastUpdated;

    // Constructors
    public MutualFundScheme() {
    }

    public MutualFundScheme(long schemeCode, String schemeName, String isin, double latestNav) {
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.isin = isin;
        this.latestNav = latestNav;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(long schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getAmcName() {
        return amcName;
    }

    public void setAmcName(String amcName) {
        this.amcName = amcName;
    }

    public String getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    public String getSchemeCategory() {
        return schemeCategory;
    }

    public void setSchemeCategory(String schemeCategory) {
        this.schemeCategory = schemeCategory;
    }

    public double getLatestNav() {
        return latestNav;
    }

    public void setLatestNav(double latestNav) {
        this.latestNav = latestNav;
    }

    public long getNavDate() {
        return navDate;
    }

    public void setNavDate(long navDate) {
        this.navDate = navDate;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
