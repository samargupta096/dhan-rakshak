package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.TripLocation;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TripLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(TripLocation location);

    @Update
    Completable update(TripLocation location);

    @Delete
    Completable delete(TripLocation location);

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId ORDER BY sequenceOrder ASC")
    Flowable<List<TripLocation>> getLocationsForTrip(long tripId);

    @Query("SELECT * FROM trip_locations WHERE id = :id")
    Single<TripLocation> getById(long id);

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId AND tripDay = :day ORDER BY sequenceOrder ASC")
    Flowable<List<TripLocation>> getLocationsForDay(long tripId, int day);

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId AND isHighlight = 1")
    Flowable<List<TripLocation>> getHighlights(long tripId);

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId AND locationType = :type")
    Flowable<List<TripLocation>> getByLocationType(long tripId, String type);

    @Query("SELECT COUNT(*) FROM trip_locations WHERE tripId = :tripId")
    Single<Integer> getLocationCount(long tripId);

    @Query("SELECT MAX(sequenceOrder) FROM trip_locations WHERE tripId = :tripId")
    Single<Integer> getMaxSequenceOrder(long tripId);

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId ORDER BY sequenceOrder DESC LIMIT 1")
    Single<TripLocation> getLastLocation(long tripId);
}
