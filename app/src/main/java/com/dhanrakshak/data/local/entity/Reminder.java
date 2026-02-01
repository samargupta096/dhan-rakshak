package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Reminder entity for personal financial reminders.
 * Supports one-time and recurring reminders.
 */
@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;
    private String category; // BILL, INVESTMENT, GOAL, CUSTOM, EXPENSE_REVIEW, INSIGHTS

    // Timing
    private long reminderTime;
    private String frequency; // ONCE, DAILY, WEEKLY, MONTHLY, YEARLY
    private int repeatInterval; // e.g., every 2 weeks
    private String daysOfWeek; // For weekly: "MON,WED,FRI"
    private int dayOfMonth; // For monthly: 1-31

    // Notification settings
    private boolean isEnabled;
    private boolean notifyBefore;
    private int notifyMinutesBefore; // 15, 30, 60, 1440 (1 day)
    private String notificationSound;
    private boolean vibrate;

    // Linked items
    private Long linkedBillId;
    private Long linkedGoalId;
    private Long linkedLoanId;

    // Calendar sync
    private boolean syncToCalendar;
    private String calendarEventId; // Google Calendar event ID

    // Status
    private long lastTriggered;
    private long nextTriggerTime;
    private int snoozeCount;
    private boolean isCompleted;

    private long createdAt;
    private long updatedAt;

    public Reminder(String title, String category, long reminderTime, String frequency) {
        this.title = title;
        this.category = category;
        this.reminderTime = reminderTime;
        this.frequency = frequency;
        this.repeatInterval = 1;
        this.isEnabled = true;
        this.notifyBefore = false;
        this.notifyMinutesBefore = 30;
        this.vibrate = true;
        this.syncToCalendar = false;
        this.snoozeCount = 0;
        this.isCompleted = false;
        this.nextTriggerTime = reminderTime;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isNotifyBefore() {
        return notifyBefore;
    }

    public void setNotifyBefore(boolean notifyBefore) {
        this.notifyBefore = notifyBefore;
    }

    public int getNotifyMinutesBefore() {
        return notifyMinutesBefore;
    }

    public void setNotifyMinutesBefore(int notifyMinutesBefore) {
        this.notifyMinutesBefore = notifyMinutesBefore;
    }

    public String getNotificationSound() {
        return notificationSound;
    }

    public void setNotificationSound(String notificationSound) {
        this.notificationSound = notificationSound;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public Long getLinkedBillId() {
        return linkedBillId;
    }

    public void setLinkedBillId(Long linkedBillId) {
        this.linkedBillId = linkedBillId;
    }

    public Long getLinkedGoalId() {
        return linkedGoalId;
    }

    public void setLinkedGoalId(Long linkedGoalId) {
        this.linkedGoalId = linkedGoalId;
    }

    public Long getLinkedLoanId() {
        return linkedLoanId;
    }

    public void setLinkedLoanId(Long linkedLoanId) {
        this.linkedLoanId = linkedLoanId;
    }

    public boolean isSyncToCalendar() {
        return syncToCalendar;
    }

    public void setSyncToCalendar(boolean syncToCalendar) {
        this.syncToCalendar = syncToCalendar;
    }

    public String getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(String calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public long getLastTriggered() {
        return lastTriggered;
    }

    public void setLastTriggered(long lastTriggered) {
        this.lastTriggered = lastTriggered;
    }

    public long getNextTriggerTime() {
        return nextTriggerTime;
    }

    public void setNextTriggerTime(long nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }

    public int getSnoozeCount() {
        return snoozeCount;
    }

    public void setSnoozeCount(int snoozeCount) {
        this.snoozeCount = snoozeCount;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public void calculateNextTrigger() {
        long now = System.currentTimeMillis();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(reminderTime);

        while (cal.getTimeInMillis() <= now) {
            switch (frequency) {
                case "DAILY":
                    cal.add(java.util.Calendar.DAY_OF_YEAR, repeatInterval);
                    break;
                case "WEEKLY":
                    cal.add(java.util.Calendar.WEEK_OF_YEAR, repeatInterval);
                    break;
                case "MONTHLY":
                    cal.add(java.util.Calendar.MONTH, repeatInterval);
                    break;
                case "YEARLY":
                    cal.add(java.util.Calendar.YEAR, repeatInterval);
                    break;
                default:
                    // ONCE - no repeat
                    this.isCompleted = true;
                    return;
            }
        }
        this.nextTriggerTime = cal.getTimeInMillis();
    }

    public void snooze(int minutes) {
        this.nextTriggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L);
        this.snoozeCount++;
        this.updatedAt = System.currentTimeMillis();
    }

    public void markTriggered() {
        this.lastTriggered = System.currentTimeMillis();
        if (!"ONCE".equals(frequency)) {
            calculateNextTrigger();
        } else {
            this.isCompleted = true;
        }
        this.updatedAt = System.currentTimeMillis();
    }

    public String getCategoryIcon() {
        switch (category) {
            case "BILL":
                return "💳";
            case "INVESTMENT":
                return "📈";
            case "GOAL":
                return "🎯";
            case "EXPENSE_REVIEW":
                return "📊";
            case "INSIGHTS":
                return "💡";
            case "HEALTH":
                return "🏥";
            default:
                return "⏰";
        }
    }

    public boolean isDue() {
        return isEnabled && !isCompleted && nextTriggerTime <= System.currentTimeMillis();
    }

    public boolean isUpcoming(int withinMinutes) {
        long now = System.currentTimeMillis();
        long threshold = now + (withinMinutes * 60 * 1000L);
        return isEnabled && !isCompleted && nextTriggerTime > now && nextTriggerTime <= threshold;
    }
}
