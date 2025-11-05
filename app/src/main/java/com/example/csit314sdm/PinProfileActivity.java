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


    private TextView tvUserId, tvFullName, tvDob, tvEmail, tvAddress, tvPhoneNumber;
    private Button btnEditProfile;
    private Button btnChangePassword;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_profile);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUserProfile();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_profile);
        topAppBar.setNavigationOnClickListener(v -> finish());


        tvUserId = findViewById(R.id.tvProfileUserId);
        tvFullName = findViewById(R.id.tvProfileFullName);
        tvDob = findViewById(R.id.tvProfileDob);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvAddress = findViewById(R.id.tvProfileAddress);
        tvPhoneNumber = findViewById(R.id.tvProfilePhoneNumber);


        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {

            Intent intent = new Intent(PinProfileActivity.this, EditPinProfileActivity.class);
            startActivity(intent);
        });


        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> {

            Intent intent = new Intent(PinProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            Toast.makeText(this, "Error: No user is logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        String email = currentUser.getEmail();
        tvEmail.setText(email != null ? email : "Not available");


        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {

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


    private void populateUI(User user) {

        tvUserId.setText(user.getShortId() != null ? user.getShortId() : "N/A");


        tvFullName.setText(user.getFullName() != null ? user.getFullName() : "Not available");
        tvDob.setText(user.getDob() != null ? user.getDob() : "Not available");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "Not available");
        tvPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not available");
    }
}
