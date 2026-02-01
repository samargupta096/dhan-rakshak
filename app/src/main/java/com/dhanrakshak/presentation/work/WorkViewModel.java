package com.dhanrakshak.presentation.work;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.JobTaskDao;
import com.dhanrakshak.data.local.entity.JobTask;
import com.dhanrakshak.data.local.entity.WorkLog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class WorkViewModel extends ViewModel {

    private final JobTaskDao jobDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<JobTask>> activeTasks = new MutableLiveData<>();
    private final MutableLiveData<Double> hoursLoggedToday = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> overallEfficiency = new MutableLiveData<>("Calculating...");
    private final MutableLiveData<Double> totalBehindSchedule = new MutableLiveData<>(0.0);

    @Inject
    public WorkViewModel(JobTaskDao jobDao) {
        this.jobDao = jobDao;
        loadData();
    }

    private void loadData() {
        // Load Active Tasks
        disposables.add(jobDao.getActiveTasks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tasks -> {
                    calculateMetrics(tasks);
                    activeTasks.setValue(tasks);
                }, Throwable::printStackTrace));

        // Load Hours Today
        long startOfDay = getStartOfDay();
        disposables.add(jobDao.getHoursLoggedToday(startOfDay)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hours -> hoursLoggedToday.setValue(hours != null ? hours : 0.0),
                        Throwable::printStackTrace));
    }

    private void calculateMetrics(List<JobTask> tasks) {
        double totalBehind = 0;
        int onTrackCount = 0;

        for (JobTask task : tasks) {
            // Need to fetch actual spent hours for strict calculation,
            // but for list view we might rely on the cached 'hoursSpent' field we added to
            // Entity earlier.
            // Assuming we keep that updated.

            // Logic:
            // Time Elapsed % = (Now - Created) / (Deadline - Created)
            // Expected Spent = Allocated * Time Elapsed %
            // Behind = Expected Spent - Actual Spent

            long now = System.currentTimeMillis();
            long totalDuration = task.getDeadlineDate() - task.getCreatedDate();
            long elapsed = now - task.getCreatedDate();

            if (totalDuration <= 0)
                continue;

            double elapsedPercent = (double) elapsed / totalDuration;
            if (elapsedPercent > 1.0)
                elapsedPercent = 1.0; // Overdue

            double expectedSpent = task.getAllocatedHours() * elapsedPercent;
            double actualSpent = task.getHoursSpent();

            double diff = expectedSpent - actualSpent;

            if (diff > 0.5) { // Behind by more than 30 mins
                totalBehind += diff;
            } else {
                onTrackCount++;
            }
        }

        totalBehindSchedule.setValue(totalBehind);

        if (tasks.isEmpty()) {
            overallEfficiency.setValue("No Active Tasks");
        } else if (totalBehind > 5) {
            overallEfficiency.setValue("Critical Delay");
        } else if (totalBehind > 0) {
            overallEfficiency.setValue("Slightly Behind");
        } else {
            overallEfficiency.setValue("On Track");
        }
    }

    public void addTask(JobTask task) {
        disposables.add(jobDao.insertTask(task)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                }, Throwable::printStackTrace));
    }

    public void logHours(long taskId, double hours, String notes) {
        WorkLog log = new WorkLog(taskId, System.currentTimeMillis(), hours, notes);
        disposables.add(jobDao.insertLog(log)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Trigger update of cached hours in Task
                    updateTaskSpent(taskId);
                }, Throwable::printStackTrace));
    }

    private void updateTaskSpent(long taskId) {
        disposables.add(jobDao.updateTaskSpentHours(taskId)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> loadData(), Throwable::printStackTrace));
    }

    private long getStartOfDay() {
        return System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000));
        // Simple approximation, better to use Calendar/LocalDate for timezone
        // correctness
    }

    public LiveData<List<JobTask>> getActiveTasks() {
        return activeTasks;
    }

    public LiveData<Double> getHoursLoggedToday() {
        return hoursLoggedToday;
    }

    public LiveData<String> getOverallEfficiency() {
        return overallEfficiency;
    }

    public LiveData<Double> getTotalBehindSchedule() {
        return totalBehindSchedule;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
