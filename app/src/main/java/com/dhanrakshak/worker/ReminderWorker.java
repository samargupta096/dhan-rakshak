package com.dhanrakshak.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.dao.ReminderDao;
import com.dhanrakshak.data.local.entity.Reminder;
import com.dhanrakshak.presentation.MainActivity;

import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Worker to check and trigger due reminders.
 * Runs periodically to check for reminders that are due.
 */
@HiltWorker
public class ReminderWorker extends RxWorker {

    private static final String CHANNEL_ID = "reminder_channel";
    private static final int BASE_NOTIFICATION_ID = 2000;

    private final ReminderDao reminderDao;
    private final Context context;

    @AssistedInject
    public ReminderWorker(@Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            ReminderDao reminderDao) {
        super(context, workerParams);
        this.context = context;
        this.reminderDao = reminderDao;
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        return reminderDao.getDueReminders(System.currentTimeMillis())
                .firstOrError()
                .map(reminders -> {
                    for (Reminder reminder : reminders) {
                        sendReminderNotification(reminder);
                        markReminderTriggered(reminder);
                    }
                    return Result.success();
                })
                .onErrorReturnItem(Result.success())
                .subscribeOn(Schedulers.io());
    }

    private void sendReminderNotification(Reminder reminder) {
        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("reminder_id", reminder.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                (int) reminder.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title = reminder.getCategoryIcon() + " " + reminder.getTitle();
        String content = reminder.getDescription() != null ? reminder.getDescription() : "Tap to view details";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (reminder.isVibrate()) {
            builder.setVibrate(new long[] { 0, 250, 250, 250 });
        }

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context,
                        android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(BASE_NOTIFICATION_ID + (int) reminder.getId(), builder.build());
                }
            } else {
                notificationManager.notify(BASE_NOTIFICATION_ID + (int) reminder.getId(), builder.build());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void markReminderTriggered(Reminder reminder) {
        reminder.markTriggered();
        reminderDao.update(reminder)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminders";
            String description = "Bill payments, SIP due dates, and other reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
