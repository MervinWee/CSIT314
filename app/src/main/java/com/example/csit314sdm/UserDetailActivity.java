package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    private UserProfileController controller;
    private ProgressBar progressBar;
    private ScrollView contentScrollView;
    private String currentUserId;

    private TextView tvDetailStatus;
    private Button btnToggleSuspend;
    private User currentUser;
    private boolean isEditMode = false;

    // UI Elements
    private EditText etDetailFullName, etDetailContact;
    private TextView tvDetailEmail;

    private TextView tvActivityTitle;
    private Spinner spinnerDetailRole;
    private Button btnToggleEdit, btnUpdateProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        controller = new UserProfileController();
        initializeUI();
        setupListeners();

        String mode = getIntent().getStringExtra("MODE");

        if ("AdminMode".equals(mode)) {
            configureForAdmin();
        } else {
            configureForUserProfile();
        }

        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId != null) {
            loadUserData(currentUserId);
        } else {
            Toast.makeText(this, "Error: No User ID provided.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeUI() {
        progressBar = findViewById(R.id.progressBar);
        contentScrollView = findViewById(R.id.contentScrollView);

        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        btnToggleSuspend = findViewById(R.id.btnToggleSuspend);

        etDetailFullName = findViewById(R.id.etDetailFullName);
        etDetailContact = findViewById(R.id.etDetailContact);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        spinnerDetailRole = findViewById(R.id.spinnerDetailRole);

        tvActivityTitle = findViewById(R.id.tv_user_detail_title);

        btnToggleEdit = findViewById(R.id.btnToggleEdit);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"PIN", "Admin", "CSR"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDetailRole.setAdapter(adapter);
    }

    private void configureForAdmin() {
        tvActivityTitle.setText("User Account Details");
        // Ensure admin buttons are visible (the suspend button is handled in populateUI)
    }

    private void configureForUserProfile() {
        tvActivityTitle.setText("User Profile Details");
        // Hide admin-only functionality
        if (btnToggleSuspend != null) {
            btnToggleSuspend.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnToggleEdit.setOnClickListener(v -> toggleEditMode());
        btnUpdateProfile.setOnClickListener(v -> handleUpdateProfile());
        btnToggleSuspend.setOnClickListener(v -> handleToggleSuspend());
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        etDetailFullName.setEnabled(isEditMode);
        etDetailContact.setEnabled(isEditMode);
        spinnerDetailRole.setEnabled(isEditMode);

        if (isEditMode) {
            btnToggleEdit.setText("Cancel");
            btnUpdateProfile.setVisibility(View.VISIBLE);
            // Change background to show fields are editable
            etDetailFullName.setBackgroundResource(android.R.drawable.edit_text);
            etDetailContact.setBackgroundResource(android.R.drawable.edit_text);
        } else {
            btnToggleEdit.setText("Edit");
            btnUpdateProfile.setVisibility(View.GONE);
            // Revert to view-only look
            etDetailFullName.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            etDetailContact.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            // Restore original data if changes were cancelled
            populateUI(currentUser);
        }
    }

    private void handleUpdateProfile() {
        // Collect data from UI
        String newFullName = etDetailFullName.getText().toString().trim();
        String newContact = etDetailContact.getText().toString().trim();
        String newRole = spinnerDetailRole.getSelectedItem().toString();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("fullName", newFullName);
        updatedData.put("contactNumber", newContact);
        // --- FIX 2: Save to the 'role' field instead of 'userType' ---
        updatedData.put("role", newRole);

        progressBar.setVisibility(View.VISIBLE);

        controller.updateUserProfile(currentUserId, updatedData, new UserProfileController.ProfileUpdateCallback() {
            @Override
            public void onProfileUpdateSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                // Exit edit mode after successful update
                toggleEditMode();
                // Refresh data to be sure
                loadUserData(currentUserId);
            }

            @Override
            public void onProfileUpdateFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleToggleSuspend() {
        if (currentUser == null) return;

        String currentStatus = currentUser.getAccountStatus();
        String newStatus = "Active".equals(currentStatus) ? "Suspended" : "Active";
        String action = "Active".equals(currentStatus) ? "suspend" : "reinstate";

        // Confirmation Dialog
        new AlertDialog.Builder(this)
                .setTitle(Character.toUpperCase(action.charAt(0)) + action.substring(1) + " User")
                .setMessage("Are you sure you want to " + action + " this user's account?")
                .setPositiveButton("Yes, " + Character.toUpperCase(action.charAt(0)) + action.substring(1), (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    controller.updateUserAccountStatus(currentUserId, newStatus, new UserProfileController.ProfileUpdateCallback() {
                        @Override
                        public void onProfileUpdateSuccess() {
                            Toast.makeText(UserDetailActivity.this, "User " + newStatus + " successfully.", Toast.LENGTH_SHORT).show();
                            loadUserData(currentUserId); // Reload to show new status
                        }

                        @Override
                        public void onProfileUpdateFailure(String errorMessage) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UserDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void loadUserData(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        contentScrollView.setVisibility(View.GONE);
        btnToggleEdit.setVisibility(View.GONE);

        controller.getUserById(userId, new UserProfileController.UserLoadCallback() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user; // Save the loaded user
                progressBar.setVisibility(View.GONE);
                contentScrollView.setVisibility(View.VISIBLE);
                btnToggleEdit.setVisibility(View.VISIBLE);
                btnToggleSuspend.setVisibility(View.VISIBLE);
                populateUI(user);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void populateUI(User user) {
        if (user == null) return;

        etDetailFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        etDetailContact.setText(user.getContactNumber() != null ? user.getContactNumber() : "");
        tvDetailEmail.setText(user.getEmail());

        String status = user.getAccountStatus();
        MaterialButton suspendButton = (MaterialButton) btnToggleSuspend;

        if (status != null && !status.isEmpty()) {
            tvDetailStatus.setText(status);

            if ("Suspended".equals(status)) {
                tvDetailStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                suspendButton.setText("Reinstate User");
                suspendButton.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary));
                suspendButton.setStrokeColorResource(com.google.android.material.R.color.design_default_color_primary);

            } else {
                tvDetailStatus.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary));
                suspendButton.setText("Suspend User");
                suspendButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                suspendButton.setStrokeColorResource(android.R.color.holo_red_dark);
            }
        } else {
            tvDetailStatus.setText("Active");
            suspendButton.setText("Suspend User");
            suspendButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            suspendButton.setStrokeColorResource(android.R.color.holo_red_dark);
        }

        if (user.getRole() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDetailRole.getAdapter();
            int position = adapter.getPosition(user.getRole());
            spinnerDetailRole.setSelection(position);
        }
    }
}
