package com.example.csit314sdm;

import java.util.Random;

public class RegistrationController {

    public interface RegistrationCallback extends User.RegistrationCallback {}

    public RegistrationController() {}

    public void registerUser(String email, String password, String role, final RegistrationCallback callback) {
        if ("CSR".equals(role)) {
            String companyId = generateUniqueCompanyId();
            User.createUser(email, password, role, companyId, callback);
        } else {
            User.createUser(email, password, role, callback);
        }
    }

    private String generateUniqueCompanyId() {
        Random random = new Random();
        // Generates a number between 10000 and 99999
        int fiveDigitNumber = 10000 + random.nextInt(90000);
        return String.valueOf(fiveDigitNumber);
    }
}
