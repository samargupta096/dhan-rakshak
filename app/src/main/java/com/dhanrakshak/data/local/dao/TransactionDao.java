package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.Transaction;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Transaction entity (investment transactions).
 */
@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Transaction transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<Transaction> transactions);

    @Update
    Completable update(Transaction transaction);

    @Delete
    Completable delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE id = :id")
    Single<Transaction> getById(long id);

    @Query("SELECT * FROM transactions WHERE assetId = :assetId ORDER BY date DESC")
    Flowable<List<Transaction>> getTransactionsByAsset(long assetId);

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    Flowable<List<Transaction>> getRecentTransactions(int limit);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    Flowable<List<Transaction>> getTransactionsBetweenDates(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM transactions WHERE assetId = :assetId AND type = 'BUY'")
    Single<Double> getTotalInvestmentByAsset(long assetId);

    @Query("DELETE FROM transactions WHERE assetId = :assetId")
    Completable deleteByAssetId(long assetId);
}
