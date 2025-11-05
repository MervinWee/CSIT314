package com.example.csit314sdm;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;

public class User {


    private String id;
    private String email;

    private String uid;
    private String fullName;
    private String phoneNumber;
    private String dob;
    private String role;
    private String accountStatus;
    private String address;
    private String shortId;
    private Date creationDate;
    private String companyId;


    public User() {}


    @Exclude
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    // --- Other Getters and Setters ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @PropertyName("fullName")
    public String getFullName() { return fullName; }
    @PropertyName("fullName")
    public void setFullName(String fullName) { this.fullName = fullName; }

    @PropertyName("phoneNumber")
    public String getPhoneNumber() { return phoneNumber; }
    @PropertyName("phoneNumber")
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @PropertyName("dob")
    public String getDob() { return dob; }
    @PropertyName("dob")
    public void setDob(String dob) { this.dob = dob; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getShortId() { return shortId; }
    public void setShortId(String shortId) { this.shortId = shortId; }

    @PropertyName("creationDate")
    public Date getCreationDate() { return creationDate; }
    @PropertyName("creationDate")
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    @PropertyName("companyId")
    public String getCompanyId() { return companyId; }
    @PropertyName("companyId")
    public void setCompanyId(String companyId) { this.companyId = companyId; }


    @PropertyName("createdAt")
    public void setCreatedAt(Date date) {
        if (this.creationDate == null) {
            this.creationDate = date;
        }
    }

}
