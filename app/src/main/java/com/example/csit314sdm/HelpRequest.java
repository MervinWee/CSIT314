package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List; // This is the correct import for a Java List

// ENTITY: Represents a single help request.
public class HelpRequest {

    // FIX: Reverted all fields to standard Java types
    private String id;
    private String title;
    private String description;
    private String location;
    private String submittedBy;
    private String pinId;
    private String requestType;
    private String preferredTime;
    private String urgencyLevel;
    private String status;
    private String category;
    private String organization;
    private Date shortlistedDate;
    private long viewCount = 0;
    private List<String> savedByCsrId;

    @ServerTimestamp
    private Date creationTimestamp;

    // Default constructor is required for Firestore's toObject() mapping.
    public HelpRequest() {}

    // --- Getters and Setters for all fields (using standard Java types) ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public String getPinId() { return pinId; }
    public void setPinId(String pinId) { this.pinId = pinId; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Date creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public Date getShortlistedDate() { return shortlistedDate; }
    public void setShortlistedDate(Date shortlistedDate) { this.shortlistedDate = shortlistedDate; }

    public String getUrgency() { return urgencyLevel; }

    public Date getCreatedAt() { return creationTimestamp; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public List<String> getSavedByCsrId() { return savedByCsrId; }
    public void setSavedByCsrId(List<String> savedByCsrId) { this.savedByCsrId = savedByCsrId; }
}
