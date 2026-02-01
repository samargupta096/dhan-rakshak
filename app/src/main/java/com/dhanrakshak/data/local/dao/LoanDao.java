package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.Loan;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface LoanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Loan loan);

    @Update
    Completable update(Loan loan);

    @Delete
    Completable delete(Loan loan);

    @Query("SELECT * FROM loans WHERE isActive = 1 ORDER BY nextEmiDate ASC")
    Flowable<List<Loan>> getActiveLoans();

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    Flowable<List<Loan>> getAllLoans();

    @Query("SELECT * FROM loans WHERE id = :id")
    Single<Loan> getById(long id);

    @Query("SELECT * FROM loans WHERE loanType = :type AND isActive = 1")
    Flowable<List<Loan>> getByType(String type);

    @Query("SELECT SUM(outstandingPrincipal) FROM loans WHERE isActive = 1")
    Single<Double> getTotalOutstanding();

    @Query("SELECT SUM(emiAmount) FROM loans WHERE isActive = 1")
    Single<Double> getTotalMonthlyEmi();

    @Query("SELECT * FROM loans WHERE nextEmiDate BETWEEN :start AND :end AND isActive = 1")
    Flowable<List<Loan>> getUpcomingEmis(long start, long end);
}
