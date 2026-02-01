package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "family_events")
public class FamilyEvent {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public long date; // Start timestamp
    public long startTime; // Time of day in millis
    public long endTime; // Time of day in millis
    public String notes;
    public String membersInvolved; // Comma separated IDs or names (optional implementation)

    public FamilyEvent(String title, long date, long startTime, long endTime, String notes) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }
}
