package com.example.csit314sdm;

import java.util.Random;

public class CreateUserAccountController {

    public CreateUserAccountController() {}

    public void CreateUser(String email, String password, String role, String fullName, String phoneNumber, String dob, String address, final User.RegistrationCallback callback) {
        if ("CSR".equals(role)) {
            String companyId = generateUniqueCompanyId();
            User.createUser(email, password, role, companyId, fullName, phoneNumber, dob, address, callback);
        } else {
            User.createUser(email, password, role, null, fullName, phoneNumber, dob, address, callback);
        }
    }

    private String generateUniqueCompanyId() {
        Random random = new Random();
        int fiveDigitNumber = 10000 + random.nextInt(90000);
        return String.valueOf(fiveDigitNumber);
    }
}
