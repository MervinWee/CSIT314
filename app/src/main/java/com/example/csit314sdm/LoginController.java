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
    public interface LoginCallback {
        void onLoginSuccess(String userType);
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
            // User is already logged in, so fetch their user type from Firestore.
            fetchUserType(currentUser.getUid(), callback);
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
                            // After successful login, fetch the user type from Firestore.
                            fetchUserType(firebaseUser.getUid(), callback);
                        } else {
                            callback.onLoginFailure("Login successful, but failed to get user details.");
                        }
                    } else {
                        callback.onLoginFailure(task.getException().getMessage());
                    }
                });
    }

    /**
     * Fetches the user's document from Firestore to get their userType.
     */
    private void fetchUserType(String uid, final LoginCallback callback) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // You can use the User Entity here for better structure
                            User user = document.toObject(User.class);
                            if (user != null && user.getUserType() != null) {
                                callback.onLoginSuccess(user.getUserType());
                            } else {
                                callback.onLoginFailure("User data is incomplete. Please contact support.");
                                mAuth.signOut(); // Sign out user with corrupt data
                            }
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
