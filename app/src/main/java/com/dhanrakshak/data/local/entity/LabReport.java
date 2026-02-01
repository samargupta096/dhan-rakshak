package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for storing references to Lab Reports (PDFs/Images).
 */
@Entity(tableName = "lab_reports")
public class LabReport {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title; // e.g., "Annual Checkup"
    private String filePath; // Local URI/Path
    private String fileType; // "PDF", "IMAGE"
    private long timestamp;

    private String labName;
    private String doctorName;

    private String notes;

    public LabReport(String title, String filePath, String fileType, long timestamp) {
        this.title = title;
        this.filePath = filePath;
        this.fileType = fileType;
        this.timestamp = timestamp;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
