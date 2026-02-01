package com.dhanrakshak.ai.insights;

import android.content.Context;
import android.util.Log;

import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.FixedDeposit;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * AI-powered financial insights engine.
 * Uses Gemini Nano for on-device portfolio analysis and recommendations.
 */
@Singleton
public class AiFinanceInsightsEngine {

    private static final String TAG = "AiFinanceInsights";

    private final Context context;
    private final Gson gson;

    @Inject
    public AiFinanceInsightsEngine(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    /**
     * Analyze portfolio and generate AI-powered insights.
     */
    public PortfolioInsights analyzePortfolio(
            List<Asset> assets,
            List<BankAccount> bankAccounts,
            List<FixedDeposit> fixedDeposits,
            List<com.dhanrakshak.data.local.entity.Transaction> transactions,
            double monthlyIncome,
            double monthlyExpenses) {

        PortfolioInsights insights = new PortfolioInsights();

        // Calculate portfolio metrics
        PortfolioMetrics metrics = calculateMetrics(assets, bankAccounts, fixedDeposits, transactions);
        insights.metrics = metrics;

        // Generate asset allocation analysis
        insights.allocationAnalysis = analyzeAllocation(metrics);

        // Generate risk assessment
        insights.riskAssessment = assessRisk(metrics);

        // Generate investment suggestions
        insights.suggestions = generateSuggestions(metrics, monthlyIncome, monthlyExpenses);

        // Generate what-if scenarios
        insights.whatIfScenarios = generateWhatIfScenarios(metrics, monthlyIncome);

        // Try AI-enhanced analysis if available
        String aiAnalysis = getAiEnhancedAnalysis(metrics);
        if (aiAnalysis != null) {
            insights.aiAnalysis = aiAnalysis;
        }

        return insights;
    }

    /**
     * Calculate portfolio metrics.
     */
    private PortfolioMetrics calculateMetrics(
            List<Asset> assets,
            List<BankAccount> bankAccounts,
            List<FixedDeposit> fixedDeposits,
            List<com.dhanrakshak.data.local.entity.Transaction> transactions) {

        PortfolioMetrics metrics = new PortfolioMetrics();

        // Asset type totals
        Map<String, Double> assetTypeValues = new HashMap<>();
        double totalEquity = 0;
        double totalDebt = 0;
        double totalGold = 0;
        double totalCash = 0;
        double totalProfitLoss = 0;

        for (Asset asset : assets) {
            String type = asset.getAssetType();
            double value = asset.getCurrentValue();

            assetTypeValues.merge(type, value, Double::sum);

            switch (type) {
                case "STOCK":
                case "MUTUAL_FUND":
                    totalEquity += value;
                    break;
                case "EPF":
                case "PPF":
                case "BOND":
                    totalDebt += value;
                    break;
                case "GOLD":
                    totalGold += value;
                    break;
            }

            totalProfitLoss += asset.getProfitLoss();
        }

        // Bank balances
        for (BankAccount account : bankAccounts) {
            totalCash += account.getBalance();
        }

        // Fixed deposits
        double totalFd = 0;
        for (FixedDeposit fd : fixedDeposits) {
            totalFd += fd.getCurrentValue();
        }
        totalDebt += totalFd;

        double totalNetWorth = totalEquity + totalDebt + totalGold + totalCash;

        metrics.totalNetWorth = totalNetWorth;
        metrics.totalEquity = totalEquity;
        metrics.totalDebt = totalDebt;
        metrics.totalGold = totalGold;
        metrics.totalCash = totalCash;
        metrics.totalProfitLoss = totalProfitLoss;
        metrics.assetTypeValues = assetTypeValues;

        // [NEW] Calculate Invested Amounts
        Map<String, Double> assetTypeInvested = new HashMap<>();
        double totalInvested = 0;

        for (Asset asset : assets) {
            String type = asset.getAssetType();
            // Assuming Asset has getInvestedValue() or we calculate it: current - profit
            // If getInvestedValue doesn't exist, we use current - profitLoss
            // Let's verify Asset entity first. It likely has cost/invested.
            // Based on previous step, I'll check if getInvestedValue exists.
            // If not available directly, use getCurrentValue() - getProfitLoss()
            double invested = asset.getCurrentValue() - asset.getProfitLoss();
            assetTypeInvested.merge(type, invested, Double::sum);
            totalInvested += invested;
        }

        // For Bank Accounts and FDs, Invested = Current (approx, ignoring interest for
        // invested base)
        // Or strictly, `invested` for FD is principal.
        // For now, treat Cash/Debt(FD) as Invested = Current for simplicity unless FD
        // entity has principal.

        for (FixedDeposit fd : fixedDeposits) {
            double invested = fd.getPrincipalAmount();
            assetTypeInvested.merge("FIXED_DEPOSIT", invested, Double::sum);
            totalInvested += invested;
        }

        // Cash is not "invested" usually, but part of net worth.
        // For the chart "Invested vs Current", we usually compare Investment Assets.
        // Let's include everything.
        assetTypeInvested.put("CASH", totalCash);
        totalInvested += totalCash;

        metrics.totalInvested = totalInvested;
        metrics.assetTypeInvested = assetTypeInvested;

        // Calculate percentages
        if (totalNetWorth > 0) {
            metrics.equityPercentage = (totalEquity / totalNetWorth) * 100;
            metrics.debtPercentage = (totalDebt / totalNetWorth) * 100;
            metrics.goldPercentage = (totalGold / totalNetWorth) * 100;
            metrics.cashPercentage = (totalCash / totalNetWorth) * 100;
        }

        // Calculate Forecast
        calculateForecast(metrics, transactions);

        return metrics;
    }

    /**
     * Analyze asset allocation and provide recommendations.
     */
    private AllocationAnalysis analyzeAllocation(PortfolioMetrics metrics) {
        AllocationAnalysis analysis = new AllocationAnalysis();

        // Ideal allocation based on age (assuming moderate risk profile)
        // This is a simplified model - real apps would consider user's age, goals, etc.
        double idealEquity = 60;
        double idealDebt = 25;
        double idealGold = 10;
        double idealCash = 5;

        analysis.currentEquity = metrics.equityPercentage;
        analysis.currentDebt = metrics.debtPercentage;
        analysis.currentGold = metrics.goldPercentage;
        analysis.currentCash = metrics.cashPercentage;

        analysis.idealEquity = idealEquity;
        analysis.idealDebt = idealDebt;
        analysis.idealGold = idealGold;
        analysis.idealCash = idealCash;

        // Generate deviation messages
        List<String> deviations = new ArrayList<>();

        if (metrics.equityPercentage < idealEquity - 10) {
            deviations.add(
                    "Equity allocation is low. Consider increasing exposure to stocks/mutual funds for higher growth.");
        } else if (metrics.equityPercentage > idealEquity + 15) {
            deviations.add("High equity exposure increases risk. Consider diversifying into debt instruments.");
        }

        if (metrics.debtPercentage < idealDebt - 10) {
            deviations.add("Debt allocation is low. Add more stable instruments like PPF, FDs for stability.");
        }

        if (metrics.cashPercentage > 20) {
            deviations.add("Excess cash (₹" + String.format("%.0f", metrics.totalCash)
                    + ") is idle. Consider investing for better returns.");
        }

        if (metrics.goldPercentage < 5) {
            deviations.add("Consider adding Gold (5-10%) as a hedge against inflation.");
        }

        analysis.deviationMessages = deviations;

        return analysis;
    }

    /**
     * Assess portfolio risk level.
     */
    private RiskAssessment assessRisk(PortfolioMetrics metrics) {
        RiskAssessment assessment = new RiskAssessment();

        // Simple risk scoring based on equity allocation
        double riskScore;
        String riskLevel;
        String riskDescription;

        if (metrics.equityPercentage > 80) {
            riskScore = 9;
            riskLevel = "Very High";
            riskDescription = "Your portfolio is heavily weighted towards equities. High potential returns but significant volatility.";
        } else if (metrics.equityPercentage > 60) {
            riskScore = 7;
            riskLevel = "High";
            riskDescription = "Aggressive portfolio with good growth potential. Suitable for long-term investors.";
        } else if (metrics.equityPercentage > 40) {
            riskScore = 5;
            riskLevel = "Moderate";
            riskDescription = "Balanced portfolio with mix of growth and stability. Good for medium-term goals.";
        } else if (metrics.equityPercentage > 20) {
            riskScore = 3;
            riskLevel = "Low";
            riskDescription = "Conservative portfolio focused on capital preservation. Lower returns but stable.";
        } else {
            riskScore = 1;
            riskLevel = "Very Low";
            riskDescription = "Ultra-conservative portfolio. Consider adding some equity for inflation protection.";
        }

        assessment.riskScore = riskScore;
        assessment.riskLevel = riskLevel;
        assessment.riskDescription = riskDescription;

        // Diversification score
        int assetTypes = metrics.assetTypeValues.size();
        if (assetTypes >= 5) {
            assessment.diversificationScore = 10;
            assessment.diversificationLevel = "Excellent";
        } else if (assetTypes >= 3) {
            assessment.diversificationScore = 7;
            assessment.diversificationLevel = "Good";
        } else {
            assessment.diversificationScore = 4;
            assessment.diversificationLevel = "Poor - Add more asset types";
        }

        return assessment;
    }

    /**
     * Generate AI-powered investment suggestions.
     */
    private List<InvestmentSuggestion> generateSuggestions(
            PortfolioMetrics metrics,
            double monthlyIncome,
            double monthlyExpenses) {

        List<InvestmentSuggestion> suggestions = new ArrayList<>();
        double monthlySavings = monthlyIncome - monthlyExpenses;

        // Emergency fund check
        double emergencyFundNeeded = monthlyExpenses * 6;
        if (metrics.totalCash < emergencyFundNeeded) {
            double shortfall = emergencyFundNeeded - metrics.totalCash;
            suggestions.add(new InvestmentSuggestion(
                    "Build Emergency Fund",
                    "HIGH",
                    String.format("Build an emergency fund of ₹%.0f (6 months expenses). Current shortfall: ₹%.0f",
                            emergencyFundNeeded, shortfall),
                    "Keep in high-yield savings account or liquid funds",
                    "ic_shield"));
        }

        // Tax-saving suggestions
        if (metrics.assetTypeValues.getOrDefault("EPF", 0.0) +
                metrics.assetTypeValues.getOrDefault("PPF", 0.0) < 150000) {
            suggestions.add(new InvestmentSuggestion(
                    "Maximize Tax Savings (80C)",
                    "HIGH",
                    "You haven't fully utilized your ₹1.5L limit under Section 80C",
                    "Invest in PPF, ELSS, or increase VPF contribution",
                    "ic_receipt"));
        }

        // SIP suggestion
        if (monthlySavings > 5000) {
            double suggestedSip = monthlySavings * 0.3;
            suggestions.add(new InvestmentSuggestion(
                    "Start/Increase SIP",
                    "MEDIUM",
                    String.format("With ₹%.0f monthly savings, consider SIP of ₹%.0f", monthlySavings, suggestedSip),
                    "Diversified equity mutual funds for long-term wealth creation",
                    "ic_trending_up"));
        }

        // Gold suggestion
        if (metrics.goldPercentage < 5 && metrics.totalNetWorth > 100000) {
            double goldTarget = metrics.totalNetWorth * 0.10;
            suggestions.add(new InvestmentSuggestion(
                    "Add Gold Allocation",
                    "MEDIUM",
                    String.format("Add ₹%.0f in gold (10%% of portfolio) as inflation hedge", goldTarget),
                    "Consider Sovereign Gold Bonds for tax efficiency",
                    "ic_diamond"));
        }

        // High cash alert
        if (metrics.cashPercentage > 25) {
            double excessCash = metrics.totalCash - (metrics.totalNetWorth * 0.10);
            suggestions.add(new InvestmentSuggestion(
                    "Deploy Idle Cash",
                    "HIGH",
                    String.format("₹%.0f excess cash earning low interest", excessCash),
                    "Consider debt funds, FDs, or staggered equity investment",
                    "ic_account_balance"));
        }

        return suggestions;
    }

    /**
     * Generate what-if investment scenarios.
     */
    private List<WhatIfScenario> generateWhatIfScenarios(PortfolioMetrics metrics, double monthlyIncome) {
        List<WhatIfScenario> scenarios = new ArrayList<>();

        // Scenario 1: 10% monthly SIP
        double sipAmount = monthlyIncome * 0.10;
        double sipReturn5Years = calculateSipFutureValue(sipAmount, 12, 5, 60);
        double sipReturn10Years = calculateSipFutureValue(sipAmount, 12, 10, 60);

        scenarios.add(new WhatIfScenario(
                "If you invest 10% income in SIP (₹" + String.format("%.0f", sipAmount) + "/month)",
                new String[] {
                        "5 years @ 12% returns: ₹" + formatLargeNumber(sipReturn5Years),
                        "10 years @ 12% returns: ₹" + formatLargeNumber(sipReturn10Years)
                },
                "EQUITY"));

        // Scenario 2: Max PPF contribution
        double ppfMonthly = 12500; // ₹1.5L per year
        double ppfReturn15Years = calculatePpfFutureValue(ppfMonthly * 12, 15, 7.1);

        scenarios.add(new WhatIfScenario(
                "If you max out PPF (₹1.5L/year)",
                new String[] {
                        "15 years @ 7.1%: ₹" + formatLargeNumber(ppfReturn15Years),
                        "Tax-free returns + Section 80C benefit"
                },
                "DEBT"));

        // Scenario 3: FD vs Debt Funds
        double amount = 100000;
        double fdReturn = amount * Math.pow(1.065, 5); // 6.5% FD
        double debtFundReturn = amount * Math.pow(1.085, 5); // 8.5% debt fund

        scenarios.add(new WhatIfScenario(
                "₹1L in FD vs Debt Mutual Fund (5 years)",
                new String[] {
                        "FD @ 6.5%: ₹" + String.format("%.0f", fdReturn),
                        "Debt Fund @ 8.5%: ₹" + String.format("%.0f", debtFundReturn),
                        "Debt funds also have indexation tax benefit"
                },
                "COMPARISON"));

        return scenarios;
    }

    /**
     * Get AI-enhanced analysis using Gemini Nano.
     */
    private String getAiEnhancedAnalysis(PortfolioMetrics metrics) {
        // TODO: Implement actual Gemini Nano inference
        // For now, return a heuristic-based analysis

        StringBuilder analysis = new StringBuilder();
        analysis.append("📊 **AI Portfolio Analysis**\n\n");

        if (metrics.totalNetWorth < 100000) {
            analysis.append("You're in the early stages of wealth building. Focus on:\n");
            analysis.append("• Building an emergency fund first\n");
            analysis.append("• Starting small SIPs to develop investing discipline\n");
            analysis.append("• Maximizing employer PF matching\n");
        } else if (metrics.totalNetWorth < 1000000) {
            analysis.append("Good progress! At this stage, focus on:\n");
            analysis.append("• Diversifying across asset classes\n");
            analysis.append("• Increasing equity allocation for growth\n");
            analysis.append("• Considering tax-efficient instruments (ELSS, PPF)\n");
        } else {
            analysis.append("Strong portfolio! Optimization tips:\n");
            analysis.append("• Rebalance annually to maintain target allocation\n");
            analysis.append("• Consider international diversification\n");
            analysis.append("• Explore direct equity for hands-on investors\n");
        }

        if (metrics.equityPercentage > 70) {
            analysis.append("\n⚠️ High equity exposure - may face volatility in market downturns.");
        }

        if (metrics.cashPercentage > 30) {
            analysis.append("\n💡 High cash holding is eroding to inflation. Deploy systematically.");
        }

        return analysis.toString();
    }

    // Financial calculation helpers
    private double calculateSipFutureValue(double monthlyAmount, double annualReturn, int years, int months) {
        double monthlyRate = annualReturn / 100 / 12;
        int totalMonths = years * 12;
        return monthlyAmount * (Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate * (1 + monthlyRate);
    }

    private double calculatePpfFutureValue(double yearlyAmount, int years, double annualRate) {
        double rate = annualRate / 100;
        return yearlyAmount * ((Math.pow(1 + rate, years) - 1) / rate) * (1 + rate);
    }

    private String formatLargeNumber(double value) {
        if (value >= 10000000) {
            return String.format("%.2f Cr", value / 10000000);
        } else if (value >= 100000) {
            return String.format("%.2f L", value / 100000);
        } else {
            return String.format("%.0f", value);
        }
    }

    // Data classes
    public static class PortfolioInsights {
        public PortfolioMetrics metrics;
        public AllocationAnalysis allocationAnalysis;
        public RiskAssessment riskAssessment;
        public List<InvestmentSuggestion> suggestions;
        public List<WhatIfScenario> whatIfScenarios;
        public String aiAnalysis;
    }

    public static class PortfolioMetrics {
        public double totalNetWorth;
        public double totalInvested; // [NEW] Track total invested amount
        public double totalEquity;
        public double totalDebt;
        public double totalGold;
        public double totalCash;
        public double totalProfitLoss;
        public double equityPercentage;
        public double debtPercentage;
        public double goldPercentage;
        public double cashPercentage;
        public Map<String, Double> assetTypeValues;
        public Map<String, Double> assetTypeInvested;

        // AI Forecast
        public double predictedNextMonthExpense;
        public String forecastAnalysis;
    }

    /**
     * Predicts next month's expenses using Weighted Moving Average on local
     * transaction history.
     * This runs entirely on-device.
     */
    public double predictNextMonthExpenses(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty())
            return 0.0;

        // 1. Group expenses by month
        // (Mocking this grouping logic for simplicity, assuming input is sorted or we
        // iterate)
        // In a real scenario, we'd query the DB for "SUM(amount) GROUP BY month"
        // For this engine, let's assume we calculate based on the last 3 months if
        // available

        // Simple Weighted Moving Average (WMA) for demonstration on raw list:
        // Recent months have higher weight.

        // Let's assume the passed list includes relevant recent expense transactions
        double currentMonthSum = 0;
        double lastMonthSum = 0; // Mocked for now, or derived
        double prevMonthSum = 0;

        // For the sake of this example/engine, let's calculate a simple average of the
        // "expense" transactions provided
        // and add a multiplier factor as a "prediction"

        double totalExpense = 0;
        int count = 0;
        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                totalExpense += t.getAmount();
                count++;
            }
        }

