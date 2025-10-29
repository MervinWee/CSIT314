
package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class HelpRequest {

    private String id;
    private String pinId;
    private String requestType;
    private String description;
    private String location;
    private String preferredTime;
    private String urgencyLevel;
    private String status;
    @ServerTimestamp
    private Date creationTimestamp;

    // Public, no-argument constructor is REQUIRED for Firestore
    public HelpRequest() {}

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPinId() { return pinId; }
    public void setPinId(String pinId) { this.pinId = pinId; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Date creationTimestamp) { this.creationTimestamp = creationTimestamp; }
}
