package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CsrProfileActivity extends AppCompatActivity {

    private static final String TAG = "PROFILE_DEBUG"; // Tag for logging

    private RetrieveUserAccountController retrieveUserAccountController;
    private TextView tvCsrId, tvCsrName, tvCsrEmail;
    private Button btnEditProfile, btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_profile);

        retrieveUserAccountController = new RetrieveUserAccountController();

        MaterialToolbar toolbar = findViewById(R.id.toolbarCsrProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCsrId = findViewById(R.id.tvCsrId);
        tvCsrName = findViewById(R.id.tvCsrName);
        tvCsrEmail = findViewById(R.id.tvCsrEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CsrProfileActivity.this, EditCsrProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(CsrProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCsrProfile();
    }

    private void loadCsrProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d(TAG, "Fetching profile for UID: " + uid); // Log the UID

            retrieveUserAccountController.fetchUserById(uid, new RetrieveUserAccountController.UserCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    // This block is now for logging the result to see what we get
                    if (user == null) {
                        Log.e(TAG, "onSuccess was called, but the User object is NULL.");
                    } else {
                        Log.d(TAG, "onSuccess: User object received.");
                        Log.d(TAG, "  - Full Name: " + user.getFullName());
                        Log.d(TAG, "  - Email: " + user.getEmail());
                        Log.d(TAG, "  - Short ID: " + user.getShortId());
                        Log.d(TAG, "  - DOB: " + user.getDob());
                        Log.d(TAG, "  - Phone: " + user.getPhoneNumber());
                    }

                    // This block updates the UI
                    runOnUiThread(() -> {
                        if (user != null) {
                            tvCsrId.setText(user.getShortId() != null ? user.getShortId() : "N/A");
                            tvCsrName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
                            tvCsrEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                        } else {
                            Toast.makeText(CsrProfileActivity.this, "User data is null.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "onFailure: Failed to load profile.", e); // Log the full exception
                    runOnUiThread(() -> {
                        Toast.makeText(CsrProfileActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Log.e(TAG, "Cannot load profile: FirebaseUser is null.");
        }
    }
}