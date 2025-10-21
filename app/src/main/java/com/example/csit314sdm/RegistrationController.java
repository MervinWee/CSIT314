package com.example.csit314sdm;

import android.content.Context; // You might need to pass context
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
import java.util.concurrent.Executor;

// CONTROL: Handles business logic for admin-level user registration.
public class RegistrationController {

    private final FirebaseFirestore db;
    // We will now handle the FirebaseAuth instance differently.

    public interface RegistrationCallback {
        void onRegistrationSuccess(String userType);
        void onRegistrationFailure(String errorMessage);
    }

    public RegistrationController() {
        // The main DB instance, used for saving user data after creation.
        // This still uses the currently logged-in admin's credentials.
        db = FirebaseFirestore.getInstance(FirebaseApp.getInstance());
    }

    // This is the main public method the Boundary will call.
    public void registerUser(String email, String password, String userType, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }

        // --- THE CORE FIX: Use a temporary, secondary Firebase App for user creation ---
        // This prevents the main app's authentication state from being disturbed.
        try {
            // Get the main app's options to create an identical, but separate, instance.
            FirebaseApp mainApp = FirebaseApp.getInstance();
            FirebaseOptions options = mainApp.getOptions();

            // Create a unique name for our temporary app instance.
            String secondaryAppName = "admin-user-creation";

            // Initialize the secondary app.
            FirebaseApp secondaryApp = FirebaseApp.initializeApp(mainApp.getApplicationContext(), options, secondaryAppName);

            // Get the FirebaseAuth instance from this new, separate app.
            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

            // Now, create the user using this sandboxed instance.
            secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                // Save the data using the main app's firestore instance (which is authenticated as admin).
                                saveUserDataToFirestore(firebaseUser, userType, callback);
                            } else {
                                callback.onRegistrationFailure("Secondary auth: Failed to get user after creation.");
                            }
                        } else {
                            callback.onRegistrationFailure("Secondary auth: " + task.getException().getMessage());
                        }

                        // IMPORTANT: Clean up the temporary app instance to free resources.
                        secondaryApp.delete();
                    });

        } catch (Exception e) {
            callback.onRegistrationFailure("Failed to initialize secondary Firebase app: " + e.getMessage());
        }
    }

    // This method remains the same and works correctly.
    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String userType, final RegistrationCallback callback) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("uid", uid);
        newUserMap.put("email", email);
        newUserMap.put("userType", userType);
        newUserMap.put("createdAt", FieldValue.serverTimestamp());
        newUserMap.put("accountStatus", "Active"); // Set default status on creation

        // This 'db' instance is still authenticated as the admin, so it has permission to write.
        db.collection("users").document(uid)
                .set(newUserMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onRegistrationSuccess(userType);
                    } else {
                        callback.onRegistrationFailure("Account created, but failed to save user data: " + task.getException().getMessage());
                    }
                });
    }

    // This validation logic is unchanged.
    private boolean isInputValid(String email, String password, RegistrationCallback callback) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || password.length() < 6) {
            callback.onRegistrationFailure("Invalid email or password (must be > 6 chars).");
            return false;
        }
        return true;
    }
}
