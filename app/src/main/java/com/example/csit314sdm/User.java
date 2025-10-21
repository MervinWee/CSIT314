package com.example.csit314sdm;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.FieldValue;
import java.util.Date;

// ENTITY: Represents the data model for a user. It's a simple data holder.
public class User {
    private String uid; // Store the UID for easy reference
    private String email;
    private String userType;

    private String accountStatus; // Can be "Active", "Suspended", etc.

    // Profile Info
    private String fullName;
    private String contactNumber;
    private String dateOfBirth;
    private String address;

    // @ServerTimestamp tells Firestore to automatically set the server time when creating the object.
    @ServerTimestamp
    private Date createdAt;

    // Firestore requires a no-argument constructor to deserialize objects.
    public User() {
        this.accountStatus = "Active"; // Default to active
    }

    // Constructor for initial account creation
    public User(String uid, String email, String userType) {
        this.uid = uid;
        this.email = email;
        this.userType = userType;
        this.accountStatus = "Active";
    }

    // --- Getters and Setters for all fields ---

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // This is useful for the Spinner to display the user's email
    // In User.java
    @Override
    public String toString() {
        // If the email is null, return a placeholder to avoid crashes
        if (this.email == null) {
            return "Invalid User";
        }
        return this.email;
    }

}