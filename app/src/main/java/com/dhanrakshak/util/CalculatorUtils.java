package com.dhanrakshak.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorUtils {

    /**
     * Calculate Fixed Deposit Maturity Amount.
     * Formula: A = P * (1 + r/n)^(n*t)
     * Assuming quarterly compounding (n=4) which is standard in India.
     */
    public static double calculateFD(double principal, double annualRate, double years) {
        double r = annualRate / 100;
        int n = 4; // Quarterly compounding
        double amount = principal * Math.pow(1 + (r / n), n * years);
        return round(amount);
    }

    /**
     * Calculate Recurring Deposit Maturity Amount.
     * Formula: M = P * n + P * n(n+1)/2 * r/12/100 (Simple Interest approximation
     * often used)
     * Or Compound Interest version: M = P * ((1+r/n)^(nt) - 1) / (1-(1+r/n)^(-1/3))
     * (Complex)
     * 
     * simplified standard RD formula used by banks (quarterly compounding):
     */
    public static double calculateRD(double monthlyAmount, double annualRate, double years) {
        // Using a standard approximation for quarterly compounding implementation
        // Total Months
        int months = (int) (years * 12);
        double rate = annualRate / 100;
        double totalAmount = 0;

        // Calculate interest for each deposit
        // First deposit earns interest for 'months', second for 'months-1', etc.
        // But with compounding it's tricky.
        // Using the standard formula available in finance libraries:
        // A = P * ((1+i)^n - 1) / i * (1+i) where i is monthly rate.

        double i = rate / 12; // Monthly rate
        totalAmount = monthlyAmount * ((Math.pow(1 + i, months) - 1) / i) * (1 + i);

        return round(totalAmount);
    }

    /**
     * Calculate SIP Future Value.
     * Formula: FV = P * [(1+i)^n - 1] * (1+i) / i
     */
    public static double calculateSIP(double monthlyAmount, double annualRate, double years) {
        double i = annualRate / 100 / 12; // Monthly rate
        double n = years * 12; // Total months

        double futureValue = monthlyAmount * ((Math.pow(1 + i, n) - 1) / i) * (1 + i);
        return round(futureValue);
    }

    /**
     * Calculate Lumpsum Investment Future Value (CAGR).
     * Formula: A = P * (1 + r)^t
     */
    public static double calculateLumpsum(double principal, double annualRate, double years) {
        double amount = principal * Math.pow(1 + (annualRate / 100), years);
        return round(amount);
    }

    private static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
