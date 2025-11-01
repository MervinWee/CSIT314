package com.example.csit314sdm;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random; // <-- FIX: Import for Random
import java.util.concurrent.Executor;

public class RegistrationController {

    private final FirebaseFirestore db;

    public interface RegistrationCallback {
        void onRegistrationSuccess(String role);
        void onRegistrationFailure(String errorMessage);
    }

    public RegistrationController() {
        db = FirebaseFirestore.getInstance(FirebaseApp.getInstance());
    }

    public void registerUser(String email, String password, String role, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }

        try {
            FirebaseApp mainApp = FirebaseApp.getInstance();
            FirebaseOptions options = mainApp.getOptions();
            String secondaryAppName = "admin-user-creation";
            FirebaseApp secondaryApp = FirebaseApp.initializeApp(mainApp.getApplicationContext(), options, secondaryAppName);
            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

            secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                // Pass the 'role' variable to the next method
                                saveUserDataToFirestore(firebaseUser, role, callback);
                            } else {
                                callback.onRegistrationFailure("Secondary auth: Failed to get user after creation.");
                            }
                        } else {
                            callback.onRegistrationFailure("Secondary auth: " + task.getException().getMessage());
                        }
                        // Clean up the secondary app instance
                        secondaryApp.delete();
                    });

        } catch (Exception e) {
            callback.onRegistrationFailure("Failed to initialize secondary Firebase app: " + e.getMessage());
        }
    }


    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String role, final RegistrationCallback callback) {
        // --- FIX: The logic to generate a unique ID is now integrated here ---
        generateUniqueShortId(new UniqueIdCallback() {
            @Override
            public void onUniqueIdFound(String shortId) {
                // This code runs only after a unique shortId has been confirmed.
                String uid = firebaseUser.getUid();
                String email = firebaseUser.getEmail();

                Map<String, Object> newUserMap = new HashMap<>();
                newUserMap.put("uid", uid);
                newUserMap.put("email", email);
                newUserMap.put("role", role);
                newUserMap.put("creationDate", FieldValue.serverTimestamp()); // <-- FIX: Changed 'createdAt' to 'creationDate'
                newUserMap.put("accountStatus", "Active");

                // --- FIX: Add the new unique shortId to the user data ---
                newUserMap.put("shortId", shortId);
                // Initialize other fields from your User.java model to prevent null errors later
                newUserMap.put("fullName", "");
                newUserMap.put("dob", "");
                newUserMap.put("address", "");
                newUserMap.put("phoneNumber", "");

                db.collection("users").document(uid)
                        .set(newUserMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Pass back the 'role' variable on success
                                callback.onRegistrationSuccess(role);
                            } else {
                                callback.onRegistrationFailure("Account created, but failed to save user data: " + task.getException().getMessage());
                            }
                        });
            }

            @Override
            public void onIdGenerationFailed(String errorMessage) {
                // If we couldn't generate an ID, fail the whole registration.
                callback.onRegistrationFailure(errorMessage);
            }
        });
    }

    private boolean isInputValid(String email, String password, RegistrationCallback callback) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || password.length() < 6) {
            callback.onRegistrationFailure("Invalid email or password (must be > 6 chars).");
            return false;
        }
        return true;
    }

    // --- FIX: New helper method and interface for generating the unique ID ---
    /**
     * Recursively generates a 4-digit ID and checks for uniqueness in the database.
     */
    private void generateUniqueShortId(final UniqueIdCallback callback) {
        // Generate a random number between 1000 and 9999
        int randomId = new Random().nextInt(9000) + 1000;
        String shortId = String.valueOf(randomId);

        // Check if a user document with this shortId already exists
        db.collection("users").whereEqualTo("shortId", shortId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // The ID is unique! Pass it back via the callback.
                            callback.onUniqueIdFound(shortId);
                        } else {
                            // The ID is already taken. Try again by calling the method recursively.
                            generateUniqueShortId(callback);
                        }
                    } else {
                        // An error occurred while checking the database.
                        callback.onIdGenerationFailed("Database error while checking for unique ID.");
                    }
                });
    }

    // A callback for the asynchronous ID generation process.
    private interface UniqueIdCallback {
        void onUniqueIdFound(String shortId);
        void onIdGenerationFailed(String errorMessage);
    }
}
