package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.RecurringDeposit;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for RecurringDeposit entity.
 */
@Dao
public interface RecurringDepositDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(RecurringDeposit rd);

    @Update
    Completable update(RecurringDeposit rd);

    @Delete
    Completable delete(RecurringDeposit rd);

    @Query("SELECT * FROM recurring_deposits WHERE id = :id")
    Single<RecurringDeposit> getById(long id);

    @Query("SELECT * FROM recurring_deposits WHERE status = 'ACTIVE' ORDER BY maturityDate")
    Flowable<List<RecurringDeposit>> getActiveDeposits();

    @Query("SELECT * FROM recurring_deposits ORDER BY maturityDate")
    Flowable<List<RecurringDeposit>> getAllDeposits();

    @Query("SELECT SUM(depositedAmount) FROM recurring_deposits WHERE status = 'ACTIVE'")
    Flowable<Double> getTotalDeposited();

    @Query("SELECT SUM(maturityAmount) FROM recurring_deposits WHERE status = 'ACTIVE'")
    Flowable<Double> getTotalMaturityValue();

    @Query("UPDATE recurring_deposits SET installmentsPaid = :count, depositedAmount = monthlyAmount * :count WHERE id = :id")
    Completable updateInstallmentCount(long id, int count);
}
