package com.dhanrakshak.presentation.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dhanrakshak.core.FeatureManager;
import com.dhanrakshak.databinding.FragmentFeatureSettingsBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FeatureSettingsFragment extends Fragment {

        @Inject
        FeatureManager featureManager;

        private FragmentFeatureSettingsBinding binding;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
                binding = FragmentFeatureSettingsBinding.inflate(inflater, container, false);
                return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);

                binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

                setupSwitches();
        }

        private void setupSwitches() {
                binding.switchInvestments
                                .setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_INVESTMENTS));
                binding.switchInvestments.setOnCheckedChangeListener((buttonView, isChecked) -> featureManager
                                .setFeatureEnabled(FeatureManager.FEATURE_INVESTMENTS, isChecked));

                binding.switchHealth.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_HEALTH));
                binding.switchHealth.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager
                                                .setFeatureEnabled(FeatureManager.FEATURE_HEALTH, isChecked));

                binding.switchGifts.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_GIFTS));
                binding.switchGifts.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager
                                                .setFeatureEnabled(FeatureManager.FEATURE_GIFTS, isChecked));

                binding.switchTrips.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_TRIPS));
                binding.switchTrips.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager
                                                .setFeatureEnabled(FeatureManager.FEATURE_TRIPS, isChecked));

                binding.switchFamily.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_FAMILY));
                binding.switchFamily.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager
                                                .setFeatureEnabled(FeatureManager.FEATURE_FAMILY, isChecked));

                binding.switchScanner.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_SCANNER));
                binding.switchScanner.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager
                                                .setFeatureEnabled(FeatureManager.FEATURE_SCANNER, isChecked));

                binding.switchWork.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_WORK));
                binding.switchWork.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager.setFeatureEnabled(FeatureManager.FEATURE_WORK,
                                                isChecked));

                binding.switchTrips.setChecked(featureManager.isFeatureEnabled(FeatureManager.FEATURE_TRIPS));
                binding.switchTrips.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> featureManager
                                                .setFeatureEnabled(FeatureManager.FEATURE_TRIPS, isChecked));
        }
}
