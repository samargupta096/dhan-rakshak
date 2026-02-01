package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity for tracking health parameters (e.g., Blood Sugar, HbA1c).
 */
@Entity(tableName = "health_metrics", indices = { @Index("metricName"), @Index("timestamp") })
public class HealthMetric {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String metricName; // e.g., "HbA1c", "Fasting Sugar", "Cholesterol"
    private double value;
    private String unit; // e.g., "%", "mg/dL"
    private long timestamp;

    // Reference Range for UI highlighting
    private double referenceRangeLow;
    private double referenceRangeHigh;

    private String notes;

    public HealthMetric(String metricName, double value, String unit, long timestamp) {
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getReferenceRangeLow() {
        return referenceRangeLow;
    }

    public void setReferenceRangeLow(double referenceRangeLow) {
        this.referenceRangeLow = referenceRangeLow;
    }

    public double getReferenceRangeHigh() {
        return referenceRangeHigh;
    }

    public void setReferenceRangeHigh(double referenceRangeHigh) {
        this.referenceRangeHigh = referenceRangeHigh;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
