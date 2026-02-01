package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.Budget;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * DAO for Budget operations.
 */
@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Budget budget);

    @Update
    Completable update(Budget budget);

    @Delete
    Completable delete(Budget budget);

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    Flowable<List<Budget>> getBudgetsForMonth(int month, int year);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year")
    Single<Budget> getBudgetForCategory(long categoryId, int month, int year);

    @Query("SELECT * FROM budgets WHERE id = :id")
    Single<Budget> getById(long id);

    @Query("SELECT SUM(budgetAmount) FROM budgets WHERE month = :month AND year = :year")
    Single<Double> getTotalBudgetForMonth(int month, int year);

    @Query("SELECT SUM(spentAmount) FROM budgets WHERE month = :month AND year = :year")
    Single<Double> getTotalSpentForMonth(int month, int year);

    @Query("UPDATE budgets SET spentAmount = :amount, updatedAt = :timestamp WHERE id = :budgetId")
    Completable updateSpentAmount(long budgetId, double amount, long timestamp);

    @Query("SELECT * FROM budgets WHERE alertEnabled = 1 AND (spentAmount * 100.0 / budgetAmount) >= alertThresholdPercent")
    Flowable<List<Budget>> getBudgetsNeedingAlert();

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    Completable deleteBudgetsForMonth(int month, int year);

    /**
     * Copy budgets from previous month.
     */
    @Query("INSERT INTO budgets (categoryId, budgetAmount, spentAmount, month, year, alertEnabled, alertThresholdPercent, createdAt, updatedAt) "
            +
            "SELECT categoryId, budgetAmount, 0, :newMonth, :newYear, alertEnabled, alertThresholdPercent, :timestamp, :timestamp "
            +
            "FROM budgets WHERE month = :prevMonth AND year = :prevYear")
    Completable copyBudgetsToNewMonth(int prevMonth, int prevYear, int newMonth, int newYear, long timestamp);
}
