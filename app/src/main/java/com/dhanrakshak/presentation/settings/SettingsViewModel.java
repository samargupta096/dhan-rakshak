package com.dhanrakshak.presentation.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.dhanrakshak.core.FeatureManager;
import com.dhanrakshak.data.local.db.DhanRakshakDatabase;
import com.dhanrakshak.data.repository.BackupRepository;
import com.dhanrakshak.data.repository.MutualFundRepository;
import com.dhanrakshak.data.repository.SyncRepository;
import com.dhanrakshak.worker.DataSyncWorker;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Settings.
 */
@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private static final String PREFS_NAME = "dhan_rakshak_prefs";
    private static final String KEY_BIOMETRIC = "biometric_enabled";

    private final Context context;
    private final MutualFundRepository mfRepository;
    private final SyncRepository syncRepository;
    private final SharedPreferences prefs;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> isBiometricEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(@ApplicationContext Context context, MutualFundRepository mfRepository,
            SyncRepository syncRepository) {
        this.context = context;
        this.mfRepository = mfRepository;
        this.syncRepository = syncRepository;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadSettings();
    }

    private void loadSettings() {
        isBiometricEnabled.setValue(prefs.getBoolean(KEY_BIOMETRIC, false));
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply();
        isBiometricEnabled.setValue(enabled);
        message.setValue(enabled ? "Biometric lock enabled" : "Biometric lock disabled");
    }

    public void backupToGoogleDrive() {
        isLoading.setValue(true);
        // TODO: Implement Google Drive backup
        // For now, simulate
        new android.os.Handler().postDelayed(() -> {
            isLoading.setValue(false);
            message.setValue("Backup feature coming soon! Your data is safely encrypted locally.");
        }, 1500);
    }

    public void restoreFromGoogleDrive() {
        isLoading.setValue(true);
        // TODO: Implement Google Drive restore
        new android.os.Handler().postDelayed(() -> {
            isLoading.setValue(false);
            message.setValue("Restore feature coming soon!");
        }, 1500);
    }

    public void syncMutualFundNav() {
        isLoading.setValue(true);
        disposables.add(
                mfRepository.syncNavData()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    message.setValue("NAV data synced successfully!");
                                },
                                error -> {
                                    isLoading.setValue(false);
                                    message.setValue("Sync failed: " + error.getMessage());
                                }));
    }

    public void exportDataToCsv() {
        isLoading.setValue(true);
        // TODO: Implement CSV export
        new android.os.Handler().postDelayed(() -> {
            isLoading.setValue(false);
            message.setValue("Export to CSV coming soon!");
        }, 1000);
    }

    public void clearAllData() {
        isLoading.setValue(true);
        // TODO: Implement data clearing
        new android.os.Handler().postDelayed(() -> {
            isLoading.setValue(false);
            message.setValue("Data cleared (functionality disabled for safety)");
        }, 1000);
    }

    public void syncToFirebase() {
        if (!syncRepository.isUserLoggedIn()) {
            // Signal UI to start Google Sign In
            message.setValue("SIGN_IN_REQUIRED");
            return;
        }

        isLoading.setValue(true);
        disposables.add(
                syncRepository.syncToCloud()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    message.setValue("Data synced to Firebase Cloud successfully!");
                                },
                                error -> {
                                    isLoading.setValue(false);
                                    message.setValue("Cloud Sync failed: " + error.getMessage());
                                }));
    }

    public boolean isUserLoggedIn() {
        return syncRepository.isUserLoggedIn();
    }

    public void signInWithGoogle(String idToken) {
        isLoading.setValue(true);
        disposables.add(
                syncRepository.signInWithGoogle(idToken)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    message.setValue("Signed in successfully as " +
                                            syncRepository.getCurrentUser().getDisplayName());
                                    // Trigger sync after login
                                    syncToFirebase();
                                },
                                error -> {
                                    isLoading.setValue(false);
                                    message.setValue("Sign in failed: " + error.getMessage());
                                }));
    }

    // Getters
    public LiveData<Boolean> getIsBiometricEnabled() {
        return isBiometricEnabled;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
