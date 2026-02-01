package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dhanrakshak.data.local.entity.GiftTransaction;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface GiftTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(GiftTransaction transaction);

    @Delete
    Completable delete(GiftTransaction transaction);

    @Query("SELECT * FROM gift_transactions ORDER BY date DESC")
    Flowable<List<GiftTransaction>> getAllTransactions();

    @Query("SELECT * FROM gift_transactions WHERE type = :type ORDER BY date DESC")
    Flowable<List<GiftTransaction>> getTransactionsByType(String type); // GIVEN/RECEIVED

    @Query("SELECT SUM(value) FROM gift_transactions WHERE type = 'GIVEN'")
    Flowable<Double> getTotalGiven();

    @Query("SELECT SUM(value) FROM gift_transactions WHERE type = 'RECEIVED'")
    Flowable<Double> getTotalReceived();

    // For net position per person logic later
    @Query("SELECT * FROM gift_transactions WHERE personName LIKE :nameQuery ORDER BY date DESC")
    Flowable<List<GiftTransaction>> searchTransactionsByPerson(String nameQuery);
}
