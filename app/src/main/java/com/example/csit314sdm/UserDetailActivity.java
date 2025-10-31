// File: app/src/main/java/com/example/csit314sdm/UserDetailActivity.java
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
import com.google.android.material.appbar.MaterialToolbar; // Import this if missing
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    // --- UI Components ---
    private EditText etFullName, etContact;
    private TextView tvEmail, tvStatus;
    private Spinner spinnerRole;
    private Button btnToggleEdit, btnToggleSuspend, btnUpdateProfile;
    private ProgressBar progressBar;
    private ScrollView contentScrollView;
    private MaterialToolbar toolbar; // Added a toolbar variable

    // --- Control and Entity ---
    private UserManagementController controller;

    private TextView tvToolbarTitle;
    private String userId;
    private User currentUser;

    private boolean isEditMode = false;
    private final String[] roles = {"PIN", "CSR", "User Admin"};

    // --- The key variable for this fix ---
    private String launchMode = "MANAGE"; // Default to "MANAGE" mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // --- Read the launch mode from the Intent ---
        if (getIntent().hasExtra("MODE")) {
            launchMode = getIntent().getStringExtra("MODE");
        }

        controller = new UserManagementController();
        userId = getIntent().getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        applyLaunchMode(); // Apply UI changes based on the mode
        loadUserDetails();
    }

    // In UserDetailActivity.java
    private void initializeViews() {
        // --- FIX #1: Find the views that ACTUALLY exist in your XML ---

        // Find the custom title TextView from your RelativeLayout header
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle); // ADD THIS ID TO YOUR XML

        // Find the back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // The finish() method is correct

        // The rest of your view initializations are correct
        etFullName = findViewById(R.id.etDetailFullName);
        etContact = findViewById(R.id.etDetailContact);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvStatus = findViewById(R.id.tvDetailStatus);
        spinnerRole = findViewById(R.id.spinnerDetailRole);
        btnToggleEdit = findViewById(R.id.btnToggleEdit);
        btnToggleSuspend = findViewById(R.id.btnToggleSuspend);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        progressBar = findViewById(R.id.progressBar);
        contentScrollView = findViewById(R.id.contentScrollView);

        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles));

        btnToggleEdit.setOnClickListener(v -> toggleEditMode());
        btnUpdateProfile.setOnClickListener(v -> saveChanges());
        btnToggleSuspend.setOnClickListener(v -> confirmAndToggleSuspend());
    }

    // --- FIX #2: Update this method to use the custom title TextView ---
    private void applyLaunchMode() {
        if ("VIEW_ONLY".equals(launchMode)) {
            // 1. Change the title using the correct TextView
            if (tvToolbarTitle != null) {
                tvToolbarTitle.setText("User Account Details");
            }

            // 2. Hide the "Edit" button
            btnToggleEdit.setVisibility(View.GONE);

            // 3. Ensure all fields are disabled
            etFullName.setEnabled(false);
            etContact.setEnabled(false);
            spinnerRole.setEnabled(false);

            // 4. Hide the suspend button
            btnToggleSuspend.setVisibility(View.GONE);

        } else {
            // This is the default "MANAGE" mode
            if (tvToolbarTitle != null) {
                tvToolbarTitle.setText("User Profile Details");
            }
            // Ensure the Edit button is visible in manage mode
            btnToggleEdit.setVisibility(View.VISIBLE);
        }
    }


    private void loadUserDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentScrollView.setVisibility(View.GONE);
        controller.fetchUserById(userId, new UserManagementController.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                populateUI(user);
                progressBar.setVisibility(View.GONE);
                contentScrollView.setVisibility(View.VISIBLE);

                // Only show buttons if not in VIEW_ONLY mode
                if (!"VIEW_ONLY".equals(launchMode)) {
                    btnToggleEdit.setVisibility(View.VISIBLE);
                    btnToggleSuspend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, "Failed to load user: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateUI(User user) {
        etFullName.setText(user.getFullName());
        etContact.setText(user.getPhoneNumber());
        tvEmail.setText(user.getEmail());
        tvStatus.setText(user.getAccountStatus());
        spinnerRole.setSelection(Arrays.asList(roles).indexOf(user.getRole()));

        if ("Suspended".equals(user.getAccountStatus())) {
            btnToggleSuspend.setText("Reinstate User");
        } else {
            btnToggleSuspend.setText("Suspend User");
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        etFullName.setEnabled(isEditMode);
        etContact.setEnabled(isEditMode);
        spinnerRole.setEnabled(isEditMode);

        if (isEditMode) {
            btnToggleEdit.setText("Cancel");
            btnUpdateProfile.setVisibility(View.VISIBLE);
            btnToggleSuspend.setVisibility(View.GONE);
        } else {
            btnToggleEdit.setText("Edit");
            btnUpdateProfile.setVisibility(View.GONE);
            btnToggleSuspend.setVisibility(View.VISIBLE);
            populateUI(currentUser);
        }
    }

    private void saveChanges() {
        String newFullName = etFullName.getText().toString();
        String newContact = etContact.getText().toString();
        String newRole = spinnerRole.getSelectedItem().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName);
        updates.put("phoneNumber", newContact);
        updates.put("role", newRole);

        progressBar.setVisibility(View.VISIBLE);
        controller.updateUserProfile(userId, updates, new UserManagementController.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                isEditMode = false;
                loadUserDetails();
                toggleEditMode();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmAndToggleSuspend() {
        boolean isSuspending = !"Suspended".equals(currentUser.getAccountStatus());
        String action = isSuspending ? "suspend" : "reinstate";

        new AlertDialog.Builder(this)
                .setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " User")
                .setMessage("Are you sure you want to " + action + " this user?")
                .setPositiveButton(action.toUpperCase(), (dialog, which) -> {
                    if (isSuspending) {
                        controller.suspendUserProfile(userId, new UserManagementController.UserCallback<Void>() {
                            @Override public void onSuccess(Void result) { loadUserDetails(); }
                            @Override public void onFailure(Exception e) { /* Handle error */ }
                        });
                    } else {
                        controller.reinstateUserProfile(userId, new UserManagementController.UserCallback<Void>() {
                            @Override public void onSuccess(Void result) { loadUserDetails(); }
                            @Override public void onFailure(Exception e) { /* Handle error */ }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
