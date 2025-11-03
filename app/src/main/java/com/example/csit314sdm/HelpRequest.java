package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class HelpRequest {

    private String id;
    private String title;
    private String description;
    private String location;
    private String submittedBy;
    private String pinId; // Retained for compatibility, but submittedBy is preferred
    private String pinName;
    private String pinShortId;
    private String requestType;
    private String preferredTime;
    private String urgencyLevel;
    private String status;
    private String category;
    private String organization;
    private String companyId;
    private String acceptedByCsrId;
    private Date shortlistedDate;
    private long viewCount = 0;
    private List<String> savedByCsrId;

    // --- START: NEW FIELD ---
    // This field is for temporary use in the app and is not saved in the help_requests collection.
    private String pinPhoneNumber;
    // --- END: NEW FIELD ---

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

    // --- START: NEW GETTER/SETTER ---
    public String getPinPhoneNumber() { return pinPhoneNumber; }
    public void setPinPhoneNumber(String pinPhoneNumber) { this.pinPhoneNumber = pinPhoneNumber; }
    // --- END: NEW GETTER/SETTER ---

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getAcceptedByCsrId() { return acceptedByCsrId; }
    public void setAcceptedByCsrId(String acceptedByCsrId) { this.acceptedByCsrId = acceptedByCsrId; }

    public Date getShortlistedDate() { return shortlistedDate; }
    public void setShortlistedDate(Date shortlistedDate) { this.shortlistedDate = shortlistedDate; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public List<String> getSavedByCsrId() { return savedByCsrId; }
    public void setSavedByCsrId(List<String> savedByCsrId) { this.savedByCsrId = savedByCsrId; }

    public Date getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Date creationTimestamp) { this.creationTimestamp = creationTimestamp; }
}
