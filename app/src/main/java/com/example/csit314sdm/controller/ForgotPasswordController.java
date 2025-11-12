package com.example.csit314sdm.controller;

import android.text.TextUtils;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;


public class ForgotPasswordController {

    private final FirebaseAuth mAuth;


    public interface ForgotPasswordCallback {
        void onResetLinkSent();
        void onFailure(String errorMessage);
    }

    public ForgotPasswordController() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void sendPasswordResetEmail(String email, final ForgotPasswordCallback callback) {

        if (TextUtils.isEmpty(email)) {
            callback.onFailure("Email address cannot be empty.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onFailure("Please enter a valid email address.");
            return;
        }


        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        callback.onResetLinkSent();
                    } else {

                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }
}
