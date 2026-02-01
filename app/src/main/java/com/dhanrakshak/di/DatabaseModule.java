package com.dhanrakshak.di;

import android.content.Context;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.ExpenseCategoryDao;
import com.dhanrakshak.data.local.dao.FixedDepositDao;
import com.dhanrakshak.data.local.dao.MutualFundSchemeDao;
import com.dhanrakshak.data.local.dao.RecurringDepositDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.dao.TransactionDao;
import com.dhanrakshak.data.local.db.DhanRakshakDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module for database dependency injection.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Provide database instance.
     * TODO: Replace with encrypted version in production with biometric-derived
     * key.
     */
    @Provides
    @Singleton
    public DhanRakshakDatabase provideDatabase(@ApplicationContext Context context) {
        // For development, use unencrypted database
        // For production: DhanRakshakDatabase.getInstance(context, getEncryptionKey());
        return DhanRakshakDatabase.getInstanceUnencrypted(context);
    }

    @Provides
    @Singleton
    public AssetDao provideAssetDao(DhanRakshakDatabase database) {
        return database.assetDao();
    }

    @Provides
    @Singleton
    public TransactionDao provideTransactionDao(DhanRakshakDatabase database) {
        return database.transactionDao();
    }

    @Provides
    @Singleton
    public BankAccountDao provideBankAccountDao(DhanRakshakDatabase database) {
        return database.bankAccountDao();
    }

    @Provides
    @Singleton
    public SmsTransactionDao provideSmsTransactionDao(DhanRakshakDatabase database) {
        return database.smsTransactionDao();
    }

    @Provides
    @Singleton
    public ExpenseCategoryDao provideExpenseCategoryDao(DhanRakshakDatabase database) {
        return database.expenseCategoryDao();
    }

    @Provides
    @Singleton
    public FixedDepositDao provideFixedDepositDao(DhanRakshakDatabase database) {
        return database.fixedDepositDao();
    }

    @Provides
    @Singleton
    public RecurringDepositDao provideRecurringDepositDao(DhanRakshakDatabase database) {
        return database.recurringDepositDao();
    }

    @Provides
    @Singleton
    public MutualFundSchemeDao provideMutualFundSchemeDao(DhanRakshakDatabase database) {
        return database.mutualFundSchemeDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.InsurancePolicyDao provideInsurancePolicyDao(DhanRakshakDatabase database) {
        return database.insurancePolicyDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.HealthMetricDao provideHealthMetricDao(DhanRakshakDatabase database) {
        return database.healthMetricDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.HealthGoalDao provideHealthGoalDao(DhanRakshakDatabase database) {
        return database.healthGoalDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.LabReportDao provideLabReportDao(DhanRakshakDatabase database) {
        return database.labReportDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.GiftTransactionDao provideGiftTransactionDao(DhanRakshakDatabase database) {
        return database.giftTransactionDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.JobTaskDao provideJobTaskDao(DhanRakshakDatabase database) {
        return database.jobTaskDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.TripDao provideTripDao(DhanRakshakDatabase database) {
        return database.tripDao();
    }

    @Provides
    @Singleton
    public com.dhanrakshak.data.local.dao.FamilyEventDao provideFamilyEventDao(DhanRakshakDatabase database) {
        return database.familyEventDao();
    }
}
