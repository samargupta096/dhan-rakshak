package com.dhanrakshak.presentation.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dhanrakshak.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Settings Fragment with backup, security, and app settings.
 */
@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupClickListeners();
        observeData();
    }

    private void setupClickListeners() {
        // Backup & Sync
        binding.cardBackup.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Backup to Google Drive")
                    .setMessage(
                            "Your encrypted database will be backed up to your Google Drive. This requires signing in with your Google account.")
                    .setPositiveButton("Backup Now", (dialog, which) -> {
                        viewModel.backupToGoogleDrive();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.cardRestore.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Restore from Google Drive")
                    .setMessage("This will replace all current data with the backed up data. Are you sure?")
                    .setPositiveButton("Restore", (dialog, which) -> {
                        viewModel.restoreFromGoogleDrive();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Security
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                viewModel.setBiometricEnabled(isChecked);
            }
        });

        // Quick Access
        binding.cardPlanner.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.dhanrakshak.presentation.planner.PlannerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.cardFamily.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.dhanrakshak.presentation.family.FamilyFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.cardCalculators.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            new com.dhanrakshak.presentation.calculators.CalculatorsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Data Management
        binding.cardSyncNav.setOnClickListener(v -> {
            viewModel.syncMutualFundNav();
            Toast.makeText(requireContext(), "Syncing NAV data...", Toast.LENGTH_SHORT).show();
        });

        binding.cardFirebaseSync.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sync to Firebase Cloud")
                    .setMessage(
                            "This will upload all your assets to your secure Firebase Firestore account. Ensure you have internet content.")
                    .setPositiveButton("Sync Now", (dialog, which) -> {
                        viewModel.syncToFirebase();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.cardExportData.setOnClickListener(v -> {
            viewModel.exportDataToCsv();
        });

        binding.cardClearData.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Clear All Data")
                    .setMessage("This will permanently delete all your financial data. This action cannot be undone!")
                    .setPositiveButton("Delete All", (dialog, which) -> {
                        viewModel.clearAllData();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // About
        binding.cardAbout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Dhan-Rakshak")
                    .setMessage(
                            "Version 1.0.0\n\nPrivacy-first personal finance manager for India.\n\n• On-device AI for SMS parsing\n• Encrypted local storage\n• No cloud dependency\n\nMade with ❤️ for your financial freedom.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        binding.cardPrivacy.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Privacy Policy")
                    .setMessage(
                            "Dhan-Rakshak is designed with privacy as the core principle:\n\n• All data stored locally on your device\n• Database encrypted with SQLCipher\n• SMS parsed on-device using Gemini Nano AI\n• No data sent to external servers\n• Optional encrypted backup to YOUR Google Drive\n\nYour financial data stays with YOU.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void observeData() {
        viewModel.getIsBiometricEnabled().observe(getViewLifecycleOwner(), enabled -> {
            binding.switchBiometric.setChecked(enabled);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
            // Trigger Google Sign In if requested
            if ("SIGN_IN_REQUIRED".equals(message)) {
                launchGoogleSignIn();
            }
        });
    }

    private void launchGoogleSignIn() {
        // Configure Google Sign In
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.dhanrakshak.R.string.default_web_client_id))
                .requestEmail()
                .build();

        com.google.android.gms.auth.api.signin.GoogleSignInClient signInClient = com.google.android.gms.auth.api.signin.GoogleSignIn
                .getClient(requireActivity(), gso);

        googleSignInLauncher.launch(signInClient.getSignInIntent());
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    try {
                        com.google.android.gms.auth.api.signin.GoogleSignInAccount account = com.google.android.gms.auth.api.signin.GoogleSignIn
                                .getSignedInAccountFromIntent(
                                        result.getData())
                                .getResult(com.google.android.gms.common.api.ApiException.class);

                        if (account != null) {
                            viewModel.signInWithGoogle(account.getIdToken());
                        }
                    } catch (com.google.android.gms.common.api.ApiException e) {
                        Toast.makeText(requireContext(), "Google Sign In failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
