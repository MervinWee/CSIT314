package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PinProfileActivity extends AppCompatActivity {

    // --- UI Components ---
    private TextView tvUserId, tvFullName, tvDob, tvEmail, tvAddress, tvPhoneNumber;
    private Button btnEditProfile;
    private Button btnChangePassword;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load (or reload) data every time the user comes back to this screen.
        loadUserProfile();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_profile);
        topAppBar.setNavigationOnClickListener(v -> finish()); // Back button functionality

        // Find all TextViews from the layout
        tvUserId = findViewById(R.id.tvProfileUserId);
        tvFullName = findViewById(R.id.tvProfileFullName);
        tvDob = findViewById(R.id.tvProfileDob);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvAddress = findViewById(R.id.tvProfileAddress);
        tvPhoneNumber = findViewById(R.id.tvProfilePhoneNumber);

        // Find the "Edit Profile" button and set its click listener
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            // This opens the new EditPinProfileActivity when the button is clicked.
            Intent intent = new Intent(PinProfileActivity.this, EditPinProfileActivity.class);
            startActivity(intent);
        });

        // Find the "Change Password" button and set its listener
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> {
            // --- FIX: This now opens the new ChangePasswordActivity ---
            Intent intent = new Intent(PinProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // This is a safety check.
            Toast.makeText(this, "Error: No user is logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get Email directly from Firebase Auth
        String email = currentUser.getEmail();
        tvEmail.setText(email != null ? email : "Not available");

        // Get the rest of the user data from the "users" collection in Firestore
        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Map the Firestore document to your User object
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Populate the UI with data from the User object
                            populateUI(user);
                        }
                    } else {
                        Toast.makeText(this, "Could not find user profile data.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Populates all the TextViews with data from the User object.
     * @param user The User object fetched from Firestore.
     */
    private void populateUI(User user) {
        // Display the new 4-digit shortId instead of the long UID
        tvUserId.setText(user.getShortId() != null ? user.getShortId() : "N/A");

        // Set the rest of the fields, with checks for null to prevent crashes
        tvFullName.setText(user.getFullName() != null ? user.getFullName() : "Not available");
        tvDob.setText(user.getDob() != null ? user.getDob() : "Not available");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "Not available");
        tvPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not available");
    }
}
