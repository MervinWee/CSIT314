package com.example.csit314sdm;

import android.text.TextUtils;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;

// CONTROL: Handles the business logic for the forgot password feature.
public class ForgotPasswordController {

    private final FirebaseAuth mAuth;

    // Interface to communicate results back to the Boundary (Activity).
    public interface ForgotPasswordCallback {
        void onResetLinkSent();
        void onFailure(String errorMessage);
    }

    public ForgotPasswordController() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void sendPasswordResetEmail(String email, final ForgotPasswordCallback callback) {
        // --- Business Logic: Validation ---
        if (TextUtils.isEmpty(email)) {
            callback.onFailure("Email address cannot be empty.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onFailure("Please enter a valid email address.");
            return;
        }

        // --- Orchestration: Interact with Firebase ---
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Report success back to the caller.
                        callback.onResetLinkSent();
                    } else {
                        // Report failure back to the caller.
                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }
}
