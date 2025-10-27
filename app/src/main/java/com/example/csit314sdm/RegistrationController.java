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
import java.util.concurrent.Executor;

public class RegistrationController {

    private final FirebaseFirestore db;

    public interface RegistrationCallback {
        // FIX 1: Rename parameter for clarity
        void onRegistrationSuccess(String role);
        void onRegistrationFailure(String errorMessage);
    }

    public RegistrationController() {
        db = FirebaseFirestore.getInstance(FirebaseApp.getInstance());
    }

    // FIX 2: Rename parameter for clarity
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
                        secondaryApp.delete();
                    });

        } catch (Exception e) {
            callback.onRegistrationFailure("Failed to initialize secondary Firebase app: " + e.getMessage());
        }
    }


    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String role, final RegistrationCallback callback) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("uid", uid);
        newUserMap.put("email", email);


        newUserMap.put("role", role);

        newUserMap.put("createdAt", FieldValue.serverTimestamp());
        newUserMap.put("accountStatus", "Active");

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

    private boolean isInputValid(String email, String password, RegistrationCallback callback) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || password.length() < 6) {
            callback.onRegistrationFailure("Invalid email or password (must be > 6 chars).");
            return false;
        }
        return true;
    }
}
