package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dhanrakshak.data.local.entity.HealthMetric;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface HealthMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(HealthMetric metric);

    @Delete
    Completable delete(HealthMetric metric);

    @Query("SELECT * FROM health_metrics ORDER BY timestamp DESC")
    Flowable<List<HealthMetric>> getAllMetrics();

    @Query("SELECT * FROM health_metrics WHERE metricName = :name ORDER BY timestamp ASC")
    Flowable<List<HealthMetric>> getHistoryForMetric(String name);

    @Query("SELECT DISTINCT metricName FROM health_metrics")
    Flowable<List<String>> getAvailableMetricNames();
}
