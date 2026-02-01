package com.dhanrakshak.data.local.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.BillReminderDao;
import com.dhanrakshak.data.local.dao.BudgetDao;
import com.dhanrakshak.data.local.dao.CryptoHoldingDao;
import com.dhanrakshak.data.local.dao.ExpenseCategoryDao;
import com.dhanrakshak.data.local.dao.FamilyMemberDao;
import com.dhanrakshak.data.local.dao.FinancialGoalDao;
import com.dhanrakshak.data.local.dao.FixedDepositDao;
import com.dhanrakshak.data.local.dao.LoanDao;
import com.dhanrakshak.data.local.dao.MutualFundSchemeDao;
import com.dhanrakshak.data.local.dao.RecurringDepositDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.dao.TransactionDao;
import com.dhanrakshak.data.local.dao.TripDao;
import com.dhanrakshak.data.local.dao.TripExpenseDao;
import com.dhanrakshak.data.local.dao.TripLocationDao;
import com.dhanrakshak.data.local.dao.UpiAccountDao;
import com.dhanrakshak.data.local.dao.ReminderDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.BillReminder;
import com.dhanrakshak.data.local.entity.Budget;
import com.dhanrakshak.data.local.entity.CryptoHolding;
import com.dhanrakshak.data.local.entity.ExpenseCategory;
import com.dhanrakshak.data.local.entity.FamilyMember;
import com.dhanrakshak.data.local.entity.FinancialGoal;
import com.dhanrakshak.data.local.entity.FixedDeposit;
import com.dhanrakshak.data.local.entity.Loan;
import com.dhanrakshak.data.local.entity.MutualFundScheme;
import com.dhanrakshak.data.local.entity.RecurringDeposit;
import com.dhanrakshak.data.local.entity.Reminder;
import com.dhanrakshak.data.local.entity.SmsTransaction;
import com.dhanrakshak.data.local.entity.Transaction;
import com.dhanrakshak.data.local.entity.Trip;
import com.dhanrakshak.data.local.entity.TripExpense;
import com.dhanrakshak.data.local.entity.TripLocation;
import com.dhanrakshak.data.local.entity.UpiAccount;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.util.concurrent.Executors;

/**
 * Room Database for Dhan-Rakshak.
 * Encrypted with SQLCipher for financial data security.
 * Version 6: Added Reminder and Calendar Sync.
 */
@Database(entities = {
        Asset.class,
        Transaction.class,
        BankAccount.class,
        SmsTransaction.class,
        ExpenseCategory.class,
        FixedDeposit.class,
        RecurringDeposit.class,
        MutualFundScheme.class,
        Budget.class,
        FinancialGoal.class,
        Loan.class,
        CryptoHolding.class,
        BillReminder.class,
        FamilyMember.class,
        Trip.class,
        TripExpense.class,
        TripLocation.class,
        UpiAccount.class,
        Reminder.class,
        com.dhanrakshak.data.local.entity.InsurancePolicy.class,
        com.dhanrakshak.data.local.entity.HealthMetric.class,
        com.dhanrakshak.data.local.entity.HealthGoal.class,
        com.dhanrakshak.data.local.entity.LabReport.class,
        com.dhanrakshak.data.local.entity.GiftTransaction.class,
        com.dhanrakshak.data.local.entity.JobTask.class,
        com.dhanrakshak.data.local.entity.WorkLog.class,
        com.dhanrakshak.data.local.entity.FamilyEvent.class
}, version = 10, exportSchema = true)

