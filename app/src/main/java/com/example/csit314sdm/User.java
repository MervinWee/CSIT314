// File: app/src/main/java/com/example/csit314sdm/User.java
// RESTORED to the standard, stable structure.

package com.example.csit314sdm;

public class User {

    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String dob;
    private String role;
    private String accountStatus;

    // Public, no-argument constructor is REQUIRED
    public User() {}

    // --- Getters and Setters ---

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
