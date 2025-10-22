package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

// ENTITY: Represents the data model for a single help request.
public class HelpRequest {

    private String id; // Firestore document ID
    private String title;
    private String description;
    private String organization; // e.g., "Seed to Branch Company"
    private String category;     // e.g., "Medical Transport"
    private String location;     // e.g., "Anywhere"
    private String urgency;      // e.g., "High Urgency", "Medium", "Low"
    private String status;       // e.g., "New", "In Progress", "Resolved"
    private String submittedBy;  // UID of the user who submitted it
    private String savedByCsrId; // UID of the CSR who saved/shortlisted this request

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date shortlistedDate; // NEW: To track when it was shortlisted

    // Firestore requires a no-argument constructor
    public HelpRequest() {}

    // --- Getters and Setters for all fields ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public String getSavedByCsrId() { return savedByCsrId; }
    public void setSavedByCsrId(String savedByCsrId) { this.savedByCsrId = savedByCsrId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getShortlistedDate() { return shortlistedDate; }
    public void setShortlistedDate(Date shortlistedDate) { this.shortlistedDate = shortlistedDate; }
}