public abstract class DhanRakshakDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "dhanrakshak.db";
    private static volatile DhanRakshakDatabase INSTANCE;

    // Core DAOs
    public abstract AssetDao assetDao();

    public abstract TransactionDao transactionDao();

    public abstract BankAccountDao bankAccountDao();

    public abstract SmsTransactionDao smsTransactionDao();

    public abstract ExpenseCategoryDao expenseCategoryDao();

    public abstract FixedDepositDao fixedDepositDao();

    public abstract RecurringDepositDao recurringDepositDao();

    public abstract MutualFundSchemeDao mutualFundSchemeDao();

    public abstract BudgetDao budgetDao();

    // Advanced Feature DAOs
    public abstract FinancialGoalDao financialGoalDao();

    public abstract LoanDao loanDao();

    public abstract CryptoHoldingDao cryptoHoldingDao();

    public abstract BillReminderDao billReminderDao();

    public abstract FamilyMemberDao familyMemberDao();

    // Trip Planner DAOs
    public abstract TripDao tripDao();

    public abstract TripExpenseDao tripExpenseDao();

    public abstract TripLocationDao tripLocationDao();

    // UPI Management DAO
    public abstract UpiAccountDao upiAccountDao();

    // Reminder DAO
    public abstract ReminderDao reminderDao();

    // Insurance Policy DAO
    public abstract com.dhanrakshak.data.local.dao.InsurancePolicyDao insurancePolicyDao();

    // Health DAOs
    public abstract com.dhanrakshak.data.local.dao.HealthMetricDao healthMetricDao();

    public abstract com.dhanrakshak.data.local.dao.HealthGoalDao healthGoalDao();

    public abstract com.dhanrakshak.data.local.dao.LabReportDao labReportDao();

    // Gift DAO
    public abstract com.dhanrakshak.data.local.dao.GiftTransactionDao giftTransactionDao();

    // Work DAO
    public abstract com.dhanrakshak.data.local.dao.JobTaskDao jobTaskDao();

    // Family Event DAO
    public abstract com.dhanrakshak.data.local.dao.FamilyEventDao familyEventDao();

    /**
     * Get encrypted database instance.
     * Uses SQLCipher for encryption at rest.
     */
    public static DhanRakshakDatabase getInstance(Context context, String passphrase) {
        if (INSTANCE == null) {
            synchronized (DhanRakshakDatabase.class) {
                if (INSTANCE == null) {
                    // Create SQLCipher support factory
                    byte[] passphraseBytes = SQLiteDatabase.getBytes(passphrase.toCharArray());
                    SupportFactory factory = new SupportFactory(passphraseBytes);

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DhanRakshakDatabase.class,
                            DATABASE_NAME)
                            .openHelperFactory(factory)
                            .addCallback(new DatabaseCallback())
                            .setQueryExecutor(Executors.newFixedThreadPool(4))
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Get non-encrypted database instance (for development/testing only).
     */
    public static DhanRakshakDatabase getInstanceUnencrypted(Context context) {
        if (INSTANCE == null) {
            synchronized (DhanRakshakDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DhanRakshakDatabase.class,
                            DATABASE_NAME)
                            .addCallback(new DatabaseCallback())
                            .setQueryExecutor(Executors.newFixedThreadPool(4))
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Database callback for pre-populating default categories.
     */
    private static class DatabaseCallback extends RoomDatabase.Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Pre-populate expense categories
            Executors.newSingleThreadExecutor().execute(() -> {
                if (INSTANCE != null) {
                    prepopulateCategories(INSTANCE.expenseCategoryDao());
                }
            });
        }
    }

    /**
     * Pre-populate default expense categories.
     */
    private static void prepopulateCategories(ExpenseCategoryDao dao) {
        String[][] defaultCategories = {
                { "Food & Dining", "restaurant", "#FF9800" },
                { "Shopping", "shopping_cart", "#E91E63" },
                { "Transport", "directions_car", "#3F51B5" },
                { "Entertainment", "movie", "#9C27B0" },
                { "Utilities", "receipt", "#009688" },
                { "Health", "local_hospital", "#F44336" },
                { "Education", "school", "#2196F3" },
                { "Groceries", "local_grocery_store", "#4CAF50" },
                { "Rent", "home", "#795548" },
                { "Insurance", "security", "#607D8B" },
                { "Travel", "flight", "#00BCD4" },
                { "Personal Care", "spa", "#FF5722" },
                { "Gifts", "card_giftcard", "#E91E63" },
                { "Investments", "trending_up", "#4CAF50" },
                { "Other", "more_horiz", "#9E9E9E" }
        };

        for (int i = 0; i < defaultCategories.length; i++) {
            ExpenseCategory category = new ExpenseCategory(
                    defaultCategories[i][0],
                    defaultCategories[i][1],
                    defaultCategories[i][2],
                    false);
            category.setDisplayOrder(i);
            dao.insert(category).subscribe();
        }
    }
}
