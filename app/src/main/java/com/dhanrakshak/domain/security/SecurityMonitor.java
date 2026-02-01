package com.dhanrakshak.domain.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Security Monitor for checking data breaches and security recommendations.
 * Uses Have I Been Pwned API for breach detection.
 */
@Singleton
public class SecurityMonitor {

    private static final String TAG = "SecurityMonitor";
    private static final String HIBP_API_URL = "https://api.pwnedpasswords.com/range/";

    private final Context context;
    private final OkHttpClient httpClient;
    private final SharedPreferences prefs;

    @Inject
    public SecurityMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient();
        this.prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE);
    }

    /**
     * Check if email has been in known data breaches (simulation).
     * Note: HIBP API for emails requires paid API key.
     */
    public Single<BreachCheckResult> checkEmailBreaches(String email) {
        return Single.fromCallable(() -> {
            // In production, would use HIBP API with API key
            // For demo, we return a simulated result
            List<Breach> breaches = new ArrayList<>();

            // Simulated check based on email domain
            String domain = email.substring(email.indexOf('@') + 1);

            // Common breached services (for demo purposes)
            if (hasBeenInBreach(email)) {
                breaches.add(new Breach(
                        "LinkedIn",
                        "May 2016",
                        "Email addresses, Passwords",
                        "164 million accounts"));
            }

            return new BreachCheckResult(
                    email,
                    !breaches.isEmpty(),
                    breaches,
                    System.currentTimeMillis());
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Check if password has been exposed in breaches.
     * Uses k-anonymity model (only sends first 5 chars of SHA-1 hash).
     */
    public Single<PasswordCheckResult> checkPasswordSecurity(String password) {
        return Single.fromCallable(() -> {
            try {
                // SHA-1 hash the password
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] hashBytes = md.digest(password.getBytes());
                String hash = bytesToHex(hashBytes).toUpperCase();

                // k-anonymity: only send first 5 characters
                String prefix = hash.substring(0, 5);
                String suffix = hash.substring(5);

                // Query HIBP API
                Request request = new Request.Builder()
                        .url(HIBP_API_URL + prefix)
                        .header("User-Agent", "DhanRakshak-SecurityCheck")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();

                        // Check if our suffix appears in results
                        for (String line : responseBody.split("\n")) {
                            String[] parts = line.split(":");
                            if (parts.length == 2 && parts[0].equals(suffix)) {
                                int count = Integer.parseInt(parts[1].trim());
                                return new PasswordCheckResult(true, count,
                                        "Password found in " + count + " data breaches!");
                            }
                        }
                    }
                }

                return new PasswordCheckResult(false, 0, "Password not found in known breaches");

            } catch (Exception e) {
                Log.e(TAG, "Error checking password", e);
                return new PasswordCheckResult(false, 0, "Check failed: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Get security recommendations.
     */
    public List<SecurityRecommendation> getSecurityRecommendations() {
        List<SecurityRecommendation> recommendations = new ArrayList<>();

        // Check biometric status
        if (!prefs.getBoolean("biometric_enabled", false)) {
            recommendations.add(new SecurityRecommendation(
                    "🔐 Enable Biometric Lock",
                    "Add fingerprint or face unlock for extra security",
                    "HIGH",
                    "settings_biometric"));
        }

        // Check last backup
        long lastBackup = prefs.getLong("last_backup_time", 0);
        if (System.currentTimeMillis() - lastBackup > 7 * 24 * 60 * 60 * 1000) {
            recommendations.add(new SecurityRecommendation(
                    "💾 Backup Your Data",
                    "Last backup was more than 7 days ago",
                    "MEDIUM",
                    "settings_backup"));
        }

        // General security tips
        recommendations.add(new SecurityRecommendation(
                "📱 Keep App Updated",
                "Always use the latest version for security patches",
                "LOW",
                null));

        recommendations.add(new SecurityRecommendation(
                "🔑 Use Unique Passwords",
                "Don't reuse your banking passwords elsewhere",
                "HIGH",
                null));

        recommendations.add(new SecurityRecommendation(
                "⚠️ Be Aware of Phishing",
                "Never share OTP or passwords via SMS/email",
                "HIGH",
                null));

        return recommendations;
    }

    /**
     * Check UPI ID safety (basic validation).
     */
    public UpiSafetyCheck checkUpiIdSafety(String upiId) {
        List<String> warnings = new ArrayList<>();

        // Basic format check
        if (!upiId.contains("@")) {
            warnings.add("Invalid UPI ID format");
        }

        // Check for suspicious patterns
        String lowerId = upiId.toLowerCase();
        if (lowerId.contains("helpdesk") || lowerId.contains("support") ||
                lowerId.contains("refund") || lowerId.contains("cashback")) {
            warnings.add("Suspicious UPI ID - may be fraud attempt");
        }

        // Check for known suspicious handles
        String[] suspiciousHandles = { "ybl", "axl", "upi" }; // legitimate but often spoofed
        // Additional checks would go here

        boolean isSafe = warnings.isEmpty();
        return new UpiSafetyCheck(upiId, isSafe, warnings);
    }

    private boolean hasBeenInBreach(String email) {
        // Simplified check - in production, use actual HIBP API
        return email.hashCode() % 3 == 0; // Simulated 33% breach rate for demo
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Data classes
    public static class BreachCheckResult {
        public String email;
        public boolean hasBreaches;
        public List<Breach> breaches;
        public long checkedAt;

        public BreachCheckResult(String email, boolean hasBreaches,
                List<Breach> breaches, long checkedAt) {
            this.email = email;
            this.hasBreaches = hasBreaches;
            this.breaches = breaches;
            this.checkedAt = checkedAt;
        }
    }

    public static class Breach {
        public String name;
        public String date;
        public String dataExposed;
        public String accountsAffected;

        public Breach(String name, String date, String dataExposed, String accountsAffected) {
            this.name = name;
            this.date = date;
            this.dataExposed = dataExposed;
            this.accountsAffected = accountsAffected;
        }
    }

    public static class PasswordCheckResult {
        public boolean isCompromised;
        public int exposureCount;
        public String message;

        public PasswordCheckResult(boolean compromised, int count, String message) {
            this.isCompromised = compromised;
            this.exposureCount = count;
            this.message = message;
        }
    }

    public static class SecurityRecommendation {
        public String title;
        public String description;
        public String priority; // HIGH, MEDIUM, LOW
        public String actionId;

        public SecurityRecommendation(String title, String desc, String priority, String action) {
            this.title = title;
            this.description = desc;
            this.priority = priority;
            this.actionId = action;
        }
    }

    public static class UpiSafetyCheck {
        public String upiId;
        public boolean isSafe;
        public List<String> warnings;

        public UpiSafetyCheck(String upiId, boolean isSafe, List<String> warnings) {
            this.upiId = upiId;
            this.isSafe = isSafe;
            this.warnings = warnings;
        }
    }
}
