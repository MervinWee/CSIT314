package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Category {
    private String id;
    private String name;
    private String description;
    @ServerTimestamp
    private Date createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
