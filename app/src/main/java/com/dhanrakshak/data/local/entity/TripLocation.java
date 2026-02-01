package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * TripLocation entity for tracking journey waypoints and visited places.
 * Stores GPS coordinates, timestamps, and photos for trip timeline.
 */
@Entity(tableName = "trip_locations", foreignKeys = @ForeignKey(entity = Trip.class, parentColumns = "id", childColumns = "tripId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("tripId") })
public class TripLocation {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long tripId;

    private String name; // Place name
    private String description;
    private String address;

    // Coordinates
    private double latitude;
    private double longitude;

    // Location type
    private String locationType; // START, WAYPOINT, ATTRACTION, HOTEL, RESTAURANT, END

    // Timing
    private long arrivalTime;
    private long departureTime;
    private int tripDay;
    private int sequenceOrder; // Order in the journey

    // Media
    private String photosJson; // JSON array of photo paths
    private String notes;

    // Rating
    private float rating; // 1-5 stars
    private boolean isHighlight; // Mark as trip highlight

    private long createdAt;

    public TripLocation(long tripId, String name, double latitude, double longitude) {
        this.tripId = tripId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationType = "WAYPOINT";
        this.tripDay = 1;
        this.sequenceOrder = 0;
        this.rating = 0;
        this.isHighlight = false;
        this.arrivalTime = System.currentTimeMillis();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public int getTripDay() {
        return tripDay;
    }

    public void setTripDay(int tripDay) {
        this.tripDay = tripDay;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public String getPhotosJson() {
        return photosJson;
    }

    public void setPhotosJson(String photosJson) {
        this.photosJson = photosJson;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isHighlight() {
        return isHighlight;
    }

    public void setHighlight(boolean highlight) {
        isHighlight = highlight;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public String getCoordinatesString() {
        return latitude + "," + longitude;
    }

    public long getDurationAtLocation() {
        if (departureTime <= 0 || departureTime <= arrivalTime)
            return 0;
        return departureTime - arrivalTime;
    }

    public String getDurationFormatted() {
        long duration = getDurationAtLocation();
        if (duration <= 0)
            return "—";

        long hours = duration / (60 * 60 * 1000);
        long minutes = (duration % (60 * 60 * 1000)) / (60 * 1000);

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + " min";
    }

    public String getLocationTypeIcon() {
        switch (locationType) {
            case "START":
                return "🚀";
            case "END":
                return "🏁";
            case "HOTEL":
                return "🏨";
            case "RESTAURANT":
                return "🍽️";
            case "ATTRACTION":
                return "📍";
            default:
                return "📌";
        }
    }

    /**
     * Calculate distance to another location in kilometers.
     */
    public double distanceTo(TripLocation other) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
