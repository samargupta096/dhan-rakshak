package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.BillReminder;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface BillReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(BillReminder bill);

    @Update
    Completable update(BillReminder bill);

    @Delete
    Completable delete(BillReminder bill);

    @Query("SELECT * FROM bill_reminders WHERE isActive = 1 ORDER BY nextDueDate ASC")
    Flowable<List<BillReminder>> getActiveBills();

    @Query("SELECT * FROM bill_reminders ORDER BY nextDueDate ASC")
    Flowable<List<BillReminder>> getAllBills();

    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    Single<BillReminder> getById(long id);

    @Query("SELECT * FROM bill_reminders WHERE category = :category AND isActive = 1")
    Flowable<List<BillReminder>> getByCategory(String category);

    @Query("SELECT * FROM bill_reminders WHERE nextDueDate BETWEEN :start AND :end AND isActive = 1")
    Flowable<List<BillReminder>> getUpcomingBills(long start, long end);

    @Query("SELECT * FROM bill_reminders WHERE nextDueDate < :now AND isActive = 1")
    Flowable<List<BillReminder>> getOverdueBills(long now);

    @Query("SELECT SUM(amount) FROM bill_reminders WHERE frequency = 'MONTHLY' AND isActive = 1")
    Single<Double> getTotalMonthlyBills();

    @Query("UPDATE bill_reminders SET lastPaidDate = :paidDate, nextDueDate = :nextDate, updatedAt = :timestamp WHERE id = :id")
    Completable markAsPaid(long id, long paidDate, long nextDate, long timestamp);
}
