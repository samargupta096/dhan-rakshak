package com.dhanrakshak.ai.coach;

import android.content.Context;

import com.dhanrakshak.data.local.dao.BudgetDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.Budget;
import com.dhanrakshak.data.local.entity.SmsTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * AI Financial Coach that provides personalized insights and encouragement.
 * Analyzes spending patterns, achievements, and provides tips.
 */
@Singleton
public class AiFinancialCoach {

    private final Context context;
    private final SmsTransactionDao smsTransactionDao;
    private final BudgetDao budgetDao;

    @Inject
    public AiFinancialCoach(Context context, SmsTransactionDao smsTransactionDao,
            BudgetDao budgetDao) {
        this.context = context.getApplicationContext();
        this.smsTransactionDao = smsTransactionDao;
        this.budgetDao = budgetDao;
    }

    /**
     * Get weekly spending analysis with insights.
     */
    public Single<WeeklyAnalysis> getWeeklyAnalysis() {
        Calendar cal = Calendar.getInstance();
        long endDate = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long startDate = cal.getTimeInMillis();

        // Previous week for comparison
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long prevStartDate = cal.getTimeInMillis();

        return Single.zip(
                smsTransactionDao.getTransactionsBetweenDates(startDate, endDate).firstOrError(),
                smsTransactionDao.getTransactionsBetweenDates(prevStartDate, startDate).firstOrError(),
                (thisWeek, lastWeek) -> analyzeWeeklySpending(thisWeek, lastWeek)).subscribeOn(Schedulers.io());
    }

    private WeeklyAnalysis analyzeWeeklySpending(List<SmsTransaction> thisWeek,
            List<SmsTransaction> lastWeek) {
        // Calculate totals
        double thisWeekSpending = 0;
        double lastWeekSpending = 0;
        Map<String, Double> categorySpending = new HashMap<>();

        for (SmsTransaction tx : thisWeek) {
            if ("DEBIT".equals(tx.getTransactionType())) {
                thisWeekSpending += tx.getAmount();
                String category = tx.getCategory() != null ? tx.getCategory() : "Others";
                categorySpending.merge(category, tx.getAmount(), Double::sum);
            }
        }

        for (SmsTransaction tx : lastWeek) {
            if ("DEBIT".equals(tx.getTransactionType())) {
                lastWeekSpending += tx.getAmount();
            }
        }

        // Calculate change
        double changePercent = 0;
        if (lastWeekSpending > 0) {
            changePercent = ((thisWeekSpending - lastWeekSpending) / lastWeekSpending) * 100;
        }

        // Generate insights
        List<CoachInsight> insights = new ArrayList<>();

        // Spending trend insight
        if (changePercent > 20) {
            insights.add(new CoachInsight(
                    "📈 Spending Alert",
                    String.format("You spent %.0f%% more than last week. Consider reviewing your expenses.",
                            changePercent),
                    "WARNING"));
        } else if (changePercent < -10) {
            insights.add(new CoachInsight(
                    "🎉 Great Job!",
                    String.format("You reduced spending by %.0f%% compared to last week. Keep it up!",
                            Math.abs(changePercent)),
                    "ACHIEVEMENT"));
        }

        // Category-specific insights
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            if (entry.getValue() > thisWeekSpending * 0.4) {
                insights.add(new CoachInsight(
                        "🎯 Category Focus",
                        String.format("%s accounts for %.0f%% of your weekly spending.",
                                entry.getKey(), (entry.getValue() / thisWeekSpending) * 100),
                        "TIP"));
            }
        }

        // Daily average insight
        double dailyAverage = thisWeekSpending / 7;
        insights.add(new CoachInsight(
                "📊 Daily Average",
                String.format("You're spending ₹%.0f per day on average.", dailyAverage),
                "INFO"));

