package com.dhanrakshak.domain.reminder;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.dhanrakshak.data.local.dao.BillReminderDao;
import com.dhanrakshak.data.local.dao.ReminderDao;
import com.dhanrakshak.data.local.entity.BillReminder;
import com.dhanrakshak.data.local.entity.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Calendar Sync Manager for syncing reminders and bills with device calendar.
 * Supports Google Calendar and local calendars.
 */
@Singleton
public class CalendarSyncManager {

    private static final String TAG = "CalendarSyncManager";

    private final Context context;
    private final ReminderDao reminderDao;
    private final BillReminderDao billReminderDao;

    @Inject
    public CalendarSyncManager(Context context, ReminderDao reminderDao,
            BillReminderDao billReminderDao) {
        this.context = context.getApplicationContext();
        this.reminderDao = reminderDao;
        this.billReminderDao = billReminderDao;
    }

    /**
     * Check if calendar permission is granted.
     */
    public boolean hasCalendarPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get available calendars on the device.
     */
    public List<CalendarInfo> getAvailableCalendars() {
        List<CalendarInfo> calendars = new ArrayList<>();

        if (!hasCalendarPermission())
            return calendars;

        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_COLOR
        };

        try (Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String account = cursor.getString(2);
                    String type = cursor.getString(3);
                    int color = cursor.getInt(4);

                    calendars.add(new CalendarInfo(id, name, account, type, color));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting calendars", e);
        }

        return calendars;
    }

    /**
     * Add reminder to device calendar.
     */
    public Single<String> addToCalendar(Reminder reminder, long calendarId) {
        return Single.fromCallable(() -> {
            if (!hasCalendarPermission()) {
                throw new SecurityException("Calendar permission not granted");
            }

            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
            values.put(CalendarContract.Events.TITLE, reminder.getTitle());
            values.put(CalendarContract.Events.DESCRIPTION,
                    reminder.getDescription() != null ? reminder.getDescription() : "Dhan-Rakshak Reminder");
            values.put(CalendarContract.Events.DTSTART, reminder.getReminderTime());
            values.put(CalendarContract.Events.DTEND, reminder.getReminderTime() + 30 * 60 * 1000); // 30 min
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            values.put(CalendarContract.Events.HAS_ALARM, 1);

            // Set recurrence if not one-time
            String rrule = getRecurrenceRule(reminder);
            if (rrule != null) {
                values.put(CalendarContract.Events.RRULE, rrule);
            }

            Uri uri = context.getContentResolver().insert(
                    CalendarContract.Events.CONTENT_URI, values);

            if (uri != null) {
                long eventId = ContentUris.parseId(uri);

                // Add reminder/alarm
                addCalendarReminder(eventId, reminder.getNotifyMinutesBefore());

                return String.valueOf(eventId);
            }

            throw new Exception("Failed to create calendar event");
        }).subscribeOn(Schedulers.io());
    }

    private void addCalendarReminder(long eventId, int minutesBefore) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, eventId);
        values.put(CalendarContract.Reminders.MINUTES, minutesBefore);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, values);
    }

    private String getRecurrenceRule(Reminder reminder) {
        switch (reminder.getFrequency()) {
            case "DAILY":
                return "FREQ=DAILY;INTERVAL=" + reminder.getRepeatInterval();
            case "WEEKLY":
                String days = reminder.getDaysOfWeek();
                if (days != null && !days.isEmpty()) {
                    return "FREQ=WEEKLY;INTERVAL=" + reminder.getRepeatInterval() + ";BYDAY=" + days;
                }
                return "FREQ=WEEKLY;INTERVAL=" + reminder.getRepeatInterval();
            case "MONTHLY":
                return "FREQ=MONTHLY;INTERVAL=" + reminder.getRepeatInterval() +
                        ";BYMONTHDAY=" + reminder.getDayOfMonth();
            case "YEARLY":
                return "FREQ=YEARLY;INTERVAL=" + reminder.getRepeatInterval();
            default:
                return null; // ONCE - no recurrence
        }
    }

    /**
     * Remove event from calendar.
     */
    public Completable removeFromCalendar(String eventId) {
        return Completable.fromAction(() -> {
            if (!hasCalendarPermission() || eventId == null)
                return;

            Uri deleteUri = ContentUris.withAppendedId(
                    CalendarContract.Events.CONTENT_URI, Long.parseLong(eventId));
            context.getContentResolver().delete(deleteUri, null, null);
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Sync all bills to calendar.
     */
    public Completable syncBillsToCalendar(long calendarId) {
        return billReminderDao.getActiveBills()
                .firstOrError()
                .flatMapCompletable(bills -> {
                    List<Completable> syncTasks = new ArrayList<>();
                    for (BillReminder bill : bills) {
                        syncTasks.add(createBillCalendarEvent(bill, calendarId));
                    }
                    return Completable.merge(syncTasks);
                });
    }

    private Completable createBillCalendarEvent(BillReminder bill, long calendarId) {
        Reminder reminder = new Reminder(
                "💳 " + bill.getName() + " Due",
                "BILL",
                bill.getNextDueDate(),
                bill.getFrequency());
        reminder.setDescription("Amount: ₹" + bill.getAmount() +
                (bill.isAutoPay() ? " (Auto-pay enabled)" : ""));
        reminder.setLinkedBillId(bill.getId());
        reminder.setNotifyBefore(true);
        reminder.setNotifyMinutesBefore(bill.getReminderDaysBefore() * 24 * 60);

        return addToCalendar(reminder, calendarId)
                .flatMapCompletable(eventId -> {
                    reminder.setCalendarEventId(eventId);
                    reminder.setSyncToCalendar(true);
                    return reminderDao.insert(reminder).ignoreElement();
                });
    }

    /**
     * Create weekly expense review reminder.
     */
    public Single<Long> createWeeklyExpenseReview(int dayOfWeek, int hour, int minute) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);

        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }

        Reminder reminder = new Reminder(
                "📊 Weekly Expense Review",
                "EXPENSE_REVIEW",
                cal.getTimeInMillis(),
                "WEEKLY");
        reminder.setDescription("Review your spending patterns and get AI insights");

        return reminderDao.insert(reminder);
    }

    /**
     * Create weekly insights notification.
     */
    public Single<Long> createWeeklyInsightsReminder(int dayOfWeek, int hour, int minute) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);

        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }

        Reminder reminder = new Reminder(
                "💡 Your Weekly Financial Insights",
                "INSIGHTS",
                cal.getTimeInMillis(),
                "WEEKLY");
        reminder.setDescription("AI-powered analysis of your finances ready!");

        return reminderDao.insert(reminder);
    }

    // Data classes
    public static class CalendarInfo {
        public long id;
        public String name;
        public String account;
        public String accountType;
        public int color;

        public CalendarInfo(long id, String name, String account, String type, int color) {
            this.id = id;
            this.name = name;
            this.account = account;
            this.accountType = type;
            this.color = color;
        }

        public boolean isGoogleCalendar() {
            return "com.google".equals(accountType);
        }
    }
}
