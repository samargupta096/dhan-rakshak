package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity for tracking gifts given or received.
 */
@Entity(tableName = "gift_transactions", indices = { @Index("personName"), @Index("date") })
public class GiftTransaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String type; // "GIVEN" or "RECEIVED"
    private String personName;
    private String occasion; // e.g., "Wedding", "birthday", "Diwali"
    private String description; // e.g., "Cash", "Watch", "Gift Card"
    private double value;
    private long date;
    private boolean isCash; // true if cash/transfer, false if object

    public GiftTransaction(String type, String personName, String occasion, double value, long date) {
        this.type = type;
        this.personName = personName;
        this.occasion = occasion;
        this.value = value;
        this.date = date;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isCash() {
        return isCash;
    }

    public void setCash(boolean cash) {
        isCash = cash;
    }
}
