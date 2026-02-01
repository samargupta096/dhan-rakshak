package com.dhanrakshak.presentation.planner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.FinancialGoalDao;
import com.dhanrakshak.data.local.dao.ReminderDao;
import com.dhanrakshak.data.local.entity.FinancialGoal;
import com.dhanrakshak.data.local.entity.Reminder;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Planner screen.
 * Manages Goals, Reminders, and daily tasks.
 */
@HiltViewModel
public class PlannerViewModel extends ViewModel {

    private final FinancialGoalDao goalDao;
    private final ReminderDao reminderDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<FinancialGoal>> goals = new MutableLiveData<>();
    private final MutableLiveData<List<Reminder>> reminders = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    @Inject
    public PlannerViewModel(FinancialGoalDao goalDao, ReminderDao reminderDao) {
        this.goalDao = goalDao;
        this.reminderDao = reminderDao;
    }

    public LiveData<List<FinancialGoal>> getGoals() {
        return goals;
    }

    public LiveData<List<Reminder>> getReminders() {
        return reminders;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadGoals() {
        disposables.add(
                goalDao.getAllGoals()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                goals::setValue,
                                error -> message.setValue("Failed to load goals: " + error.getMessage())));
    }

    public void loadReminders() {
        disposables.add(
                reminderDao.getActiveReminders()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                reminders::setValue,
                                error -> message.setValue("Failed to load reminders: " + error.getMessage())));
    }

    public void loadTodayTasks() {
        // Load reminders due today
        long startOfDay = getStartOfDay();
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);

        disposables.add(
                reminderDao.getRemindersBetween(startOfDay, endOfDay)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                reminders::setValue,
                                error -> message.setValue("Failed to load tasks: " + error.getMessage())));
    }

    public void addGoal(FinancialGoal goal) {
        disposables.add(
                goalDao.insert(goal)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                id -> {
                                    message.setValue("Goal added successfully!");
                                    loadGoals();
                                },
                                error -> message.setValue("Failed to add goal: " + error.getMessage())));
    }

    public void addReminder(Reminder reminder) {
        disposables.add(
                reminderDao.insert(reminder)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                id -> {
                                    message.setValue("Reminder added successfully!");
                                    loadReminders();
                                },
                                error -> message.setValue("Failed to add reminder: " + error.getMessage())));
    }

    public void markReminderComplete(Reminder reminder) {
        reminder.setCompleted(true);
        reminder.setUpdatedAt(System.currentTimeMillis());

        disposables.add(
                reminderDao.update(reminder)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    message.setValue("Reminder completed!");
                                    loadReminders();
                                },
                                error -> message.setValue("Failed to update reminder")));
    }

    private long getStartOfDay() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
