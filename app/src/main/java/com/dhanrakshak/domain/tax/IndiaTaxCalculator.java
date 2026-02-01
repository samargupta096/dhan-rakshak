package com.dhanrakshak.domain.tax;

import java.util.ArrayList;
import java.util.List;

/**
 * Indian Tax Calculator for FY 2024-25.
 * Supports both Old and New tax regimes with comparison.
 */
public class IndiaTaxCalculator {

    // New Regime Tax Slabs (FY 2024-25)
    private static final double[][] NEW_REGIME_SLABS = {
            { 0, 300000, 0 }, // 0-3L: 0%
            { 300000, 700000, 5 }, // 3-7L: 5%
            { 700000, 1000000, 10 }, // 7-10L: 10%
            { 1000000, 1200000, 15 }, // 10-12L: 15%
            { 1200000, 1500000, 20 }, // 12-15L: 20%
            { 1500000, Double.MAX_VALUE, 30 } // >15L: 30%
    };

    // Old Regime Tax Slabs
    private static final double[][] OLD_REGIME_SLABS = {
            { 0, 250000, 0 }, // 0-2.5L: 0%
            { 250000, 500000, 5 }, // 2.5-5L: 5%
            { 500000, 1000000, 20 }, // 5-10L: 20%
            { 1000000, Double.MAX_VALUE, 30 } // >10L: 30%
    };

    // Standard Deduction (New Regime FY 2024-25)
    private static final double NEW_REGIME_STANDARD_DEDUCTION = 75000;
    private static final double OLD_REGIME_STANDARD_DEDUCTION = 50000;

    // Section 80C limit
    public static final double SECTION_80C_LIMIT = 150000;

    // Section 80D limits (Health Insurance)
    public static final double SECTION_80D_SELF = 25000;
    public static final double SECTION_80D_PARENTS = 25000;
    public static final double SECTION_80D_SENIOR_PARENTS = 50000;

    // NPS limit under 80CCD(1B)
    public static final double NPS_ADDITIONAL_LIMIT = 50000;

    // Home Loan Interest under 80EEA
    public static final double HOME_LOAN_INTEREST_LIMIT = 200000;

    /**
     * Calculate tax for both regimes and return comparison.
     */
    public TaxComparison calculateTax(TaxInput input) {
        TaxResult oldRegime = calculateOldRegime(input);
        TaxResult newRegime = calculateNewRegime(input);

        return new TaxComparison(oldRegime, newRegime);
    }

    /**
     * Calculate tax under Old Regime.
     */
    public TaxResult calculateOldRegime(TaxInput input) {
        double grossIncome = input.grossSalary + input.otherIncome;

        // Deductions
        List<Deduction> deductions = new ArrayList<>();

        // Standard Deduction
        deductions.add(new Deduction("Standard Deduction", OLD_REGIME_STANDARD_DEDUCTION));

        // Section 80C
        double total80C = Math.min(input.section80C, SECTION_80C_LIMIT);
        if (total80C > 0) {
            deductions.add(new Deduction("Section 80C (PF, PPF, ELSS, etc.)", total80C));
        }

        // Section 80D - Health Insurance
        double total80D = Math.min(input.healthInsuranceSelf, SECTION_80D_SELF) +
                Math.min(input.healthInsuranceParents,
                        input.parentsAreSenior ? SECTION_80D_SENIOR_PARENTS : SECTION_80D_PARENTS);
        if (total80D > 0) {
            deductions.add(new Deduction("Section 80D (Health Insurance)", total80D));
        }

        // NPS 80CCD(1B)
        double nps = Math.min(input.npsContribution, NPS_ADDITIONAL_LIMIT);
        if (nps > 0) {
            deductions.add(new Deduction("Section 80CCD(1B) - NPS", nps));
        }

        // Home Loan Interest
        double homeLoan = Math.min(input.homeLoanInterest, HOME_LOAN_INTEREST_LIMIT);
        if (homeLoan > 0) {
            deductions.add(new Deduction("Home Loan Interest (80EEA)", homeLoan));
        }

        // HRA Exemption
        if (input.hraReceived > 0 && input.rentPaid > 0) {
            double hraExemption = calculateHraExemption(input);
            if (hraExemption > 0) {
                deductions.add(new Deduction("HRA Exemption", hraExemption));
            }
        }

        // Calculate taxable income
        double totalDeductions = deductions.stream().mapToDouble(d -> d.amount).sum();
        double taxableIncome = Math.max(0, grossIncome - totalDeductions);

        // Calculate tax
        double tax = calculateTaxForSlabs(taxableIncome, OLD_REGIME_SLABS);

        // Rebate under 87A (up to 5L taxable income)
        double rebate = 0;
        if (taxableIncome <= 500000) {
            rebate = Math.min(tax, 12500);
        }

        // Health & Education Cess (4%)
        double cess = (tax - rebate) * 0.04;
        double totalTax = tax - rebate + cess;

        return new TaxResult("Old Regime", grossIncome, totalDeductions,
                taxableIncome, tax, rebate, cess, totalTax, deductions);
    }

