package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.Reminder;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Reminder reminder);

    @Update
    Completable update(Reminder reminder);

    @Delete
    Completable delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 ORDER BY nextTriggerTime ASC")
    Flowable<List<Reminder>> getActiveReminders();

    @Query("SELECT * FROM reminders ORDER BY nextTriggerTime ASC")
    Flowable<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Single<Reminder> getById(long id);

    @Query("SELECT * FROM reminders WHERE category = :category AND isEnabled = 1")
    Flowable<List<Reminder>> getByCategory(String category);

    @Query("SELECT * FROM reminders WHERE nextTriggerTime BETWEEN :start AND :end AND isEnabled = 1")
    Flowable<List<Reminder>> getUpcomingReminders(long start, long end);

    @Query("SELECT * FROM reminders WHERE nextTriggerTime BETWEEN :start AND :end AND isEnabled = 1 ORDER BY nextTriggerTime ASC")
    Flowable<List<Reminder>> getRemindersBetween(long start, long end);

    @Query("SELECT * FROM reminders WHERE nextTriggerTime <= :now AND isEnabled = 1 AND isCompleted = 0")
    Flowable<List<Reminder>> getDueReminders(long now);

    @Query("SELECT * FROM reminders WHERE linkedBillId = :billId")
    Single<Reminder> getByBillId(long billId);

    @Query("SELECT * FROM reminders WHERE syncToCalendar = 1")
    Flowable<List<Reminder>> getCalendarSyncedReminders();

    @Query("UPDATE reminders SET isEnabled = :enabled, updatedAt = :time WHERE id = :id")
    Completable setEnabled(long id, boolean enabled, long time);

    @Query("UPDATE reminders SET nextTriggerTime = :time, snoozeCount = snoozeCount + 1, updatedAt = :updateTime WHERE id = :id")
    Completable snooze(long id, long time, long updateTime);

    @Query("UPDATE reminders SET lastTriggered = :time, isCompleted = :completed, updatedAt = :time WHERE id = :id")
    Completable markTriggered(long id, long time, boolean completed);

    @Query("UPDATE reminders SET calendarEventId = :eventId WHERE id = :id")
    Completable updateCalendarEventId(long id, String eventId);

    @Query("SELECT COUNT(*) FROM reminders WHERE isEnabled = 1 AND isCompleted = 0")
    Single<Integer> getActiveCount();
}
