package com.dhanrakshak.ai.categorization;

import android.content.Context;
import android.util.Log;

import com.dhanrakshak.data.local.dao.ExpenseCategoryDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.ExpenseCategory;
import com.dhanrakshak.data.local.entity.SmsTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * AI-powered expense categorization engine.
 * Uses keyword matching + machine learning patterns to auto-categorize
 * transactions.
 */
@Singleton
public class ExpenseCategorizationEngine {

    private static final String TAG = "ExpenseCategorizationEngine";

    private final Context context;
    private final SmsTransactionDao smsTransactionDao;
    private final ExpenseCategoryDao categoryDao;

    // Keyword patterns for each category
    private final Map<String, Pattern[]> categoryPatterns = new HashMap<>();

    // Merchant to category learned mappings
    private final Map<String, Long> merchantCategoryCache = new HashMap<>();

    @Inject
    public ExpenseCategorizationEngine(Context context,
            SmsTransactionDao smsTransactionDao,
            ExpenseCategoryDao categoryDao) {
        this.context = context.getApplicationContext();
        this.smsTransactionDao = smsTransactionDao;
        this.categoryDao = categoryDao;

        initializePatterns();
    }

    private void initializePatterns() {
        // Food & Dining
        categoryPatterns.put("Food & Dining", new Pattern[] {
                Pattern.compile(
                        "(?i)(swiggy|zomato|uber\\s*eats|dominos|pizza|mcdonalds|kfc|burger|restaurant|cafe|hotel|food|meal|lunch|dinner|breakfast)"),
                Pattern.compile("(?i)(barbeque|nation|haldiram|chai|coffee|starbucks|costa)")
        });

        // Shopping
        categoryPatterns.put("Shopping", new Pattern[] {
                Pattern.compile("(?i)(amazon|flipkart|myntra|ajio|nykaa|meesho|snapdeal|shopclues)"),
                Pattern.compile(
                        "(?i)(mall|mart|store|retail|lifestyle|big\\s*bazaar|dmart|reliance|croma|vijay\\s*sales)")
        });

        // Transportation
        categoryPatterns.put("Transportation", new Pattern[] {
                Pattern.compile("(?i)(uber|ola|rapido|meru|auto|taxi|cab|metro|irctc|railway|train|bus|redbus)"),
                Pattern.compile("(?i)(petrol|diesel|fuel|hp|iocl|bpcl|shell|parking)")
        });

        // Utilities
        categoryPatterns.put("Utilities", new Pattern[] {
                Pattern.compile("(?i)(electricity|water|gas|piped|power|bescom|msedcl|pgvcl|uppcl)"),
                Pattern.compile("(?i)(bill\\s*payment|utility|jio|airtel|vodafone|bsnl|vi|recharge|mobile\\s*bill)")
        });

        // Health
        categoryPatterns.put("Health", new Pattern[] {
                Pattern.compile("(?i)(hospital|clinic|doctor|medical|pharmacy|apollo|medplus|netmeds|1mg|pharmeasy)"),
                Pattern.compile("(?i)(gym|fitness|cult|healthify|yoga|dental|diagnostic|lab)")
        });

        // Entertainment
        categoryPatterns.put("Entertainment", new Pattern[] {
                Pattern.compile("(?i)(netflix|prime|hotstar|disney|spotify|gaana|youtube|pvr|inox|movie|cinema)"),
                Pattern.compile("(?i)(bookmyshow|gaming|dream11|mpl|book|subscription)")
        });

        // Investment
        categoryPatterns.put("Investment", new Pattern[] {
                Pattern.compile("(?i)(sip|mutual\\s*fund|zerodha|groww|upstox|kite|angel|mf|nse|bse)"),
                Pattern.compile("(?i)(investment|trading|stock|share|demat|ipo|lumpsum)")
        });

        // Insurance
        categoryPatterns.put("Insurance", new Pattern[] {
                Pattern.compile("(?i)(insurance|lic|hdfc\\s*life|icici\\s*pru|sbi\\s*life|premium|policy)"),
                Pattern.compile("(?i)(health\\s*insurance|term|car\\s*insurance|motor)")
        });

        // EMI & Loans
        categoryPatterns.put("EMI & Loans", new Pattern[] {
                Pattern.compile("(?i)(emi|loan|bajaj\\s*finserv|capital\\s*first|nbfc|credit|repayment)"),
                Pattern.compile("(?i)(personal\\s*loan|home\\s*loan|car\\s*loan|education\\s*loan)")
        });

        // Education
        categoryPatterns.put("Education", new Pattern[] {
                Pattern.compile("(?i)(school|college|university|tuition|course|udemy|coursera|unacademy|byjus)"),
                Pattern.compile("(?i)(education|books|exam|coaching|institute|academy)")
        });

        // Rent
        categoryPatterns.put("Rent", new Pattern[] {
                Pattern.compile("(?i)(rent|landlord|house\\s*rent|flat\\s*rent|pg|hostel|accommodation)")
        });

        // Transfer
        categoryPatterns.put("Transfer", new Pattern[] {
                Pattern.compile("(?i)(transfer|imps|neft|rtgs|upi|self\\s*transfer|fund\\s*transfer)")
        });
    }

    /**
     * Categorize a transaction based on merchant name and description.
     */
    public Single<String> categorizeTransaction(String merchant, String description) {
        return Single.fromCallable(() -> {
            String textToAnalyze = (merchant + " " + description).toLowerCase();

            // First check cache for known merchant
            if (merchantCategoryCache.containsKey(merchant.toLowerCase())) {
                ExpenseCategory category = categoryDao.getById(
                        merchantCategoryCache.get(merchant.toLowerCase())).blockingGet();
                if (category != null) {
                    return category.getName();
                }
            }

            // Pattern matching
            for (Map.Entry<String, Pattern[]> entry : categoryPatterns.entrySet()) {
                for (Pattern pattern : entry.getValue()) {
                    if (pattern.matcher(textToAnalyze).find()) {
                        return entry.getKey();
                    }
                }
            }

            return "Others";
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Batch categorize all uncategorized transactions.
     */
    public Completable categorizeAllUncategorized() {
        return smsTransactionDao.getUncategorizedTransactions()
                .firstOrError()
                .flatMapCompletable(transactions -> Completable.fromAction(() -> {
                    for (SmsTransaction tx : transactions) {
                        String category = categorizeTransaction(
                                tx.getMerchant() != null ? tx.getMerchant() : "",
                                "").blockingGet();

                        // Get category ID
                        ExpenseCategory cat = categoryDao.getByName(category).blockingGet();
                        if (cat != null) {
                            tx.setCategoryId(cat.getId());
                            smsTransactionDao.update(tx).blockingAwait();
                        }
                    }
                })).subscribeOn(Schedulers.io());
    }

    /**
     * Learn from user's manual categorization.
     */
    public Completable learnFromUserChoice(String merchant, long categoryId) {
        return Completable.fromAction(() -> {
            merchantCategoryCache.put(merchant.toLowerCase(), categoryId);
            Log.d(TAG, "Learned: " + merchant + " -> category " + categoryId);
        });
    }

    /**
     * Get spending summary by category.
     */
    public Single<Map<String, Double>> getSpendingByCategory(long startDate, long endDate) {
        return smsTransactionDao.getCategoryWiseSpending(startDate, endDate)
                .firstOrError()
                .map(spendingList -> {
                    Map<String, Double> result = new HashMap<>();
                    for (SmsTransactionDao.CategorySpending spending : spendingList) {
                        result.put(spending.categoryName, spending.totalAmount);
                    }
                    return result;
                });
    }
}
