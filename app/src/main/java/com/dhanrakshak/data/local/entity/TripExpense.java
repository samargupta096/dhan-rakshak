package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * TripExpense entity for tracking individual expenses during a trip.
 * Supports categories, location tagging, and receipt photos.
 */
@Entity(tableName = "trip_expenses", foreignKeys = @ForeignKey(entity = Trip.class, parentColumns = "id", childColumns = "tripId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("tripId"), @Index("category") })
public class TripExpense {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long tripId;

    private String description;
    private double amount;
    private String currency;
    private double amountInInr; // Converted amount for totals

    // Categories
    private String category; // TRANSPORT, ACCOMMODATION, FOOD, ACTIVITIES, SHOPPING, OTHER
    private String subcategory; // Flight, Hotel, Restaurant, etc.

    // Payment info
    private String paymentMethod; // CASH, CARD, UPI, OTHER
    private boolean isShared; // Split among travelers
    private int splitCount;

    // Location
    private String location;
    private String locationCoords; // "lat,lng"

    // Receipt
    private String receiptImagePath;
    private String vendorName;

    // Date and time
    private long expenseDate;
    private int tripDay; // Day 1, Day 2, etc.

    private String notes;
    private long createdAt;

    public TripExpense(long tripId, String description, double amount, String category) {
        this.tripId = tripId;
        this.description = description;
        this.amount = amount;
        this.amountInInr = amount; // Default: same currency
        this.category = category;
        this.currency = "INR";
        this.paymentMethod = "CASH";
        this.isShared = false;
        this.splitCount = 1;
        this.expenseDate = System.currentTimeMillis();
        this.tripDay = 1;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmountInInr() {
        return amountInInr;
    }

    public void setAmountInInr(double amountInInr) {
        this.amountInInr = amountInInr;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationCoords() {
        return locationCoords;
    }

    public void setLocationCoords(String locationCoords) {
        this.locationCoords = locationCoords;
    }

    public String getReceiptImagePath() {
        return receiptImagePath;
    }

    public void setReceiptImagePath(String receiptImagePath) {
        this.receiptImagePath = receiptImagePath;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public long getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(long expenseDate) {
        this.expenseDate = expenseDate;
    }

    public int getTripDay() {
        return tripDay;
    }

    public void setTripDay(int tripDay) {
        this.tripDay = tripDay;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public double getPerPersonAmount() {
        if (!isShared || splitCount <= 0)
            return amount;
        return amount / splitCount;
    }

    public String getCategoryIcon() {
        switch (category) {
            case "TRANSPORT":
                return "✈️";
            case "ACCOMMODATION":
                return "🏨";
            case "FOOD":
                return "🍽️";
            case "ACTIVITIES":
                return "🎭";
            case "SHOPPING":
                return "🛍️";
            default:
                return "💰";
        }
    }
}
