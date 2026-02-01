package com.dhanrakshak.presentation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dhanrakshak.R;
import com.dhanrakshak.databinding.ActivityMainBinding;
import com.dhanrakshak.presentation.dashboard.DashboardFragment;
import com.dhanrakshak.presentation.insights.InsightsFragment;
import com.dhanrakshak.presentation.portfolio.PortfolioFragment;
import com.dhanrakshak.presentation.settings.SettingsFragment;
import com.google.android.material.navigation.NavigationBarView;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main Activity with bottom navigation.
 * Hosts Dashboard, Portfolio, Insights, and Settings fragments.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @javax.inject.Inject
    com.dhanrakshak.core.FeatureManager featureManager;

    // SMS permission request
    private final ActivityResultLauncher<String[]> smsPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean receiveSms = result.get(Manifest.permission.RECEIVE_SMS);
                Boolean readSms = result.get(Manifest.permission.READ_SMS);

                if (Boolean.TRUE.equals(receiveSms) && Boolean.TRUE.equals(readSms)) {
                    Toast.makeText(this, "SMS permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            "SMS permissions required for automatic transaction tracking",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (featureManager.isFirstRun()) {
            startActivity(
                    new android.content.Intent(this, com.dhanrakshak.presentation.onboarding.OnboardingActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_portfolio, R.id.navigation_insights)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Hide Portfolio / Insights if disabled
        if (!featureManager.isFeatureEnabled(com.dhanrakshak.core.FeatureManager.FEATURE_INVESTMENTS)) {
            binding.navView.getMenu().findItem(R.id.navigation_portfolio).setVisible(false);
        }

        // NavigationUI.setupActionBarWithNavController(this, navController,
        // appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        // Request SMS permissions
        requestSmsPermissions();
    }

    /**
     * Setup bottom navigation bar.
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (itemId == R.id.nav_portfolio) {
                    fragment = new PortfolioFragment();
                } else if (itemId == R.id.nav_insights) {
                    fragment = new InsightsFragment();
                } else if (itemId == R.id.nav_settings) {
                    fragment = new SettingsFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Load fragment into container.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Request SMS permissions for transaction tracking.
     */
    private void requestSmsPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            smsPermissionLauncher.launch(new String[] {
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            });
        }

        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission granted
                    } else {
                        // Permission denied
                    }
                }).launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
