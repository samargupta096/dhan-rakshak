package com.dhanrakshak.domain.analytics;

import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Investment Analytics Engine.
 * Provides XIRR, benchmarking, sector analysis, and performance metrics.
 */
public class InvestmentAnalyticsEngine {

    /**
     * Calculate XIRR (Extended Internal Rate of Return) for cash flows.
     * Uses Newton-Raphson method.
     */
    public double calculateXirr(List<CashFlow> cashFlows) {
        if (cashFlows == null || cashFlows.size() < 2)
            return 0;

        // Sort by date
        Collections.sort(cashFlows, (a, b) -> Long.compare(a.date, b.date));

        // Convert dates to years from first date
        long firstDate = cashFlows.get(0).date;
        double[] years = new double[cashFlows.size()];
        double[] amounts = new double[cashFlows.size()];

        for (int i = 0; i < cashFlows.size(); i++) {
            years[i] = (cashFlows.get(i).date - firstDate) / (365.25 * 24 * 60 * 60 * 1000);
            amounts[i] = cashFlows.get(i).amount;
        }

        // Newton-Raphson iteration
        double rate = 0.1; // Initial guess
        for (int iteration = 0; iteration < 100; iteration++) {
            double f = 0;
            double df = 0;

            for (int i = 0; i < amounts.length; i++) {
                double factor = Math.pow(1 + rate, years[i]);
                f += amounts[i] / factor;
                df -= years[i] * amounts[i] / (factor * (1 + rate));
            }

            if (Math.abs(df) < 1e-10)
                break;

            double newRate = rate - f / df;
            if (Math.abs(newRate - rate) < 1e-7) {
                return newRate * 100; // Convert to percentage
            }
            rate = newRate;
        }

        return rate * 100;
    }

    /**
     * Calculate CAGR (Compound Annual Growth Rate).
     */
    public double calculateCagr(double beginningValue, double endingValue, double years) {
        if (beginningValue <= 0 || years <= 0)
            return 0;
        return (Math.pow(endingValue / beginningValue, 1.0 / years) - 1) * 100;
    }

    /**
     * Compare portfolio performance with benchmark.
     */
    public BenchmarkComparison compareToBenchmark(List<Asset> assets,
            double benchmarkReturn) {
        double portfolioValue = 0;
        double portfolioInvested = 0;

        for (Asset asset : assets) {
            portfolioValue += asset.getCurrentValue();
            portfolioInvested += asset.getInvestedAmount();
        }

        double portfolioReturn = 0;
        if (portfolioInvested > 0) {
            portfolioReturn = ((portfolioValue - portfolioInvested) / portfolioInvested) * 100;
        }

        double alpha = portfolioReturn - benchmarkReturn;
        boolean outperformed = alpha > 0;

        return new BenchmarkComparison(portfolioReturn, benchmarkReturn, alpha, outperformed);
    }

    /**
     * Analyze sector-wise diversification.
     */
    public SectorAnalysis analyzeSectorDiversification(List<Asset> assets) {
        Map<String, Double> sectorAllocation = new HashMap<>();
        double totalValue = 0;

        for (Asset asset : assets) {
            String sector = getSectorForAsset(asset);
            double value = asset.getCurrentValue();
            sectorAllocation.merge(sector, value, Double::sum);
            totalValue += value;
        }

        // Convert to percentages
        Map<String, Double> sectorPercentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : sectorAllocation.entrySet()) {
            sectorPercentages.put(entry.getKey(), (entry.getValue() / totalValue) * 100);
        }

        // Calculate diversification score (0-100)
        // Higher is better, based on entropy
        double diversificationScore = calculateDiversificationScore(sectorPercentages);

