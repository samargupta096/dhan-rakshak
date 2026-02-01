package com.dhanrakshak.presentation.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dhanrakshak.core.FeatureManager;
import com.dhanrakshak.databinding.ActivityOnboardingBinding;
import com.dhanrakshak.presentation.MainActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OnboardingActivity extends AppCompatActivity {

    @Inject
    FeatureManager featureManager;

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnGetStarted.setOnClickListener(v -> savePreferencesAndProceed());
    }

    private void savePreferencesAndProceed() {
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_INVESTMENTS, binding.checkInvestments.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_HEALTH, binding.checkHealth.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_GIFTS, binding.checkGifts.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_TRIPS, binding.checkTrips.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_FAMILY, binding.checkFamily.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_SCANNER, binding.checkScanner.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_WORK, binding.checkWork.isChecked());
        featureManager.setFeatureEnabled(FeatureManager.FEATURE_TRIPS, binding.checkTrips.isChecked());

        featureManager.setFirstRunCompleted();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
