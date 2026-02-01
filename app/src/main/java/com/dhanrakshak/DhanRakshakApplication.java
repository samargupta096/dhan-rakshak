package com.dhanrakshak;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Dhan-Rakshak Application class.
 * Initializes Hilt dependency injection and WorkManager for background tasks.
 */
@HiltAndroidApp
public class DhanRakshakApplication extends Application implements Configuration.Provider {

        @Inject
        HiltWorkerFactory workerFactory;

        @Override
        public void onCreate() {
                super.onCreate();
                setupDailyNotificationWorker();
        }

        @NonNull
        @Override
        public Configuration getWorkManagerConfiguration() {
                return new Configuration.Builder()
                                .setWorkerFactory(workerFactory)
                                .setMinimumLoggingLevel(android.util.Log.INFO)
                                .build();
        }

        private void setupDailyNotificationWorker() {
                // Daily Net Worth Summary
                PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                                com.dhanrakshak.worker.DailySummaryWorker.class, 24,
                                java.util.concurrent.TimeUnit.HOURS)
                                .addTag("daily_summary")
                                .build();

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                                "DailySummaryWork",
                                ExistingPeriodicWorkPolicy.KEEP,
                                dailyWorkRequest);

                // Hourly Reminder Check
                PeriodicWorkRequest reminderWorkRequest = new PeriodicWorkRequest.Builder(
                                com.dhanrakshak.worker.ReminderWorker.class, 1, java.util.concurrent.TimeUnit.HOURS)
                                .addTag("reminder_check")
                                .build();

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                                "ReminderCheckWork",
                                ExistingPeriodicWorkPolicy.KEEP,
                                reminderWorkRequest);

                // Daily Cloud Sync (if logged in)
                PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                                com.dhanrakshak.worker.DataSyncWorker.class, 24, java.util.concurrent.TimeUnit.HOURS)
                                .setConstraints(new androidx.work.Constraints.Builder()
                                                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                                .build())
                                .addTag("cloud_sync")
                                .build();

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                                "DailyCloudSyncWork",
                                ExistingPeriodicWorkPolicy.KEEP,
                                syncWorkRequest);
        }
}
