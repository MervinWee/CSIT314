package com.example.csit314sdm;

import android.text.TextUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegistrationController {

    private final FirebaseFirestore db;

    // Callback interface remains the same
    public interface RegistrationCallback {
        void onRegistrationSuccess(String role);
        void onRegistrationFailure(String errorMessage);
    }

    public RegistrationController() {
        // No need to pass FirebaseApp.getInstance() here, just getInstance() is fine.
        db = FirebaseFirestore.getInstance();
    }

    // --- FIX: This is the NEW overloaded method for creating CSRs with a company ID ---
    public void registerUser(String email, String password, String role, String companyId, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }
        // This single method now handles the secondary app creation logic
        createUserWithSecondaryAuth(email, password, role, companyId, callback);
    }

    // This is the ORIGINAL method, now simplified to call the new one with a null companyId
    public void registerUser(String email, String password, String role, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }
        // For non-CSR users, we just pass null for the companyId
        createUserWithSecondaryAuth(email, password, role, null, callback);
    }

    // --- Centralized user creation logic ---
    private void createUserWithSecondaryAuth(String email, String password, String role, String companyId, final RegistrationCallback callback) {
        try {
            FirebaseApp mainApp = FirebaseApp.getInstance();
            // Use a unique name for the temporary app to avoid crashes
            String secondaryAppName = "user-creation-instance-" + System.currentTimeMillis();
            FirebaseApp secondaryApp = FirebaseApp.initializeApp(mainApp.getApplicationContext(), mainApp.getOptions(), secondaryAppName);
            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

            secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                // Pass all user data to the save method
                                saveUserDataToFirestore(firebaseUser, role, companyId, callback);
                            } else {
                                callback.onRegistrationFailure("Secondary auth: Failed to get user after creation.");
                            }
                        } else {
                            callback.onRegistrationFailure("Secondary auth: " + task.getException().getMessage());
                        }
                        // Always clean up the temporary app instance
                        secondaryApp.delete();
                    });
        } catch (Exception e) {
            callback.onRegistrationFailure("Failed to initialize secondary Firebase app: " + e.getMessage());
        }
    }


    // --- FIX: NEW overloaded version of saveUserDataToFirestore that handles companyId ---
    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String role, String companyId, final RegistrationCallback callback) {
        generateUniqueShortId(new UniqueIdCallback() {
            @Override
            public void onUniqueIdFound(String shortId) {
                String uid = firebaseUser.getUid();
                String email = firebaseUser.getEmail();

                Map<String, Object> newUserMap = new HashMap<>();
                newUserMap.put("email", email);
                newUserMap.put("role", role);
                newUserMap.put("creationDate", FieldValue.serverTimestamp());
                newUserMap.put("accountStatus", "Active");
                newUserMap.put("shortId", shortId);

                // Initialize other fields to prevent null errors later
                newUserMap.put("fullName", "");
                newUserMap.put("dateOfBirth", ""); // Renamed from dob for consistency
                newUserMap.put("address", "");
                newUserMap.put("phoneNumber", "");

                // *** FIX: Add the companyId to the map ONLY if it's a CSR user ***
                if (companyId != null && !companyId.isEmpty()) {
                    newUserMap.put("companyId", companyId);
                }

                db.collection("users").document(uid)
                        .set(newUserMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onRegistrationSuccess(role);
                            } else {
                                callback.onRegistrationFailure("Account created, but failed to save user data: " + task.getException().getMessage());
                            }
                        });
            }

            @Override
            public void onIdGenerationFailed(String errorMessage) {
                callback.onRegistrationFailure(errorMessage);
            }
        });
    }

    private boolean isInputValid(String email, String password, RegistrationCallback callback) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onRegistrationFailure("Invalid email address.");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            callback.onRegistrationFailure("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    // Helper method and interface for generating the unique 4-digit ID
    private void generateUniqueShortId(final UniqueIdCallback callback) {
        int randomId = new Random().nextInt(9000) + 1000;
        String shortId = String.valueOf(randomId);

        db.collection("users").whereEqualTo("shortId", shortId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            callback.onUniqueIdFound(shortId);
                        } else {
                            generateUniqueShortId(callback); // ID exists, try again
                        }
                    } else {
                        callback.onIdGenerationFailed("Database error while checking for unique ID.");
                    }
                });
    }

    private interface UniqueIdCallback {
        void onUniqueIdFound(String shortId);
        void onIdGenerationFailed(String errorMessage);
    }
}
