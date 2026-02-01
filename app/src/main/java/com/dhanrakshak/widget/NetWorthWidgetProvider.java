package com.dhanrakshak.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.dhanrakshak.R;
import com.dhanrakshak.presentation.MainActivity;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Home screen widget showing net worth summary.
 */
public class NetWorthWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_REFRESH = "com.dhanrakshak.widget.REFRESH";

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_REFRESH.equals(intent.getAction())) {
            // Trigger widget update
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(
                    new android.content.ComponentName(context, NetWorthWidgetProvider.class));
            onUpdate(context, manager, ids);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_net_worth);

        // Set click action to open app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent);

        // Set refresh action
        Intent refreshIntent = new Intent(context, NetWorthWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent);

        // Load data using shared preferences (for quick access)
        android.content.SharedPreferences prefs = context.getSharedPreferences(
                "dhan_rakshak_widget", Context.MODE_PRIVATE);

        double netWorth = prefs.getFloat("net_worth", 0f);
        double profitLoss = prefs.getFloat("profit_loss", 0f);
        double profitLossPercent = prefs.getFloat("profit_loss_percent", 0f);

        // Update views
        views.setTextViewText(R.id.textNetWorth, currencyFormat.format(netWorth));

        String plPrefix = profitLoss >= 0 ? "+" : "";
        String plText = String.format("%s%s (%.2f%%)",
                plPrefix, currencyFormat.format(profitLoss), profitLossPercent);
        views.setTextViewText(R.id.textProfitLoss, plText);

        int plColor = profitLoss >= 0 ? context.getResources().getColor(R.color.credit_green, null)
                : context.getResources().getColor(R.color.debit_red, null);
        views.setTextColor(R.id.textProfitLoss, plColor);

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Update widget data from app.
     * Call this when net worth changes.
     */
    public static void updateWidgetData(Context context, double netWorth,
            double profitLoss, double profitLossPercent) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(
                "dhan_rakshak_widget", Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat("net_worth", (float) netWorth)
                .putFloat("profit_loss", (float) profitLoss)
                .putFloat("profit_loss_percent", (float) profitLossPercent)
                .apply();

        // Trigger widget update
        Intent intent = new Intent(context, NetWorthWidgetProvider.class);
        intent.setAction(ACTION_REFRESH);
        context.sendBroadcast(intent);
    }
}