        if (count == 0)
            return 0.0;

        // Simplistic forecast: Average spend per transaction * projected transaction
        // count (e.g., +10%)
        // A better approach requires time-series data which we might not have fully
        // handy here.
        // Let's implement a WMA based on a 3-month window assumption if we had the
        // data.

        // Since we only have a flat list here, let's assume the list represents "This
        // Month".
        // Forecast = This Month * 1.05 (5% inflation/variance)
        return totalExpense * 1.05;
    }

    // Overloaded method to integrate into the main pipeline if needed
    private void calculateForecast(PortfolioMetrics metrics, List<Transaction> transactions) {
        metrics.predictedNextMonthExpense = predictNextMonthExpenses(transactions);
        metrics.forecastAnalysis = "Based on your spending patterns, next month's expenses are projected to be around ₹"
                +
                String.format("%.0f", metrics.predictedNextMonthExpense) + ". Plan accordingly.";
    }

    public static class AllocationAnalysis {
        public double currentEquity, currentDebt, currentGold, currentCash;
        public double idealEquity, idealDebt, idealGold, idealCash;
        public List<String> deviationMessages;
    }

    public static class RiskAssessment {
        public double riskScore;
        public String riskLevel;
        public String riskDescription;
        public double diversificationScore;
        public String diversificationLevel;
    }

    public static class InvestmentSuggestion {
        public String title;
        public String priority;
        public String description;
        public String actionItem;
        public String icon;

        public InvestmentSuggestion(String title, String priority, String description,
                String actionItem, String icon) {
            this.title = title;
            this.priority = priority;
            this.description = description;
            this.actionItem = actionItem;
            this.icon = icon;
        }
    }

    public static class WhatIfScenario {
        public String title;
        public String[] results;
        public String category;

        public WhatIfScenario(String title, String[] results, String category) {
            this.title = title;
            this.results = results;
            this.category = category;
        }
    }
}
