package com.example.csit314sdm;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

// CONTROL: Handles the business logic for registration. It is independent of the UI.
public class RegistrationController {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    // An interface to communicate the results back to the Boundary (the Activity).
    public interface RegistrationCallback {
        void onRegistrationSuccess(String userType);
        void onRegistrationFailure(String errorMessage);
    }

    public RegistrationController() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // This is the main public method the Boundary will call.
    public void registerUser(String email, String password, String userType, final RegistrationCallback callback) {
        // --- Business Logic: Validation ---
        if (!isInputValid(email, password, callback)) {
            return;
        }

        // --- Orchestration: Interact with external services ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserDataToFirestore(firebaseUser, userType, callback);
                        } else {
                            callback.onRegistrationFailure("Failed to get user after creation.");
                        }
                    } else {
                        callback.onRegistrationFailure(task.getException().getMessage());
                    }
                });
    }

    // Private helper for validation logic.
    private boolean isInputValid(String email, String password, RegistrationCallback callback) {
        if (TextUtils.isEmpty(email)) {
            callback.onRegistrationFailure("Email is required.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            callback.onRegistrationFailure("Password is required.");
            return false;
        }
        if (password.length() < 6) {
            callback.onRegistrationFailure("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    // Private helper to save data to Firestore.
    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String userType, final RegistrationCallback callback) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        // Use the User Entity object we created.
        User userEntity = new User(email, userType);

        db.collection("users").document(uid)
                .set(userEntity)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Report success back to the caller.
                        callback.onRegistrationSuccess(userType);
                    } else {
                        // Report failure back to the caller.
                        callback.onRegistrationFailure("Account created, but failed to save user data.");
                    }
                });
    }
}
