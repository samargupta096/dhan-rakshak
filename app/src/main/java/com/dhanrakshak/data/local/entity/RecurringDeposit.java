package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Recurring Deposit entity for tracking RDs.
 * Supports monthly contribution tracking and maturity calculation.
 */
@Entity(tableName = "recurring_deposits")
public class RecurringDeposit {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Bank name where RD is held
     */
    private String bankName;

    /**
     * RD account number
     */
    private String rdNumber;

    /**
     * Monthly deposit amount
     */
    private double monthlyAmount;

    /**
     * Annual interest rate (e.g., 6.5 for 6.5%)
     */
    private double interestRate;

    /**
     * Start date as timestamp
     */
    private long startDate;

    /**
     * Tenure in months
     */
    private int tenureMonths;

    /**
     * Number of installments paid
     */
    private int installmentsPaid;

    /**
     * Maturity date as timestamp
     */
    private long maturityDate;

    /**
     * Projected maturity amount
     */
    private double maturityAmount;

    /**
     * Current deposited amount (monthlyAmount * installmentsPaid)
     */
    private double depositedAmount;

    /**
     * Status: ACTIVE, MATURED, CLOSED
     */
    private String status = "ACTIVE";

    /**
     * Notes
     */
    private String notes;

    // Constructors
    public RecurringDeposit() {
    }

    public RecurringDeposit(String bankName, double monthlyAmount, double interestRate,
            long startDate, int tenureMonths) {
        this.bankName = bankName;
        this.monthlyAmount = monthlyAmount;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.tenureMonths = tenureMonths;
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

    public String getRdNumber() {
        return rdNumber;
    }

    public void setRdNumber(String rdNumber) {
        this.rdNumber = rdNumber;
    }

    public double getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public void setInstallmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
        this.depositedAmount = monthlyAmount * installmentsPaid;
    }

    public long getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(long maturityDate) {
        this.maturityDate = maturityDate;
    }

    public double getMaturityAmount() {
        return maturityAmount;
    }

    public void setMaturityAmount(double maturityAmount) {
        this.maturityAmount = maturityAmount;
    }

    public double getDepositedAmount() {
        return depositedAmount;
    }

    public void setDepositedAmount(double depositedAmount) {
        this.depositedAmount = depositedAmount;
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
     * Calculate RD maturity amount.
     * Uses standard RD formula: M = P * [(1+r/n)^nt - 1] / [1 - (1+r/n)^(-1/3)]
     * Simplified quarterly compounding
     */
    public void calculateMaturityAmount() {
        double r = interestRate / 100.0;
        double n = 4; // Quarterly compounding
        double totalMonths = tenureMonths;

        // RD maturity calculation with quarterly compounding
        double i = r / n;
        double maturity = 0;

        for (int month = 0; month < totalMonths; month++) {
            double remainingQuarters = (totalMonths - month) / 3.0;
            maturity += monthlyAmount * Math.pow(1 + i, remainingQuarters);
        }

        this.maturityAmount = maturity;
        this.maturityDate = startDate + (tenureMonths * 30L * 24 * 60 * 60 * 1000);
    }

    /**
     * Get current accumulated value with interest
     */
    public double getCurrentValue() {
        if (installmentsPaid == 0)
            return 0;

        double r = interestRate / 100.0;
        double n = 4;
        double i = r / n;
        double value = 0;

        for (int month = 0; month < installmentsPaid; month++) {
            double elapsedMonths = installmentsPaid - month;
            double quarters = elapsedMonths / 3.0;
            value += monthlyAmount * Math.pow(1 + i, quarters);
        }

        return value;
    }

    /**
     * Get remaining installments
     */
    public int getRemainingInstallments() {
        return tenureMonths - installmentsPaid;
    }

    /**
     * Get total interest earned so far
     */
    public double getInterestEarned() {
        return getCurrentValue() - depositedAmount;
    }
}
