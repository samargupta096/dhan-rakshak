package com.dhanrakshak.domain.budget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.dao.BudgetDao;
import com.dhanrakshak.data.local.dao.ExpenseCategoryDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.Budget;
import com.dhanrakshak.data.local.entity.ExpenseCategory;
import com.dhanrakshak.presentation.MainActivity;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Budget Manager for planning, tracking, and alerting.
 */
@Singleton
public class BudgetManager {

    private static final String TAG = "BudgetManager";
    private static final String CHANNEL_ID = "budget_alerts";
    private static final int NOTIFICATION_ID_BASE = 2000;

    private final Context context;
    private final BudgetDao budgetDao;
    private final ExpenseCategoryDao categoryDao;
    private final SmsTransactionDao smsTransactionDao;

    @Inject
    public BudgetManager(Context context, BudgetDao budgetDao,
            ExpenseCategoryDao categoryDao,
            SmsTransactionDao smsTransactionDao) {
        this.context = context.getApplicationContext();
        this.budgetDao = budgetDao;
        this.categoryDao = categoryDao;
        this.smsTransactionDao = smsTransactionDao;

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alerts when spending exceeds budget limits");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create a new budget for a category.
     */
    public Completable createBudget(long categoryId, double amount, int month, int year) {
        Budget budget = new Budget(categoryId, amount, month, year);
        return budgetDao.insert(budget);
    }

    /**
     * Get all budgets for current month.
     */
    public Flowable<List<Budget>> getCurrentMonthBudgets() {
        Calendar cal = Calendar.getInstance();
        return budgetDao.getBudgetsForMonth(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    /**
     * Get budget summary for current month.
     */
    public Single<BudgetSummary> getCurrentMonthSummary() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        return Single.zip(
                budgetDao.getTotalBudgetForMonth(month, year).onErrorReturnItem(0.0),
                budgetDao.getTotalSpentForMonth(month, year).onErrorReturnItem(0.0),
                (totalBudget, totalSpent) -> new BudgetSummary(totalBudget, totalSpent));
    }

    /**
     * Update spent amount when a transaction is added.
     */
    public Completable updateSpendingForCategory(long categoryId, double amount) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        return budgetDao.getBudgetForCategory(categoryId, month, year)
                .flatMapCompletable(budget -> {
                    double newSpent = budget.getSpentAmount() + amount;
                    budget.setSpentAmount(newSpent);
                    budget.setUpdatedAt(System.currentTimeMillis());

                    return budgetDao.update(budget)
                            .andThen(Completable.fromAction(() -> {
                                if (budget.shouldAlert()) {
                                    sendBudgetAlert(budget, categoryId);
                                }
                            }));
                })
                .onErrorComplete(); // Ignore if no budget exists
    }

    /**
     * Sync budgets with actual spending from transactions.
     */
    public Completable syncBudgetsWithTransactions() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        // Get start and end of month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        long startOfMonth = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        long endOfMonth = cal.getTimeInMillis();

        return budgetDao.getBudgetsForMonth(month, year)
                .firstOrError()
                .flatMapCompletable(budgets -> Completable.merge(
                        io.reactivex.rxjava3.core.Flowable.fromIterable(budgets)
                                .map(budget -> smsTransactionDao.getTotalForCategoryBetweenDates(
                                        budget.getCategoryId(), startOfMonth, endOfMonth)
                                        .flatMapCompletable(spent -> {
                                            budget.setSpentAmount(spent != null ? spent : 0);
                                            return budgetDao.update(budget);
                                        })
                                        .onErrorComplete())))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Copy budgets to new month.
     */
    public Completable copyBudgetsToNextMonth() {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentYear = cal.get(Calendar.YEAR);

        int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        int nextYear = currentMonth == 12 ? currentYear + 1 : currentYear;

        return budgetDao.copyBudgetsToNewMonth(
                currentMonth, currentYear, nextMonth, nextYear, System.currentTimeMillis());
    }

    /**
     * Send budget alert notification.
     */
    private void sendBudgetAlert(Budget budget, long categoryId) {
        categoryDao.getById(categoryId)
                .subscribeOn(Schedulers.io())
                .subscribe(category -> {
                    if (category != null) {
                        showNotification(budget, category);
                    }
                }, error -> {
                });
    }

    private void showNotification(Budget budget, ExpenseCategory category) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String title = "⚠️ Budget Alert: " + category.getName();
        String message;

        if (budget.isOverBudget()) {
            message = String.format("You've exceeded your ₹%.0f budget by ₹%.0f!",
                    budget.getBudgetAmount(), budget.getSpentAmount() - budget.getBudgetAmount());
        } else {
            message = String.format("You've spent %.0f%% of your ₹%.0f budget",
                    budget.getSpentPercentage(), budget.getBudgetAmount());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_BASE + (int) budget.getId(), builder.build());
        }
    }

    /**
     * Budget summary for display.
     */
    public static class BudgetSummary {
        public final double totalBudget;
        public final double totalSpent;
        public final double remaining;
        public final double spentPercentage;

        public BudgetSummary(double totalBudget, double totalSpent) {
            this.totalBudget = totalBudget;
            this.totalSpent = totalSpent;
            this.remaining = totalBudget - totalSpent;
            this.spentPercentage = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0;
        }
    }
}
