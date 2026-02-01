package com.dhanrakshak.domain.upi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.UpiAccountDao;
import com.dhanrakshak.data.local.entity.UpiAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * UPI Manager for handling UPI payments, balance checks, and transaction
 * tracking.
 * Integrates with GPay, PhonePe, Paytm, BHIM, etc.
 */
@Singleton
public class UpiManager {

    private static final String TAG = "UpiManager";

    private final Context context;
    private final UpiAccountDao upiAccountDao;
    private final BankAccountDao bankAccountDao;

    // UPI Handle to Bank mapping
    private static final Map<String, String> UPI_HANDLE_BANKS = new HashMap<String, String>() {
        {
            // Google Pay
            put("@okaxis", "Axis Bank");
            put("@okhdfcbank", "HDFC Bank");
            put("@okicici", "ICICI Bank");
            put("@oksbi", "SBI");

            // PhonePe
            put("@ybl", "Yes Bank");
            put("@ibl", "ICICI Bank");
            put("@axl", "Axis Bank");

            // Paytm
            put("@paytm", "Paytm Payments Bank");

            // BHIM
            put("@upi", "Various Banks");

            // Amazon Pay
            put("@apl", "Axis Bank");
            put("@rapl", "RBL Bank");

            // WhatsApp
            put("@waicici", "ICICI Bank");
            put("@wahdfcbank", "HDFC Bank");
            put("@wasbi", "SBI");
            put("@waaxis", "Axis Bank");
        }
    };

