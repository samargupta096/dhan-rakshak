package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dhanrakshak.data.local.entity.LabReport;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface LabReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(LabReport report);

    @Delete
    Completable delete(LabReport report);

    @Query("SELECT * FROM lab_reports ORDER BY timestamp DESC")
    Flowable<List<LabReport>> getAllReports();
}
