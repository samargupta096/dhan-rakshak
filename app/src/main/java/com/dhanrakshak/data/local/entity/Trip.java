package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Trip entity for travel planning and expense tracking.
 * Tracks journey with locations, budget, and all expenses.
 */
@Entity(tableName = "trips")
public class Trip {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String destination;
    private String description;
    private String coverImagePath; // Local path to trip cover photo

    // Trip dates
    private long startDate;
    private long endDate;
    private int durationDays;

    // Budget tracking
    private double plannedBudget;
    private double actualSpent;
    private String currency; // INR, USD, EUR, etc.

    // Trip status
    private String status; // PLANNING, ONGOING, COMPLETED, CANCELLED

    // Location tracking
    private String startLocation;
    private String startLocationCoords; // "lat,lng"
    private String endLocation;
    private String endLocationCoords;
    private String visitedLocations; // JSON array of locations with coords

    // Trip type
    private String tripType; // SOLO, COUPLE, FAMILY, FRIENDS, BUSINESS
    private int travelerCount;

    // Notes and extras
    private String notes;
    private String packingListJson; // JSON array of packing items
    private String documentsJson; // JSON array of document paths (tickets, bookings)

    private long createdAt;
    private long updatedAt;

    public Trip(String name, String destination, long startDate, long endDate, double plannedBudget) {
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.plannedBudget = plannedBudget;
        this.actualSpent = 0;
        this.currency = "INR";
        this.status = "PLANNING";
        this.tripType = "SOLO";
        this.travelerCount = 1;
        this.durationDays = (int) ((endDate - startDate) / (24 * 60 * 60 * 1000)) + 1;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public double getPlannedBudget() {
        return plannedBudget;
    }

    public void setPlannedBudget(double plannedBudget) {
        this.plannedBudget = plannedBudget;
    }

    public double getActualSpent() {
        return actualSpent;
    }

    public void setActualSpent(double actualSpent) {
        this.actualSpent = actualSpent;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getStartLocationCoords() {
        return startLocationCoords;
    }

    public void setStartLocationCoords(String startLocationCoords) {
        this.startLocationCoords = startLocationCoords;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getEndLocationCoords() {
        return endLocationCoords;
    }

    public void setEndLocationCoords(String endLocationCoords) {
        this.endLocationCoords = endLocationCoords;
    }

    public String getVisitedLocations() {
        return visitedLocations;
    }

    public void setVisitedLocations(String visitedLocations) {
        this.visitedLocations = visitedLocations;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public int getTravelerCount() {
        return travelerCount;
    }

    public void setTravelerCount(int travelerCount) {
        this.travelerCount = travelerCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPackingListJson() {
        return packingListJson;
    }

    public void setPackingListJson(String packingListJson) {
        this.packingListJson = packingListJson;
    }

    public String getDocumentsJson() {
        return documentsJson;
    }

    public void setDocumentsJson(String documentsJson) {
        this.documentsJson = documentsJson;
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
    public double getRemainingBudget() {
        return plannedBudget - actualSpent;
    }

    public double getBudgetUsagePercentage() {
        if (plannedBudget <= 0)
            return 0;
        return (actualSpent / plannedBudget) * 100;
    }

    public boolean isOverBudget() {
        return actualSpent > plannedBudget;
    }

    public double getDailyBudget() {
        if (durationDays <= 0)
            return plannedBudget;
        return plannedBudget / durationDays;
    }

    public double getDailySpent() {
        if (durationDays <= 0)
            return actualSpent;
        return actualSpent / durationDays;
    }

    public boolean isOngoing() {
        long now = System.currentTimeMillis();
        return now >= startDate && now <= endDate;
    }

    public boolean isUpcoming() {
        return System.currentTimeMillis() < startDate;
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status) || System.currentTimeMillis() > endDate;
    }

    public long getDaysUntilStart() {
        return Math.max(0, (startDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000));
    }

    public int getCurrentDay() {
        if (!isOngoing())
            return 0;
        return (int) ((System.currentTimeMillis() - startDate) / (24 * 60 * 60 * 1000)) + 1;
    }

    public double getPerPersonCost() {
        if (travelerCount <= 0)
            return actualSpent;
        return actualSpent / travelerCount;
    }
}
