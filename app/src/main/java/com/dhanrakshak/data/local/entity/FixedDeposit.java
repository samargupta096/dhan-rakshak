package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Fixed Deposit entity for tracking FDs.
 * Supports interest calculation for maturity projections.
 */
@Entity(tableName = "fixed_deposits")
public class FixedDeposit {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Bank name where FD is held
     */
    private String bankName;

    /**
     * FD account/certificate number
     */
    private String fdNumber;

    /**
     * Principal amount
     */
    private double principal;

    /**
     * Annual interest rate (e.g., 7.5 for 7.5%)
     */
    private double interestRate;

    /**
     * Compounding frequency: MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY
     */
    private String compoundingFrequency;

    /**
     * Start date as timestamp
     */
    private long startDate;

    /**
     * Maturity date as timestamp
     */
    private long maturityDate;

    /**
     * Tenure in months
     */
    private int tenureMonths;

    /**
     * Maturity amount (calculated)
     */
    private double maturityAmount;

    /**
     * Whether this is a tax-saving FD (5-year lock-in)
     */
    private boolean isTaxSaver = false;

    /**
     * Whether interest is cumulative or paid out
     */
    private boolean isCumulative = true;

    /**
     * Status: ACTIVE, MATURED, CLOSED
     */
    private String status = "ACTIVE";

    /**
     * Notes
     */
    private String notes;

    // Constructors
    public FixedDeposit() {
    }

    public FixedDeposit(String bankName, double principal, double interestRate,
            long startDate, long maturityDate) {
        this.bankName = bankName;
        this.principal = principal;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        calculateMaturityAmount();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getFdNumber() {
        return fdNumber;
    }

    public void setFdNumber(String fdNumber) {
        this.fdNumber = fdNumber;
    }

    public double getPrincipal() {
        return principal;
    }

    public void setPrincipal(double principal) {
        this.principal = principal;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public String getCompoundingFrequency() {
        return compoundingFrequency;
    }

    public void setCompoundingFrequency(String compoundingFrequency) {
        this.compoundingFrequency = compoundingFrequency;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(long maturityDate) {
        this.maturityDate = maturityDate;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public double getMaturityAmount() {
        return maturityAmount;
    }

    public void setMaturityAmount(double maturityAmount) {
        this.maturityAmount = maturityAmount;
    }

    public boolean isTaxSaver() {
        return isTaxSaver;
    }

    public void setTaxSaver(boolean taxSaver) {
        isTaxSaver = taxSaver;
    }

    public boolean isCumulative() {
        return isCumulative;
    }

    public void setCumulative(boolean cumulative) {
        isCumulative = cumulative;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Calculate maturity amount using compound interest formula.
     * A = P(1 + r/n)^(nt)
     */
    public void calculateMaturityAmount() {
        int n = getCompoundingFrequencyValue();
        double years = tenureMonths / 12.0;
        double r = interestRate / 100.0;
        this.maturityAmount = principal * Math.pow((1 + r / n), n * years);
    }

    /**
     * Get current value (interpolated based on time elapsed)
     */
    public double getCurrentValue() {
        long now = System.currentTimeMillis();
        if (now >= maturityDate)
            return maturityAmount;
        if (now <= startDate)
            return principal;

        double totalDuration = maturityDate - startDate;
        double elapsed = now - startDate;
        double progress = elapsed / totalDuration;

        double totalInterest = maturityAmount - principal;
        return principal + (totalInterest * progress);
    }

    /**
     * Get total interest earned
     */
    public double getTotalInterest() {
        return maturityAmount - principal;
    }

    private int getCompoundingFrequencyValue() {
        if (compoundingFrequency == null)
            return 4; // Default quarterly
        switch (compoundingFrequency) {
            case "MONTHLY":
                return 12;
            case "QUARTERLY":
                return 4;
            case "HALF_YEARLY":
                return 2;
            case "YEARLY":
                return 1;
            default:
                return 4;
        }
    }
}
