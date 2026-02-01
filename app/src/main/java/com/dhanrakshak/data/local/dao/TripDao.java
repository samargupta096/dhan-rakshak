package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.dhanrakshak.data.local.entity.Trip;
import com.dhanrakshak.data.local.entity.TripExpense;
import com.dhanrakshak.data.local.entity.TripLocation;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TripDao {

    // --- Trip Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertTrip(Trip trip);

    @Update
    Completable updateTrip(Trip trip);

    @Delete
    Completable deleteTrip(Trip trip);

    @Query("SELECT * FROM trips ORDER BY startDate ASC")
    Flowable<List<Trip>> getAllTrips();

    @Query("SELECT * FROM trips WHERE endDate >= :currentTime ORDER BY startDate ASC")
    Flowable<List<Trip>> getUpcomingTrips(long currentTime);

    @Query("SELECT * FROM trips WHERE id = :tripId")
    Flowable<Trip> getTripById(long tripId);

    // --- Expense Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertExpense(TripExpense expense);

    @Delete
    Completable deleteExpense(TripExpense expense);

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY date DESC")
    Flowable<List<TripExpense>> getExpensesForTrip(long tripId);

    @Query("SELECT SUM(amount) FROM trip_expenses WHERE tripId = :tripId")
    Flowable<Double> getTotalExpensesForTrip(long tripId);

    // --- Location Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertLocation(TripLocation location);

    @Delete
    Completable deleteLocation(TripLocation location);

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId ORDER BY arrivalTime ASC")
    Flowable<List<TripLocation>> getLocationsForTrip(long tripId);
}
