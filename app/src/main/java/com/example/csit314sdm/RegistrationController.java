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
            User.createUser(email, password, role, null, callback);
        }
    }

    public void registerUser(String email, String password, String role, String fullName, String phoneNumber, String dob, String address, final RegistrationCallback callback) {
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
