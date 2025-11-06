package com.example.csit314sdm;

import java.util.Random;

/**
 * The RegistrationController acts as the "Control" in the BCE pattern.
 * Its primary job is to orchestrate the user registration process.
 * It receives requests from the Boundary (the Activity) and delegates the data-handling
 * work to the Entity (the User class), applying any business logic in between.
 */
public class RegistrationController {


    public interface RegistrationCallback extends User.RegistrationCallback {}


    public RegistrationController() {}

    /**
     * This is the main method called by the UI (the Boundary) to start the user registration.
     * It contains the core business logic for this use case.
     * @param email The user's email.
     * @param password The user's chosen password.
     * @param role The role assigned to the new user (e.g., "Admin", "PIN", "CSR").
     * @param callback The callback to notify the UI of success or failure.
     */
    public void registerUser(String email, String password, String role, final RegistrationCallback callback) {
        if ("CSR".equals(role)) {
            String companyId = generateUniqueCompanyId();
            User.createUser(email, password, role, companyId, callback);
        } else {
            User.createUser(email, password, role, callback);
        }
    }

    /**
     * A private helper method that encapsulates the business rule for generating a company ID.
     * Currently, it generates a random 5-digit number.
     * @return A unique string representing the company ID.
     */
    private String generateUniqueCompanyId() {
        Random random = new Random();

        int fiveDigitNumber = 10000 + random.nextInt(90000);
        return String.valueOf(fiveDigitNumber);
    }
}
