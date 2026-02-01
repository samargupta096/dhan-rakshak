package com.dhanrakshak.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.HealthGoal;

import java.util.List;

@Dao
public interface HealthGoalDao {
    @Query("SELECT * FROM health_goals")
    LiveData<List<HealthGoal>> getAllGoals();

    @Query("SELECT * FROM health_goals WHERE metricType = :type LIMIT 1")
    LiveData<HealthGoal> getGoalByType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HealthGoal goal);

    @Update
    void update(HealthGoal goal);
}
