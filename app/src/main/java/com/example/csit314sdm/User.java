package com.example.csit314sdm;

import com.google.firebase.firestore.PropertyName;
import java.util.Date;

public class User {

    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String dob;
    private String role;
    private String accountStatus;
    private String address;
    private String shortId;
    private Date creationDate; // FIX: Changed from long to Date

    // Public, no-argument constructor is REQUIRED
    public User() {}

    // --- Getters and Setters with PropertyName annotations ---

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @PropertyName("fullName")
    public String getFullName() { return fullName; }
    @PropertyName("fullName")
    public void setFullName(String fullName) { this.fullName = fullName; }

    @PropertyName("contactNumber") // FIX: Maps 'contactNumber' in DB to this field
    public String getPhoneNumber() { return phoneNumber; }
    @PropertyName("contactNumber")
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @PropertyName("dateOfBirth") // FIX: Maps 'dateOfBirth' in DB to this field
    public String getDob() { return dob; }
    @PropertyName("dateOfBirth")
    public void setDob(String dob) { this.dob = dob; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getShortId() { return shortId; }
    public void setShortId(String shortId) { this.shortId = shortId; }

    @PropertyName("creationDate") // FIX: Maps 'creationDate' or 'createdAt'
    public Date getCreationDate() { return creationDate; }
    @PropertyName("creationDate")
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    // Getter for old 'createdAt' field to avoid crashes with old data
    @PropertyName("createdAt")
    public void setCreatedAt(Date date) {
        if (this.creationDate == null) {
            this.creationDate = date;
        }
    }
}
