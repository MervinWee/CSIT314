package com.example.csit314sdm;

public class CreateUserAccountController {

    public void createUserAccount(String email, String password, String role, String fullName, String phoneNumber, String dob, String address, User.RegistrationCallback callback) {
        // This controller handles the 'Account' use case, but the underlying entity action is the same.
        User.createUser(email, password, role, null, fullName, phoneNumber, dob, address, callback);
    }
}
