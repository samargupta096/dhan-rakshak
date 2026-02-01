package com.dhanrakshak.domain.reminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.dao.ReminderDao;
import com.dhanrakshak.data.local.entity.Reminder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Notification Scheduler for managing reminder notifications.
 * Uses WorkManager for reliable background scheduling.
 */
@Singleton
public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    private static final String CHANNEL_REMINDERS = "reminders";
    private static final String CHANNEL_INSIGHTS = "insights";
    private static final String CHANNEL_BILLS = "bills";

    private static final String WORK_WEEKLY_INSIGHTS = "weekly_insights";
    private static final String WORK_WEEKLY_EXPENSES = "weekly_expenses";
    private static final String WORK_CHECK_REMINDERS = "check_reminders";

    private final Context context;
    private final ReminderDao reminderDao;
    private final WorkManager workManager;
    private final NotificationManager notificationManager;

    @Inject
    public NotificationScheduler(Context context, ReminderDao reminderDao) {
        this.context = context.getApplicationContext();
        this.reminderDao = reminderDao;
        this.workManager = WorkManager.getInstance(context);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            remindersChannel.setDescription("Personal financial reminders");
            remindersChannel.enableVibration(true);

            // Insights channel
            NotificationChannel insightsChannel = new NotificationChannel(
                    CHANNEL_INSIGHTS,
                    "Weekly Insights",
                    NotificationManager.IMPORTANCE_DEFAULT);
            insightsChannel.setDescription("AI-powered financial insights");

            // Bills channel
            NotificationChannel billsChannel = new NotificationChannel(
                    CHANNEL_BILLS,
                    "Bill Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            billsChannel.setDescription("Upcoming bill payment reminders");
            billsChannel.enableVibration(true);

            notificationManager.createNotificationChannel(remindersChannel);
            notificationManager.createNotificationChannel(insightsChannel);
            notificationManager.createNotificationChannel(billsChannel);
        }
    }

    /**
     * Schedule weekly insights notification.
     * 
     * @param dayOfWeek Calendar.SUNDAY = 1, Calendar.SATURDAY = 7
     * @param hour      0-23
     * @param minute    0-59
     */
    public void scheduleWeeklyInsights(int dayOfWeek, int hour, int minute) {
        long delay = calculateDelayToNextOccurrence(dayOfWeek, hour, minute);

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeeklyInsightsWorker.class,
                7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_WEEKLY_INSIGHTS)
                .build();

        workManager.enqueueUniquePeriodicWork(
                WORK_WEEKLY_INSIGHTS,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest);
    }

    /**
     * Schedule weekly expense review notification.
     */
    public void scheduleWeeklyExpenseReview(int dayOfWeek, int hour, int minute) {
        long delay = calculateDelayToNextOccurrence(dayOfWeek, hour, minute);

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeeklyExpenseWorker.class,
                7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_WEEKLY_EXPENSES)
                .build();

        workManager.enqueueUniquePeriodicWork(
                WORK_WEEKLY_EXPENSES,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest);
    }

    /**
     * Schedule periodic reminder check (every 15 minutes).
     */
    public void scheduleReminderCheck() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                ReminderCheckWorker.class,
                15, TimeUnit.MINUTES)
                .addTag(WORK_CHECK_REMINDERS)
                .build();

        workManager.enqueueUniquePeriodicWork(
                WORK_CHECK_REMINDERS,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest);
    }

    /**
     * Schedule a specific reminder notification.
     */
    public void scheduleReminder(Reminder reminder) {
        long delay = reminder.getNextTriggerTime() - System.currentTimeMillis();
        if (delay < 0)
            delay = 0;

        Data inputData = new Data.Builder()
                .putLong("reminder_id", reminder.getId())
                .putString("title", reminder.getTitle())
                .putString("description", reminder.getDescription())
                .putString("category", reminder.getCategory())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(
                ReminderNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_" + reminder.getId())
                .build();

        workManager.enqueue(workRequest);
    }

    /**
     * Cancel a scheduled reminder.
     */
    public void cancelReminder(long reminderId) {
        workManager.cancelAllWorkByTag("reminder_" + reminderId);
    }

    /**
     * Cancel all weekly notifications.
     */
    public void cancelWeeklyNotifications() {
        workManager.cancelUniqueWork(WORK_WEEKLY_INSIGHTS);
        workManager.cancelUniqueWork(WORK_WEEKLY_EXPENSES);
    }

    /**
     * Show immediate notification.
     */
    public void showNotification(String channelId, int notificationId,
            String title, String content, String category) {
        String icon = getIconForCategory(category);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(icon + " " + title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Add action buttons based on category
        if ("BILL".equals(category)) {
            builder.addAction(0, "Mark Paid", createActionIntent("MARK_PAID", notificationId));
            builder.addAction(0, "Snooze", createActionIntent("SNOOZE", notificationId));
        } else if ("EXPENSE_REVIEW".equals(category) || "INSIGHTS".equals(category)) {
            builder.addAction(0, "View Now", createActionIntent("VIEW", notificationId));
        }

        notificationManager.notify(notificationId, builder.build());
    }

    private PendingIntent createActionIntent(String action, int notificationId) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(action);
        intent.putExtra("notification_id", notificationId);

        return PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private String getIconForCategory(String category) {
        if (category == null)
            return "⏰";
        switch (category) {
            case "BILL":
                return "💳";
            case "INVESTMENT":
                return "📈";
            case "GOAL":
                return "🎯";
            case "EXPENSE_REVIEW":
                return "📊";
            case "INSIGHTS":
                return "💡";
            default:
                return "⏰";
        }
    }

    private long calculateDelayToNextOccurrence(int dayOfWeek, int hour, int minute) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }

        return cal.getTimeInMillis() - System.currentTimeMillis();
    }

    // Worker classes
    public static class WeeklyInsightsWorker extends Worker {
        public WeeklyInsightsWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            // Show insights notification
            NotificationManager nm = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    getApplicationContext(), CHANNEL_INSIGHTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("💡 Your Weekly Financial Insights")
                    .setContentText("AI has analyzed your finances. Tap to see recommendations!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            nm.notify(1001, builder.build());
            return Result.success();
        }
    }

    public static class WeeklyExpenseWorker extends Worker {
        public WeeklyExpenseWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            NotificationManager nm = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    getApplicationContext(), CHANNEL_INSIGHTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("📊 Weekly Expense Review")
                    .setContentText("See how you spent this week and compare with last week!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            nm.notify(1002, builder.build());
            return Result.success();
        }
    }

    public static class ReminderCheckWorker extends Worker {
        public ReminderCheckWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            // In a real implementation, would inject ReminderDao and check for due
            // reminders
            return Result.success();
        }
    }

    public static class ReminderNotificationWorker extends Worker {
        public ReminderNotificationWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            String title = getInputData().getString("title");
            String description = getInputData().getString("description");
            String category = getInputData().getString("category");
            long reminderId = getInputData().getLong("reminder_id", 0);

            if (title == null)
                return Result.failure();

            NotificationManager nm = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "BILL".equals(category) ? CHANNEL_BILLS : CHANNEL_REMINDERS;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    getApplicationContext(), channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(description != null ? description : "Tap to view details")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            nm.notify((int) reminderId, builder.build());
            return Result.success();
        }
    }

    // BroadcastReceiver for notification actions
    public static class NotificationActionReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int notificationId = intent.getIntExtra("notification_id", 0);

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(notificationId);

            // Handle actions - in real app would update database
            if ("SNOOZE".equals(action)) {
                // Snooze for 1 hour
            } else if ("MARK_PAID".equals(action)) {
                // Mark bill as paid
            }
        }
    }
}
