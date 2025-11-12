package com.example.csit314sdm.controller;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChangePasswordController {

    private final FirebaseAuth mAuth;

    public interface ChangePasswordCallback {
        void onChangeSuccess();
        void onChangeFailure(String errorMessage);
    }

    public ChangePasswordController() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void updateUserPassword(String currentPassword, String newPassword, String confirmPassword, final ChangePasswordCallback callback) {
        // 1. Basic Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            callback.onChangeFailure("All fields are required.");
            return;
        }
        if (newPassword.length() < 6) {
            callback.onChangeFailure("New password must be at least 6 characters long.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            callback.onChangeFailure("New passwords do not match.");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onChangeFailure("No user is currently logged in.");
            return;
        }

        // 2. Re-authenticate the user to ensure they know their current password. This is a crucial security step.
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                // 3. If re-authentication is successful, proceed to update the password.
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onChangeSuccess(); // Password updated successfully
                    } else {
                        callback.onChangeFailure("Failed to update password: " + updateTask.getException().getMessage());
                    }
                });
            } else {
                callback.onChangeFailure("Authentication failed. Please check your current password.");
            }
        });
    }
}
