package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.SmsTransaction;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for SmsTransaction entity.
 */
@Dao
public interface SmsTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(SmsTransaction transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<SmsTransaction> transactions);

    @Update
    Completable update(SmsTransaction transaction);

    @Delete
    Completable delete(SmsTransaction transaction);

    @Query("SELECT * FROM sms_transactions WHERE id = :id")
    Single<SmsTransaction> getById(long id);

    @Query("SELECT * FROM sms_transactions WHERE isSpam = 0 ORDER BY timestamp DESC LIMIT :limit")
    Flowable<List<SmsTransaction>> getRecentTransactions(int limit);

    @Query("SELECT * FROM sms_transactions WHERE bankAccountId = :accountId AND isSpam = 0 ORDER BY timestamp DESC")
    Flowable<List<SmsTransaction>> getTransactionsByAccount(long accountId);

    @Query("SELECT * FROM sms_transactions WHERE type = :type AND isSpam = 0 ORDER BY timestamp DESC")
    Flowable<List<SmsTransaction>> getTransactionsByType(String type);

    @Query("SELECT * FROM sms_transactions WHERE category = :category AND isSpam = 0 ORDER BY timestamp DESC")
    Flowable<List<SmsTransaction>> getTransactionsByCategory(String category);

    @Query("SELECT * FROM sms_transactions WHERE timestamp BETWEEN :startDate AND :endDate AND isSpam = 0 ORDER BY timestamp DESC")
    Flowable<List<SmsTransaction>> getTransactionsBetweenDates(long startDate, long endDate);

    @Query("SELECT * FROM sms_transactions WHERE category = :category AND timestamp BETWEEN :startDate AND :endDate AND isSpam = 0 ORDER BY timestamp DESC")
    Flowable<List<SmsTransaction>> getTransactionsByCategoryBetweenDates(String category, long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM sms_transactions WHERE type = 'DEBIT' AND timestamp BETWEEN :startDate AND :endDate AND isSpam = 0")
    Single<Double> getTotalDebitBetweenDates(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM sms_transactions WHERE type = 'CREDIT' AND timestamp BETWEEN :startDate AND :endDate AND isSpam = 0")
    Single<Double> getTotalCreditBetweenDates(long startDate, long endDate);

    @Query("SELECT category, SUM(amount) as total FROM sms_transactions WHERE type = 'DEBIT' AND timestamp BETWEEN :startDate AND :endDate AND isSpam = 0 GROUP BY category ORDER BY total DESC")
    Flowable<List<CategorySum>> getCategoryWiseSpending(long startDate, long endDate);

    @Query("UPDATE sms_transactions SET category = :category, categoryManual = 1 WHERE id = :id")
    Completable updateCategory(long id, String category);

    @Query("UPDATE sms_transactions SET isSpam = :isSpam WHERE id = :id")
    Completable markAsSpam(long id, boolean isSpam);

    @Query("SELECT * FROM sms_transactions WHERE merchant LIKE '%' || :query || '%' AND isSpam = 0 ORDER BY timestamp DESC")
    Flowable<List<SmsTransaction>> searchByMerchant(String query);

    /**
     * Inner class for category aggregation results
     */
    class CategorySum {
        public String category;
        public double total;
    }
}
