package com.example.csit314sdm;

import android.text.TextUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginController {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;


    public interface LoginCallback {
        void onLoginSuccess(String userRole);
        void onLoginFailure(String errorMessage);
    }

    public LoginController() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }


    public void checkForExistingSession(final LoginCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser.getUid(), callback);
        }
    }


    public void loginUser(String email, String password, final LoginCallback callback) {
        if (TextUtils.isEmpty(email)) {
            callback.onLoginFailure("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            callback.onLoginFailure("Password is required.");
            return;
        }

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


    private void fetchUserRole(String uid, final LoginCallback callback) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            User user = document.toObject(User.class);


                            if (user != null && user.getRole() != null) {
                                callback.onLoginSuccess(user.getRole());
                            } else {
                                callback.onLoginFailure("User data is incomplete (role not found). Please contact support.");
                                mAuth.signOut();
                            }


                        } else {

                            callback.onLoginFailure("User data not found. Please re-register or contact support.");
                            mAuth.signOut(); // Sign out for security
                        }
                    } else {
                        callback.onLoginFailure("Failed to fetch user data. Please try again.");
                    }
                });
    }
}