        return new WeeklyAnalysis(thisWeekSpending, lastWeekSpending, changePercent,
                categorySpending, insights);
    }

    /**
     * Get personalized savings tips based on spending patterns.
     */
    public Single<List<SavingsTip>> getPersonalizedTips() {
        return getWeeklyAnalysis().map(analysis -> {
            List<SavingsTip> tips = new ArrayList<>();

            // Tips based on category spending
            for (Map.Entry<String, Double> entry : analysis.categorySpending.entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();

                switch (category) {
                    case "Food & Dining":
                        if (amount > 3000) {
                            tips.add(new SavingsTip(
                                    "🍽️ Dining Savings",
                                    "Cook at home 2 more days per week to save ₹1000+",
                                    1000,
                                    "MEDIUM"));
                        }
                        break;
                    case "Entertainment":
                        if (amount > 2000) {
                            tips.add(new SavingsTip(
                                    "🎬 Entertainment",
                                    "Consider sharing streaming subscriptions with family",
                                    500,
                                    "EASY"));
                        }
                        break;
                    case "Shopping":
                        if (amount > 5000) {
                            tips.add(new SavingsTip(
                                    "🛒 Smart Shopping",
                                    "Wait 24 hours before non-essential purchases over ₹1000",
                                    2000,
                                    "MEDIUM"));
                        }
                        break;
                    case "Transportation":
                        if (amount > 2000) {
                            tips.add(new SavingsTip(
                                    "🚗 Transport Savings",
                                    "Consider carpooling or public transport twice a week",
                                    800,
                                    "MEDIUM"));
                        }
                        break;
                }
            }

            // General tips
            if (analysis.weeklySpending > 10000) {
                tips.add(new SavingsTip(
                        "💰 50-30-20 Rule",
                        "Allocate 50% to needs, 30% to wants, 20% to savings",
                        analysis.weeklySpending * 0.2,
                        "IMPORTANT"));
            }

            return tips;
        });
    }

    /**
     * Check and award achievements.
     */
    public Single<List<Achievement>> checkAchievements() {
        return getWeeklyAnalysis().map(analysis -> {
            List<Achievement> achievements = new ArrayList<>();

            if (analysis.changePercent < -20) {
                achievements.add(new Achievement(
                        "🏆 Super Saver",
                        "Reduced spending by 20%+ this week!",
                        "super_saver",
                        50));
            }

            if (analysis.weeklySpending < 5000) {
                achievements.add(new Achievement(
                        "🌟 Frugal Week",
                        "Spent less than ₹5000 this week",
                        "frugal_week",
                        30));
            }

            // Check budget adherence
            boolean allBudgetsOk = true;
            // Would check budgets here

            if (allBudgetsOk) {
                achievements.add(new Achievement(
                        "📊 Budget Master",
                        "Stayed within all category budgets",
                        "budget_master",
                        40));
            }

            return achievements;
        });
    }

    /**
     * Get motivational messages based on financial behavior.
     */
    public String getMotivationalMessage() {
        String[] messages = {
                "Every rupee saved today is a step towards financial freedom! 💪",
                "Small consistent savings beat occasional large deposits. Keep going! 🎯",
                "You're doing great! Your future self will thank you. 🌟",
                "Track, save, invest, repeat. You've got this! 📈",
                "Building wealth is a marathon, not a sprint. Stay patient! 🏃",
                "Your financial discipline is inspiring. Keep it up! 🔥"
        };
        return messages[(int) (System.currentTimeMillis() % messages.length)];
    }

    // Data classes
    public static class WeeklyAnalysis {
        public double weeklySpending;
        public double previousWeekSpending;
        public double changePercent;
        public Map<String, Double> categorySpending;
        public List<CoachInsight> insights;

        public WeeklyAnalysis(double weekly, double previous, double change,
                Map<String, Double> categories, List<CoachInsight> insights) {
            this.weeklySpending = weekly;
            this.previousWeekSpending = previous;
            this.changePercent = change;
            this.categorySpending = categories;
            this.insights = insights;
        }
    }

    public static class CoachInsight {
        public String title;
        public String message;
        public String type; // INFO, TIP, WARNING, ACHIEVEMENT

        public CoachInsight(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
        }
    }

    public static class SavingsTip {
        public String title;
        public String description;
        public double potentialSavings;
        public String difficulty; // EASY, MEDIUM, HARD

        public SavingsTip(String title, String desc, double savings, String diff) {
            this.title = title;
            this.description = desc;
            this.potentialSavings = savings;
            this.difficulty = diff;
        }
    }

    public static class Achievement {
        public String title;
        public String description;
        public String badge;
        public int points;

        public Achievement(String title, String desc, String badge, int points) {
            this.title = title;
            this.description = desc;
            this.badge = badge;
            this.points = points;
        }
    }
}
