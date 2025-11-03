package com.example.csit314sdm;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

// ENTITY: Represents a single help request.
public class HelpRequest {

    // --- Field Declarations ---
    private String id;
    private String title;
    private String description;
    private String location;
    private String submittedBy;
    private String pinId;
    private String pinName;
    private String pinShortId; // Unique ID for the PIN
    private String requestType;
    private String preferredTime;
    private String urgencyLevel;
    private String status;
    private String category;
    private String organization;
    private String companyId;
    private Date shortlistedDate;
    private long viewCount = 0;
    private List<String> savedByCsrId;
    private String notes;
    private String phoneNumber;

    @ServerTimestamp
    private Date creationTimestamp;

    public HelpRequest() {}

    // --- Getters and Setters ---

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

    public String getPinName() { return pinName; }
    public void setPinName(String pinName) { this.pinName = pinName; }

    public String getPinShortId() { return pinShortId; }
    public void setPinShortId(String pinShortId) { this.pinShortId = pinShortId; }

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

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public Date getShortlistedDate() { return shortlistedDate; }
    public void setShortlistedDate(Date shortlistedDate) { this.shortlistedDate = shortlistedDate; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public List<String> getSavedByCsrId() { return savedByCsrId; }
    public void setSavedByCsrId(List<String> savedByCsrId) { this.savedByCsrId = savedByCsrId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @PropertyName("savedBy")
    public void setSavedBy(List<String> savedByList) {
        if (this.savedByCsrId == null) {
            this.savedByCsrId = savedByList;
        }
    }
}
