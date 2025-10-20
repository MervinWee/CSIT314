package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.FieldValue;
import java.util.Date;

// ENTITY: Represents the data model for a user. It's a simple data holder.
public class User {
    private String email;
    private String userType;

    // @ServerTimestamp tells Firestore to automatically set the server time when creating the object.
    @ServerTimestamp
    private Date createdAt;

    // Firestore requires a no-argument constructor to deserialize objects.
    public User() {}

    public User(String email, String userType) {
        this.email = email;
        this.userType = userType;
    }

    // --- Getters and Setters for all fields ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
