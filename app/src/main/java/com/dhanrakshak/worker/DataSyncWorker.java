package com.dhanrakshak.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.dhanrakshak.data.repository.SyncRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.rxjava3.core.Single;

@HiltWorker
public class DataSyncWorker extends RxWorker {

    private static final String TAG = "DataSyncWorker";
    private final SyncRepository syncRepository;

    @AssistedInject
    public DataSyncWorker(@Assisted @NonNull Context appContext,
            @Assisted @NonNull WorkerParameters workerParams,
            SyncRepository syncRepository) {
        super(appContext, workerParams);
        this.syncRepository = syncRepository;
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        Log.d(TAG, "Starting Cloud Sync Work");

        if (!syncRepository.isUserLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping sync.");
            return Single.just(Result.success());
        }

        return syncRepository.syncToCloud()
                .toSingleDefault(Result.success())
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Sync failed", throwable);
                    if (getRunAttemptCount() < 3) {
                        return Result.retry();
                    }
                    return Result.failure();
                });
    }
}
