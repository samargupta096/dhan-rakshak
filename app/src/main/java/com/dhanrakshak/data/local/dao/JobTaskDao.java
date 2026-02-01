package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.JobTask;
import com.dhanrakshak.data.local.entity.WorkLog;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface JobTaskDao {

    // --- Task Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertTask(JobTask task);

    @Update
    Completable updateTask(JobTask task);

    @Delete
    Completable deleteTask(JobTask task);

    @Query("SELECT * FROM job_tasks ORDER BY deadlineDate ASC")
    Flowable<List<JobTask>> getAllTasks();

    @Query("SELECT * FROM job_tasks WHERE status != 'DONE' ORDER BY priority DESC, deadlineDate ASC")
    Flowable<List<JobTask>> getActiveTasks();

    @Query("SELECT * FROM job_tasks WHERE id = :id")
    Flowable<JobTask> getTaskById(long id);

    // --- Work Log Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertLog(WorkLog log);

    @Delete
    Completable deleteLog(WorkLog log);

    @Query("SELECT * FROM work_logs WHERE taskId = :taskId ORDER BY date DESC")
    Flowable<List<WorkLog>> getLogsForTask(long taskId);

    @Query("SELECT * FROM work_logs WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    Flowable<List<WorkLog>> getLogsByDateRange(long start, long end);

    @Query("SELECT SUM(hoursLogged) FROM work_logs WHERE taskId = :taskId")
    Flowable<Double> getTotalHoursForTask(long taskId);

    // Manual Update for cache
    @Query("UPDATE job_tasks SET hoursSpent = (SELECT COALESCE(SUM(hoursLogged), 0) FROM work_logs WHERE taskId = job_tasks.id) WHERE id = :taskId")
    Completable updateTaskSpentHours(long taskId);

    @Query("SELECT SUM(hoursLogged) FROM work_logs WHERE date >= :todayStart")
    Flowable<Double> getHoursLoggedToday(long todayStart);
}
