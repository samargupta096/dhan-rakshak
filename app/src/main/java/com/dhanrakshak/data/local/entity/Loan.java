package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Loan entity for tracking all types of loans.
 * Supports Home, Car, Personal, Education, Gold loans.
 */
@Entity(tableName = "loans")
public class Loan {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String lenderName; // Bank/NBFC name
    private String loanType; // HOME, CAR, PERSONAL, EDUCATION, GOLD, CREDIT_CARD, OTHER
    private String accountNumber;

    private double principalAmount;
    private double outstandingPrincipal;
    private double interestRate; // Annual rate
    private int tenureMonths;
    private int emiPaidCount;

    private double emiAmount;
    private int emiDay; // Day of month EMI is due

    private long disbursementDate;
    private long maturityDate;
    private long nextEmiDate;

    private double totalInterestPaid;
    private double totalPrincipalPaid;

    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    public Loan(String name, String lenderName, String loanType,
            double principalAmount, double interestRate, int tenureMonths) {
        this.name = name;
        this.lenderName = lenderName;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.outstandingPrincipal = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.emiPaidCount = 0;
        this.totalInterestPaid = 0;
        this.totalPrincipalPaid = 0;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.disbursementDate = System.currentTimeMillis();

        calculateEmi();
        calculateMaturityDate();
    }

    /**
     * Calculate EMI using standard formula.
     * EMI = P × r × (1 + r)^n / ((1 + r)^n – 1)
     */
    public void calculateEmi() {
        double monthlyRate = interestRate / 100.0 / 12.0;
        if (monthlyRate > 0) {
            double factor = Math.pow(1 + monthlyRate, tenureMonths);
            this.emiAmount = principalAmount * monthlyRate * factor / (factor - 1);
        } else {
            this.emiAmount = principalAmount / tenureMonths;
        }
    }

    /**
     * Get EMI breakdown for a specific month.
     */
    public EmiBreakdown getEmiBreakdown(int monthNumber) {
        double remainingPrincipal = principalAmount;
        double interestComponent = 0;
        double principalComponent = 0;
        double monthlyRate = interestRate / 100.0 / 12.0;

        for (int i = 1; i <= monthNumber; i++) {
            interestComponent = remainingPrincipal * monthlyRate;
            principalComponent = emiAmount - interestComponent;
            remainingPrincipal -= principalComponent;
        }

        return new EmiBreakdown(interestComponent, principalComponent,
                Math.max(0, remainingPrincipal));
    }

    /**
     * Calculate impact of prepayment.
     */
    public PrepaymentImpact calculatePrepaymentImpact(double prepaymentAmount) {
        // Without prepayment
        int remainingMonths = tenureMonths - emiPaidCount;
        double totalWithout = emiAmount * remainingMonths;

        // With prepayment - reduce principal
        double newPrincipal = outstandingPrincipal - prepaymentAmount;
        double monthlyRate = interestRate / 100.0 / 12.0;

        // Option 1: Keep EMI same, reduce tenure
        int newTenure = 0;
        if (emiAmount > 0 && monthlyRate > 0) {
            newTenure = (int) Math.ceil(
                    Math.log(emiAmount / (emiAmount - newPrincipal * monthlyRate)) /
                            Math.log(1 + monthlyRate));
        }
        double totalWithReduced = emiAmount * newTenure;

        return new PrepaymentImpact(
                remainingMonths - newTenure,
                totalWithout - totalWithReduced - prepaymentAmount);
    }

    private void calculateMaturityDate() {
        long monthsInMillis = tenureMonths * 30L * 24 * 60 * 60 * 1000;
        this.maturityDate = disbursementDate + monthsInMillis;
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

    public String getLenderName() {
        return lenderName;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(double outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public int getEmiPaidCount() {
        return emiPaidCount;
    }

    public void setEmiPaidCount(int emiPaidCount) {
        this.emiPaidCount = emiPaidCount;
    }

    public double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public int getEmiDay() {
        return emiDay;
    }

    public void setEmiDay(int emiDay) {
        this.emiDay = emiDay;
    }

    public long getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(long disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public long getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(long maturityDate) {
        this.maturityDate = maturityDate;
    }

    public long getNextEmiDate() {
        return nextEmiDate;
    }

    public void setNextEmiDate(long nextEmiDate) {
        this.nextEmiDate = nextEmiDate;
    }

    public double getTotalInterestPaid() {
        return totalInterestPaid;
    }

    public void setTotalInterestPaid(double totalInterestPaid) {
        this.totalInterestPaid = totalInterestPaid;
    }

    public double getTotalPrincipalPaid() {
        return totalPrincipalPaid;
    }

    public void setTotalPrincipalPaid(double totalPrincipalPaid) {
        this.totalPrincipalPaid = totalPrincipalPaid;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    // Helper methods
    public double getTotalAmountPayable() {
        return emiAmount * tenureMonths;
    }

    public double getTotalInterestPayable() {
        return getTotalAmountPayable() - principalAmount;
    }

    public double getPaymentProgress() {
        if (tenureMonths <= 0)
            return 100;
        return (emiPaidCount * 100.0) / tenureMonths;
    }

    public int getRemainingMonths() {
        return tenureMonths - emiPaidCount;
    }

    /**
     * EMI breakdown helper class.
     */
    public static class EmiBreakdown {
        public final double interest;
        public final double principal;
        public final double remainingPrincipal;

        public EmiBreakdown(double interest, double principal, double remainingPrincipal) {
            this.interest = interest;
            this.principal = principal;
            this.remainingPrincipal = remainingPrincipal;
        }
    }

    /**
     * Prepayment impact helper class.
     */
    public static class PrepaymentImpact {
        public final int monthsSaved;
        public final double interestSaved;

        public PrepaymentImpact(int monthsSaved, double interestSaved) {
            this.monthsSaved = monthsSaved;
            this.interestSaved = interestSaved;
        }
    }
}
