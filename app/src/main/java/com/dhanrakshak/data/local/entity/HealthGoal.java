package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "health_goals")
public class HealthGoal {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String metricType; // e.g., "STEPS", "CALORIES", "SLEEP"
    public double targetValue; // e.g., 10000, 2500, 8 (hours)
    public String period; // "DAILY", "WEEKLY"
    public long lastUpdated;

    public HealthGoal(String metricType, double targetValue, String period, long lastUpdated) {
        this.metricType = metricType;
        this.targetValue = targetValue;
        this.period = period;
        this.lastUpdated = lastUpdated;
    }
}