    // SMS patterns for balance extraction
    private static final Pattern[] BALANCE_PATTERNS = {
            Pattern.compile("(?i)a\\/c\\s*(?:balance|bal)[:\\s]*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d*)"),
            Pattern.compile("(?i)available\\s*(?:balance|bal)[:\\s]*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d*)"),
            Pattern.compile("(?i)(?:balance|bal)\\s*(?:is)?[:\\s]*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d*)"),
            Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)\\s*(?:is\\s*)?(?:available|bal)"),
            Pattern.compile("(?i)avl\\.?\\s*bal[:\\s]*(?:rs\\.?)?\\s*([\\d,]+\\.?\\d*)")
    };

    @Inject
    public UpiManager(Context context, UpiAccountDao upiAccountDao, BankAccountDao bankAccountDao) {
        this.context = context.getApplicationContext();
        this.upiAccountDao = upiAccountDao;
        this.bankAccountDao = bankAccountDao;
    }

    /**
     * Add a new UPI account.
     */
    public Completable addUpiAccount(String upiId, String upiApp) {
        String bank = detectBankFromUpiId(upiId);
        UpiAccount account = new UpiAccount(upiId, bank, upiApp);
        return upiAccountDao.insert(account);
    }

    /**
     * Detect bank from UPI handle.
     */
    public String detectBankFromUpiId(String upiId) {
        if (upiId == null)
            return "Unknown Bank";
        String handle = upiId.substring(upiId.indexOf("@")).toLowerCase();
        return UPI_HANDLE_BANKS.getOrDefault(handle, "Unknown Bank");
    }

    /**
     * Get installed UPI apps on device.
     */
    public List<UpiAppInfo> getInstalledUpiApps() {
        List<UpiAppInfo> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        // Create UPI intent to find UPI-capable apps
        Intent upiIntent = new Intent(Intent.ACTION_VIEW);
        upiIntent.setData(Uri.parse("upi://pay"));

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(upiIntent, 0);

        for (ResolveInfo info : resolveInfos) {
            String packageName = info.activityInfo.packageName;
            String appName = info.loadLabel(pm).toString();
            String appCode = getAppCodeFromPackage(packageName);

            if (appCode != null) {
                apps.add(new UpiAppInfo(appCode, appName, packageName));
            }
        }

        return apps;
    }

    private String getAppCodeFromPackage(String packageName) {
        switch (packageName) {
            case "com.google.android.apps.nbu.paisa.user":
                return "GPAY";
            case "com.phonepe.app":
                return "PHONEPE";
            case "net.one97.paytm":
                return "PAYTM";
            case "in.org.npci.upiapp":
                return "BHIM";
            case "in.amazon.mShop.android.shopping":
                return "AMAZONPAY";
            case "com.whatsapp":
                return "WHATSAPP";
            default:
                return null;
        }
    }

    /**
     * Launch UPI app to check balance.
     * Note: Balance check requires opening the UPI app - cannot be done via API.
     */
    public Intent createBalanceCheckIntent(UpiAccount account) {
        String packageName = account.getUpiAppPackage();
        if (packageName == null) {
            // Fallback: Open generic UPI deeplink
            return new Intent(Intent.ACTION_VIEW, Uri.parse("upi://"));
        }

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    /**
     * Create UPI payment intent.
     */
    public Intent createPaymentIntent(String payeeUpiId, String payeeName,
            double amount, String note) {
        Uri uri = Uri.parse("upi://pay")
                .buildUpon()
                .appendQueryParameter("pa", payeeUpiId) // Payee address
                .appendQueryParameter("pn", payeeName) // Payee name
                .appendQueryParameter("am", String.valueOf(amount)) // Amount
                .appendQueryParameter("cu", "INR") // Currency
                .appendQueryParameter("tn", note) // Transaction note
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Parse balance from SMS after balance check.
     */
    public Double parseBalanceFromSms(String smsBody) {
        for (Pattern pattern : BALANCE_PATTERNS) {
            Matcher matcher = pattern.matcher(smsBody);
            if (matcher.find()) {
                String balanceStr = matcher.group(1).replace(",", "");
                try {
                    return Double.parseDouble(balanceStr);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Failed to parse balance: " + balanceStr);
                }
            }
        }
        return null;
    }

    /**
     * Update balance for a UPI account from SMS.
     */
    public Completable updateBalanceFromSms(long accountId, String smsBody) {
        Double balance = parseBalanceFromSms(smsBody);
        if (balance != null) {
            return upiAccountDao.updateBalance(accountId, balance, System.currentTimeMillis());
        }
        return Completable.complete();
    }

    /**
     * Validate UPI ID format.
     */
    public boolean isValidUpiId(String upiId) {
        if (upiId == null || !upiId.contains("@"))
            return false;

        // Basic UPI ID format: username@handle
        String regex = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$";
        return upiId.matches(regex);
    }

    /**
     * Check if UPI ID might be suspicious (fraud prevention).
     */
    public UpiSafetyResult checkUpiIdSafety(String upiId) {
        List<String> warnings = new ArrayList<>();

        if (!isValidUpiId(upiId)) {
            warnings.add("Invalid UPI ID format");
            return new UpiSafetyResult(false, warnings, "INVALID");
        }

        String lowerUpi = upiId.toLowerCase();

        // Check for suspicious keywords
        String[] suspiciousKeywords = {
                "helpdesk", "support", "refund", "cashback", "lottery",
                "prize", "winner", "claim", "kyc", "update"
        };

        for (String keyword : suspiciousKeywords) {
            if (lowerUpi.contains(keyword)) {
                warnings.add("Suspicious keyword detected: " + keyword);
            }
        }

        // Check for number-heavy UPI IDs (often fake)
        String username = lowerUpi.split("@")[0];
        int digitCount = 0;
        for (char c : username.toCharArray()) {
            if (Character.isDigit(c))
                digitCount++;
        }
        if (digitCount > username.length() * 0.7) {
            warnings.add("Unusually many digits in UPI ID");
        }

        if (warnings.isEmpty()) {
            return new UpiSafetyResult(true, warnings, "SAFE");
        } else if (warnings.size() > 2) {
            return new UpiSafetyResult(false, warnings, "DANGEROUS");
        } else {
            return new UpiSafetyResult(true, warnings, "WARNING");
        }
    }

    /**
     * Get total balance across all UPI accounts.
     */
    public Single<Double> getTotalUpiBalance() {
        return upiAccountDao.getTotalBalance()
                .onErrorReturnItem(0.0)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Set account as primary.
     */
    public Completable setPrimaryAccount(long accountId) {
        return upiAccountDao.clearAllPrimary()
                .andThen(upiAccountDao.setPrimary(accountId));
    }

    /**
     * Record a UPI transaction usage.
     */
    public Completable recordUsage(long accountId, double amount) {
        return upiAccountDao.getById(accountId)
                .flatMapCompletable(account -> {
                    account.incrementUsage(amount);
                    return upiAccountDao.update(account);
                });
    }

    // Data classes
    public static class UpiAppInfo {
        public String code;
        public String name;
        public String packageName;

        public UpiAppInfo(String code, String name, String packageName) {
            this.code = code;
            this.name = name;
            this.packageName = packageName;
        }
    }

    public static class UpiSafetyResult {
        public boolean isSafe;
        public List<String> warnings;
        public String riskLevel; // SAFE, WARNING, DANGEROUS, INVALID

        public UpiSafetyResult(boolean isSafe, List<String> warnings, String risk) {
            this.isSafe = isSafe;
            this.warnings = warnings;
            this.riskLevel = risk;
        }
    }
}
