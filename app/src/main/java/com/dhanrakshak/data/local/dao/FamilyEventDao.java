package com.dhanrakshak.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.FamilyEvent;

import java.util.List;

@Dao
public interface FamilyEventDao {
    @Query("SELECT * FROM family_events ORDER BY date ASC, startTime ASC")
    LiveData<List<FamilyEvent>> getAllEvents();

    @Insert
    void insert(FamilyEvent event);

    @Update
    void update(FamilyEvent event);

    @Delete
    void delete(FamilyEvent event);
}
