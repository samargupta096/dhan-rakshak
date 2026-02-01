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
import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.presentation.MainActivity;

import java.util.Locale;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltWorker
public class DailySummaryWorker extends RxWorker {

    private static final String CHANNEL_ID = "daily_summary_channel";
    private static final int NOTIFICATION_ID = 1001;

    private final AssetDao assetDao;
    private final BankAccountDao bankAccountDao;
    private final Context context;

    @AssistedInject
    public DailySummaryWorker(@Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            AssetDao assetDao,
            BankAccountDao bankAccountDao) {
        super(context, workerParams);
        this.context = context;
        this.assetDao = assetDao;
        this.bankAccountDao = bankAccountDao;
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        return Single.zip(
                assetDao.getTotalAssetsValue().firstOrError().onErrorReturnItem(0.0),
                bankAccountDao.getTotalBankBalance().firstOrError().onErrorReturnItem(0.0),
                (assets, bank) -> assets + bank).map(totalNetWorth -> {
                    sendNotification(totalNetWorth);
                    return Result.success();
                }).subscribeOn(Schedulers.io());
    }

    private void sendNotification(double netWorth) {
        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String contentText = String.format(Locale.US, "Your total net worth is updated: ₹%,.2f", netWorth);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this exists or use fallback
                .setContentTitle("Daily Financial Update")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context,
                        android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            } else {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        } catch (SecurityException e) {
            // Log error
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Updates";
            String description = "Daily notification about your net worth";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
