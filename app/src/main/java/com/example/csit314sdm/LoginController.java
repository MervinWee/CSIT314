package com.example.csit314sdm;

import android.text.TextUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

// CONTROL: Handles all business logic for the login process.
public class LoginController {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    // Interface to communicate results back to the Boundary (loginPage).
    // I've renamed the parameter for clarity, but the interface itself is unchanged.
    public interface LoginCallback {
        void onLoginSuccess(String userRole);
        void onLoginFailure(String errorMessage);
    }

    public LoginController() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Checks if a user is already signed in. If so, fetches their data.
     */
    public void checkForExistingSession(final LoginCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, so fetch their role from Firestore.
            fetchUserRole(currentUser.getUid(), callback);
        }
        // If no user is logged in, do nothing. The UI will just wait for input.
    }

    /**
     * Attempts to sign in a user with email and password.
     */
    public void loginUser(String email, String password, final LoginCallback callback) {
        // --- Business Logic: Validation ---
        if (TextUtils.isEmpty(email)) {
            callback.onLoginFailure("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            callback.onLoginFailure("Password is required.");
            return;
        }

        // --- Orchestration: Interact with Firebase Auth ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // After successful login, fetch the user role from Firestore.
                            fetchUserRole(firebaseUser.getUid(), callback);
                        } else {
                            callback.onLoginFailure("Login successful, but failed to get user details.");
                        }
                    } else {
                        callback.onLoginFailure(task.getException().getMessage());
                    }
                });
    }

    /**
     * Fetches the user's document from Firestore to get their role.
     * (Formerly fetchUserType)
     */
    private void fetchUserRole(String uid, final LoginCallback callback) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Use the User Entity to deserialize the document.
                            User user = document.toObject(User.class);

                            // --- THIS IS THE KEY CHANGE ---
                            // Instead of user.getUserType(), we now use user.getRole().
                            if (user != null && user.getRole() != null) {
                                callback.onLoginSuccess(user.getRole());
                            } else {
                                callback.onLoginFailure("User data is incomplete (role not found). Please contact support.");
                                mAuth.signOut(); // Sign out user with corrupt data
                            }
                            // -----------------------------

                        } else {
                            // This is a critical error: user exists in Auth but not in Firestore.
                            callback.onLoginFailure("User data not found. Please re-register or contact support.");
                            mAuth.signOut(); // Sign out for security
                        }
                    } else {
                        callback.onLoginFailure("Failed to fetch user data. Please try again.");
                    }
                });
    }
}
