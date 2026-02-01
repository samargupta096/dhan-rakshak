package com.dhanrakshak.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Family Member entity for family finance management.
 */
@Entity(tableName = "family_members")
public class FamilyMember {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String relationship; // SELF, SPOUSE, PARENT, CHILD, SIBLING
    private String email;
    private String phone;

    private boolean isOwner; // Primary account owner
    private boolean canView; // Can view all data
    private boolean canEdit; // Can add/edit assets
    private boolean canDelete; // Can delete data

    private String linkedAssetIds; // Assets visible to this member
    private String linkedGoalIds; // Goals visible to this member

    private double contributionToNetWorth; // Their contribution amount

    private String avatarColor; // For display
    private long createdAt;
    private long updatedAt;

    public FamilyMember(String name, String relationship) {
        this.name = name;
        this.relationship = relationship;
        this.isOwner = false;
        this.canView = true;
        this.canEdit = false;
        this.canDelete = false;
        this.contributionToNetWorth = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();

        // Assign color based on relationship
        switch (relationship) {
            case "SELF":
                this.avatarColor = "#1E88E5";
                break;
            case "SPOUSE":
                this.avatarColor = "#E91E63";
                break;
            case "PARENT":
                this.avatarColor = "#4CAF50";
                break;
            case "CHILD":
                this.avatarColor = "#FF9800";
                break;
            default:
                this.avatarColor = "#9C27B0";
                break;
        }
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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public boolean isCanView() {
        return canView;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public String getLinkedAssetIds() {
        return linkedAssetIds;
    }

    public void setLinkedAssetIds(String linkedAssetIds) {
        this.linkedAssetIds = linkedAssetIds;
    }

    public String getLinkedGoalIds() {
        return linkedGoalIds;
    }

    public void setLinkedGoalIds(String linkedGoalIds) {
        this.linkedGoalIds = linkedGoalIds;
    }

    public double getContributionToNetWorth() {
        return contributionToNetWorth;
    }

    public void setContributionToNetWorth(double contributionToNetWorth) {
        this.contributionToNetWorth = contributionToNetWorth;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
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
    public String getInitials() {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + name.charAt(0)).toUpperCase();
    }
}
