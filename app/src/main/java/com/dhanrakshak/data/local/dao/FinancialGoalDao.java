package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.FinancialGoal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FinancialGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(FinancialGoal goal);

    @Update
    Completable update(FinancialGoal goal);

    @Delete
    Completable delete(FinancialGoal goal);

    @Query("SELECT * FROM financial_goals ORDER BY priority ASC, targetDate ASC")
    Flowable<List<FinancialGoal>> getAllGoals();

    @Query("SELECT * FROM financial_goals WHERE isCompleted = 0 ORDER BY priority ASC")
    Flowable<List<FinancialGoal>> getActiveGoals();

    @Query("SELECT * FROM financial_goals WHERE isCompleted = 1")
    Flowable<List<FinancialGoal>> getCompletedGoals();

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    Single<FinancialGoal> getById(long id);

    @Query("SELECT * FROM financial_goals WHERE category = :category")
    Flowable<List<FinancialGoal>> getByCategory(String category);

    @Query("SELECT SUM(targetAmount) FROM financial_goals WHERE isCompleted = 0")
    Single<Double> getTotalTargetAmount();

    @Query("SELECT SUM(currentAmount) FROM financial_goals WHERE isCompleted = 0")
    Single<Double> getTotalCurrentAmount();

    @Query("UPDATE financial_goals SET currentAmount = :amount, updatedAt = :timestamp WHERE id = :id")
    Completable updateProgress(long id, double amount, long timestamp);
}
