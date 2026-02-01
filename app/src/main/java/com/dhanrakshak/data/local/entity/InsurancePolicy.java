package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an Insurance Policy (Health, Life, Vehicle, etc.).
 * Stores details locally for the user.
 */
@Entity(tableName = "insurance_policies")
public class InsurancePolicy {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Name of the policy (e.g., "HDFC Sanchay Par", "Star Health Optima")
     */
    private String policyName;

    /**
     * Provider Name (e.g., HDFC Life, LIC, Niva Bupa)
     */
    private String provider;

    /**
     * Policy Number (for reference)
     */
    private String policyNumber;

    /**
     * Category: HEALTH, LIFE, VEHICLE, OTHER
     */
    private String category;

    /**
     * Sum Insured / Coverage Amount
     */
    private double sumInsured;

    /**
     * Premium Amount
     */
    private double premiumAmount;

    /**
     * Frequency: ANNUALLY, MONTHLY, ONE_TIME
     */
    private String premiumFrequency;

    /**
     * Next Renewal Date (Timestamp)
     */
    private long renewalDate;

    /**
     * Associated ABHA ID (Ayushman Bharat Health Account) for Health policies
     */
    private String abhaId;

    /**
     * Policy Holder Name
     */
    private String policyHolderName;

    /**
     * Notes or Remarks
     */
    private String notes;

    // Constructors
    public InsurancePolicy() {
    }

    public InsurancePolicy(String policyName, String provider, String policyNumber, String category, double sumInsured,
            double premiumAmount, long renewalDate) {
        this.policyName = policyName;
        this.provider = provider;
        this.policyNumber = policyNumber;
        this.category = category;
        this.sumInsured = sumInsured;
        this.premiumAmount = premiumAmount;
        this.renewalDate = renewalDate;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(double sumInsured) {
        this.sumInsured = sumInsured;
    }

    public double getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(double premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public String getPremiumFrequency() {
        return premiumFrequency;
    }

    public void setPremiumFrequency(String premiumFrequency) {
        this.premiumFrequency = premiumFrequency;
    }

    public long getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(long renewalDate) {
        this.renewalDate = renewalDate;
    }

    public String getAbhaId() {
        return abhaId;
    }

    public void setAbhaId(String abhaId) {
        this.abhaId = abhaId;
    }

    public String getPolicyHolderName() {
        return policyHolderName;
    }

    public void setPolicyHolderName(String policyHolderName) {
        this.policyHolderName = policyHolderName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