    /**
     * Calculate tax under New Regime (Default from FY 2023-24).
     */
    public TaxResult calculateNewRegime(TaxInput input) {
        double grossIncome = input.grossSalary + input.otherIncome;

        List<Deduction> deductions = new ArrayList<>();

        // Only Standard Deduction allowed
        deductions.add(new Deduction("Standard Deduction", NEW_REGIME_STANDARD_DEDUCTION));

        // NPS employer contribution (14% for govt, 10% for others)
        if (input.employerNpsContribution > 0) {
            deductions.add(new Deduction("Employer NPS (80CCD(2))", input.employerNpsContribution));
        }

        double totalDeductions = deductions.stream().mapToDouble(d -> d.amount).sum();
        double taxableIncome = Math.max(0, grossIncome - totalDeductions);

        // Calculate tax
        double tax = calculateTaxForSlabs(taxableIncome, NEW_REGIME_SLABS);

        // Rebate under 87A (up to 7L taxable income in new regime)
        double rebate = 0;
        if (taxableIncome <= 700000) {
            rebate = Math.min(tax, 25000);
        }

        // Health & Education Cess (4%)
        double cess = (tax - rebate) * 0.04;
        double totalTax = tax - rebate + cess;

        return new TaxResult("New Regime", grossIncome, totalDeductions,
                taxableIncome, tax, rebate, cess, totalTax, deductions);
    }

    private double calculateTaxForSlabs(double taxableIncome, double[][] slabs) {
        double tax = 0;
        for (double[] slab : slabs) {
            double lower = slab[0];
            double upper = slab[1];
            double rate = slab[2];

            if (taxableIncome > lower) {
                double taxableInSlab = Math.min(taxableIncome, upper) - lower;
                tax += taxableInSlab * rate / 100;
            }
        }
        return tax;
    }

    private double calculateHraExemption(TaxInput input) {
        // HRA Exemption is minimum of:
        // 1. Actual HRA received
        // 2. 50% of salary (metro) or 40% (non-metro)
        // 3. Rent paid - 10% of salary

        double basicPlusDa = input.basicSalary + input.da;
        double option1 = input.hraReceived;
        double option2 = basicPlusDa * (input.isMetroCity ? 0.50 : 0.40);
        double option3 = Math.max(0, input.rentPaid - (basicPlusDa * 0.10));

        return Math.min(option1, Math.min(option2, option3));
    }

    /**
     * Calculate capital gains tax.
     */
    public CapitalGainsTax calculateCapitalGains(double shortTermGains, double longTermGains,
            String assetType) {
        double stcgTax = 0;
        double ltcgTax = 0;

        if ("EQUITY".equals(assetType) || "MUTUAL_FUND_EQUITY".equals(assetType)) {
            // STCG: 20% (changed from 15% in Budget 2024)
            stcgTax = shortTermGains * 0.20;
            // LTCG: 12.5% above 1.25L exemption (changed from 10% above 1L)
            double ltcgTaxable = Math.max(0, longTermGains - 125000);
            ltcgTax = ltcgTaxable * 0.125;
        } else {
            // Debt funds, gold, real estate - as per slab
            stcgTax = shortTermGains * 0.30; // Assuming 30% slab
            ltcgTax = longTermGains * 0.20; // 20% with indexation
        }

        return new CapitalGainsTax(shortTermGains, longTermGains, stcgTax, ltcgTax);
    }

    // Data classes
    public static class TaxInput {
        public double grossSalary;
        public double basicSalary;
        public double da;
        public double hraReceived;
        public double otherIncome;

        public double section80C; // PPF, ELSS, PF, LIC, etc.
        public double healthInsuranceSelf;
        public double healthInsuranceParents;
        public boolean parentsAreSenior;
        public double npsContribution;
        public double employerNpsContribution;
        public double homeLoanInterest;

        public double rentPaid;
        public boolean isMetroCity;
    }

    public static class TaxResult {
        public String regime;
        public double grossIncome;
        public double totalDeductions;
        public double taxableIncome;
        public double taxBeforeRebate;
        public double rebate;
        public double cess;
        public double totalTax;
        public List<Deduction> deductions;

        public TaxResult(String regime, double grossIncome, double totalDeductions,
                double taxableIncome, double taxBeforeRebate, double rebate,
                double cess, double totalTax, List<Deduction> deductions) {
            this.regime = regime;
            this.grossIncome = grossIncome;
            this.totalDeductions = totalDeductions;
            this.taxableIncome = taxableIncome;
            this.taxBeforeRebate = taxBeforeRebate;
            this.rebate = rebate;
            this.cess = cess;
            this.totalTax = totalTax;
            this.deductions = deductions;
        }
    }

    public static class Deduction {
        public String name;
        public double amount;

        public Deduction(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }
    }

    public static class TaxComparison {
        public TaxResult oldRegime;
        public TaxResult newRegime;
        public String recommendedRegime;
        public double savings;

        public TaxComparison(TaxResult oldRegime, TaxResult newRegime) {
            this.oldRegime = oldRegime;
            this.newRegime = newRegime;

            if (oldRegime.totalTax <= newRegime.totalTax) {
                this.recommendedRegime = "Old Regime";
                this.savings = newRegime.totalTax - oldRegime.totalTax;
            } else {
                this.recommendedRegime = "New Regime";
                this.savings = oldRegime.totalTax - newRegime.totalTax;
            }
        }
    }

    public static class CapitalGainsTax {
        public double shortTermGains;
        public double longTermGains;
        public double stcgTax;
        public double ltcgTax;
        public double totalTax;

        public CapitalGainsTax(double stGains, double ltGains, double stcgTax, double ltcgTax) {
            this.shortTermGains = stGains;
            this.longTermGains = ltGains;
            this.stcgTax = stcgTax;
            this.ltcgTax = ltcgTax;
            this.totalTax = stcgTax + ltcgTax;
        }
    }
}