        // Identify concentration risks
        List<String> concentrationWarnings = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sectorPercentages.entrySet()) {
            if (entry.getValue() > 30) {
                concentrationWarnings.add("High exposure to " + entry.getKey() +
                        " (" + String.format("%.1f%%", entry.getValue()) + ")");
            }
        }

        return new SectorAnalysis(sectorPercentages, diversificationScore, concentrationWarnings);
    }

    /**
     * Track dividends received.
     */
    public DividendSummary calculateDividendSummary(List<Transaction> transactions) {
        double totalDividends = 0;
        int dividendCount = 0;
        Map<String, Double> dividendsByAsset = new HashMap<>();

        for (Transaction tx : transactions) {
            if ("DIVIDEND".equals(tx.getType())) {
                totalDividends += tx.getAmount();
                dividendCount++;
                // dividendsByAsset.merge(tx.getAssetName(), tx.getAmount(), Double::sum);
            }
        }

        return new DividendSummary(totalDividends, dividendCount, dividendsByAsset);
    }

    /**
     * Calculate portfolio volatility (standard deviation of returns).
     */
    public double calculateVolatility(List<Double> monthlyReturns) {
        if (monthlyReturns == null || monthlyReturns.size() < 2)
            return 0;

        double mean = monthlyReturns.stream().mapToDouble(d -> d).average().orElse(0);
        double variance = monthlyReturns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average().orElse(0);

        // Annualize: multiply by sqrt(12) for monthly data
        return Math.sqrt(variance) * Math.sqrt(12);
    }

    /**
     * Calculate Sharpe Ratio.
     * (Portfolio Return - Risk-Free Rate) / Portfolio Volatility
     */
    public double calculateSharpeRatio(double portfolioReturn, double riskFreeRate,
            double volatility) {
        if (volatility == 0)
            return 0;
        return (portfolioReturn - riskFreeRate) / volatility;
    }

    /**
     * Get risk-adjusted return metrics.
     */
    public RiskMetrics calculateRiskMetrics(List<Asset> assets, List<Double> monthlyReturns,
            double riskFreeRate) {
        double portfolioReturn = 0;
        double invested = 0;
        double current = 0;

        for (Asset asset : assets) {
            invested += asset.getInvestedAmount();
            current += asset.getCurrentValue();
        }

        if (invested > 0) {
            portfolioReturn = ((current - invested) / invested) * 100;
        }

        double volatility = calculateVolatility(monthlyReturns);
        double sharpeRatio = calculateSharpeRatio(portfolioReturn, riskFreeRate, volatility);

        // Calculate max drawdown
        double maxDrawdown = calculateMaxDrawdown(monthlyReturns);

        return new RiskMetrics(portfolioReturn, volatility, sharpeRatio, maxDrawdown);
    }

    private double calculateMaxDrawdown(List<Double> monthlyReturns) {
        if (monthlyReturns == null || monthlyReturns.isEmpty())
            return 0;

        double peak = 100; // Start with base 100
        double maxDrawdown = 0;
        double current = 100;

        for (double ret : monthlyReturns) {
            current = current * (1 + ret / 100);
            peak = Math.max(peak, current);
            double drawdown = (peak - current) / peak * 100;
            maxDrawdown = Math.max(maxDrawdown, drawdown);
        }

        return maxDrawdown;
    }

    private String getSectorForAsset(Asset asset) {
        String type = asset.getAssetType();
        if ("STOCK".equals(type)) {
            // In real app, would look up sector from stock data
            return "Equity";
        } else if ("MUTUAL_FUND".equals(type)) {
            String name = asset.getName().toLowerCase();
            if (name.contains("debt") || name.contains("bond"))
                return "Debt";
            if (name.contains("gold"))
                return "Gold";
            if (name.contains("liquid"))
                return "Liquid";
            return "Equity";
        } else if ("GOLD".equals(type)) {
            return "Gold";
        } else if ("PPF".equals(type) || "EPF".equals(type) || "FIXED_DEPOSIT".equals(type)) {
            return "Debt";
        }
        return "Other";
    }

    private double calculateDiversificationScore(Map<String, Double> sectorPercentages) {
        if (sectorPercentages.isEmpty())
            return 0;

        // Use entropy-based score
        double entropy = 0;
        for (double pct : sectorPercentages.values()) {
            if (pct > 0) {
                double p = pct / 100;
                entropy -= p * Math.log(p);
            }
        }

        // Normalize: max entropy for n sectors is ln(n)
        double maxEntropy = Math.log(sectorPercentages.size());
        if (maxEntropy == 0)
            return 0;

        return (entropy / maxEntropy) * 100;
    }

    // Data classes
    public static class CashFlow {
        public long date;
        public double amount; // Negative for investment, positive for redemption

        public CashFlow(long date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }

    public static class BenchmarkComparison {
        public double portfolioReturn;
        public double benchmarkReturn;
        public double alpha;
        public boolean outperformed;

        public BenchmarkComparison(double portfolio, double benchmark, double alpha, boolean out) {
            this.portfolioReturn = portfolio;
            this.benchmarkReturn = benchmark;
            this.alpha = alpha;
            this.outperformed = out;
        }
    }

    public static class SectorAnalysis {
        public Map<String, Double> sectorPercentages;
        public double diversificationScore;
        public List<String> warnings;

        public SectorAnalysis(Map<String, Double> sectors, double score, List<String> warnings) {
            this.sectorPercentages = sectors;
            this.diversificationScore = score;
            this.warnings = warnings;
        }
    }

    public static class DividendSummary {
        public double totalDividends;
        public int dividendCount;
        public Map<String, Double> dividendsByAsset;

        public DividendSummary(double total, int count, Map<String, Double> byAsset) {
            this.totalDividends = total;
            this.dividendCount = count;
            this.dividendsByAsset = byAsset;
        }
    }

    public static class RiskMetrics {
        public double portfolioReturn;
        public double volatility;
        public double sharpeRatio;
        public double maxDrawdown;

        public RiskMetrics(double ret, double vol, double sharpe, double dd) {
            this.portfolioReturn = ret;
            this.volatility = vol;
            this.sharpeRatio = sharpe;
            this.maxDrawdown = dd;
        }
    }
}
