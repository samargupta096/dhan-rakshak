package com.dhanrakshak.core;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Manages feature toggles and first-run status.
 */
@Singleton
public class FeatureManager {

    private static final String PREF_NAME = "app_features";
    private static final String KEY_FIRST_RUN = "is_first_run";

    // Feature Keys
    public static final String FEATURE_INVESTMENTS = "feature_investments";
    public static final String FEATURE_HEALTH = "feature_health";
    public static final String FEATURE_GIFTS = "feature_gifts";
    public static final String FEATURE_TRIPS = "feature_trips";
    public static final String FEATURE_FAMILY = "feature_family";
    public static final String FEATURE_SCANNER = "feature_scanner";
    public static final String FEATURE_WORK = "feature_work";

    private final SharedPreferences prefs;

    @Inject
    public FeatureManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }

    public boolean isFeatureEnabled(String featureKey) {
        // Default to true for now, or false if we want opt-in
        return prefs.getBoolean(featureKey, true);
    }

    public void setFeatureEnabled(String featureKey, boolean enabled) {
        prefs.edit().putBoolean(featureKey, enabled).apply();
    }
}
