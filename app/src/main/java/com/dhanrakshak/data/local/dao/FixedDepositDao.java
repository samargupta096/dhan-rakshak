package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.FixedDeposit;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for FixedDeposit entity.
 */
@Dao
public interface FixedDepositDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(FixedDeposit fd);

    @Update
    Completable update(FixedDeposit fd);

    @Delete
    Completable delete(FixedDeposit fd);

    @Query("SELECT * FROM fixed_deposits WHERE id = :id")
    Single<FixedDeposit> getById(long id);

    @Query("SELECT * FROM fixed_deposits WHERE status = 'ACTIVE' ORDER BY maturityDate")
    Flowable<List<FixedDeposit>> getActiveDeposits();

    @Query("SELECT * FROM fixed_deposits ORDER BY maturityDate")
    Flowable<List<FixedDeposit>> getAllDeposits();

    @Query("SELECT * FROM fixed_deposits WHERE maturityDate BETWEEN :start AND :end")
    Flowable<List<FixedDeposit>> getMaturingBetween(long start, long end);

    @Query("SELECT SUM(principal) FROM fixed_deposits WHERE status = 'ACTIVE'")
    Flowable<Double> getTotalPrincipal();

    @Query("SELECT SUM(maturityAmount) FROM fixed_deposits WHERE status = 'ACTIVE'")
    Flowable<Double> getTotalMaturityValue();
}
