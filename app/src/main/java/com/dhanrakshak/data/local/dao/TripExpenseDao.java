package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.TripExpense;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TripExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(TripExpense expense);

    @Update
    Completable update(TripExpense expense);

    @Delete
    Completable delete(TripExpense expense);

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY expenseDate DESC")
    Flowable<List<TripExpense>> getExpensesForTrip(long tripId);

    @Query("SELECT * FROM trip_expenses WHERE id = :id")
    Single<TripExpense> getById(long id);

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId AND tripDay = :day ORDER BY expenseDate ASC")
    Flowable<List<TripExpense>> getExpensesForTripDay(long tripId, int day);

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId AND category = :category")
    Flowable<List<TripExpense>> getExpensesByCategory(long tripId, String category);

    @Query("SELECT SUM(amountInInr) FROM trip_expenses WHERE tripId = :tripId")
    Single<Double> getTotalForTrip(long tripId);

    @Query("SELECT SUM(amountInInr) FROM trip_expenses WHERE tripId = :tripId AND category = :category")
    Single<Double> getTotalByCategory(long tripId, String category);

    @Query("SELECT SUM(amountInInr) FROM trip_expenses WHERE tripId = :tripId AND tripDay = :day")
    Single<Double> getTotalForDay(long tripId, int day);

    @Query("SELECT category, SUM(amountInInr) as total FROM trip_expenses WHERE tripId = :tripId GROUP BY category")
    Single<List<CategoryTotal>> getCategoryWiseTotals(long tripId);

    @Query("SELECT COUNT(*) FROM trip_expenses WHERE tripId = :tripId")
    Single<Integer> getExpenseCount(long tripId);

    class CategoryTotal {
        public String category;
        public double total;
    }
}
