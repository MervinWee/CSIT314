package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.User;

public class CreateUserAccountController {

    public void createUserAccount(String email, String password, String role, String fullName, String phoneNumber, String dob, String address, User.RegistrationCallback callback) {
        // This controller handles the 'Account' use case, but the underlying entity action is the same.
        User.createUser(email, password, role, null, fullName, phoneNumber, dob, address, callback);
    }
}
