package com.example.csit314sdm;

public class CreateUserProfileController {

    public void createUserProfile(String email, String password, String role, String fullName, String phoneNumber, String dob, String address, User.RegistrationCallback callback) {
        // Pass null for the companyId to match the User.createUser method signature
        User.createUser(email, password, role, null, fullName, phoneNumber, dob, address, callback);
    }
}
