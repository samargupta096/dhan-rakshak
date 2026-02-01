package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "job_tasks")
public class JobTask {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;

    // Status: TODO, IN_PROGRESS, DONE, ARCHIVED
    private String status;

    // Priority: HIGH, MEDIUM, LOW
    private String priority;

    private long createdDate;
    private long deadlineDate;

    private double allocatedHours;

    // Calculated field not stored directly but we can update it via triggers or app
    // logic.
    // However, usually we calculate this from WorkLogs.
    // For performance, we can cache it here or in a view.
    // Let's keep it simple and calculate it in the ViewModel for now,
    // but storing a cached value helps with sorting.
    private double hoursSpent;

    public JobTask(String title, String description, long deadlineDate, double allocatedHours, String priority) {
        this.title = title;
        this.description = description;
        this.deadlineDate = deadlineDate;
        this.allocatedHours = allocatedHours;
        this.priority = priority;
        this.status = "TODO";
        this.createdDate = System.currentTimeMillis();
        this.hoursSpent = 0.0;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(long deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public double getAllocatedHours() {
        return allocatedHours;
    }

    public void setAllocatedHours(double allocatedHours) {
        this.allocatedHours = allocatedHours;
    }

    public double getHoursSpent() {
        return hoursSpent;
    }

    public void setHoursSpent(double hoursSpent) {
        this.hoursSpent = hoursSpent;
    }

    // Helpers
    public double getRemainingHours() {
        return Math.max(0, allocatedHours - hoursSpent);
    }

    public int getProgressPercentage() {
        if (allocatedHours <= 0)
            return 0;
        return (int) ((hoursSpent / allocatedHours) * 100);
    }
}
