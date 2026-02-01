package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "work_logs", foreignKeys = @ForeignKey(entity = JobTask.class, parentColumns = "id", childColumns = "taskId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("taskId"), @Index("date") })
public class WorkLog {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;
    private long date;
    private double hoursLogged;
    private String notes;

    public WorkLog(long taskId, long date, double hoursLogged, String notes) {
        this.taskId = taskId;
        this.date = date;
        this.hoursLogged = hoursLogged;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getHoursLogged() {
        return hoursLogged;
    }

    public void setHoursLogged(double hoursLogged) {
        this.hoursLogged = hoursLogged;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
