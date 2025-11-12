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
import com.google.android.material.appbar.MaterialToolbar;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    private EditText etFullName, etContact;
    private TextView tvEmail, tvStatus;
    private Spinner spinnerRole;
    private Button btnToggleEdit, btnToggleSuspend, btnUpdateProfile;
    private ProgressBar progressBar;
    private ScrollView contentScrollView;
    private MaterialToolbar toolbar;

    // Controllers for both Account and Profile use cases
    private RetrieveUserAccountController retrieveUserAccountController;
    private RetrieveUserProfileController retrieveUserProfileController;
    private UpdateUserAccountController updateUserAccountController;

    private UpdateUserProfileController updateUserProfileController;

    private SuspendUserAccountController suspendUserAccountController;
    private SuspendUserProfileController suspendUserProfileController;
    private CreateUserProfileController createUserProfileController;

    private TextView tvToolbarTitle;
    private String userId;
    private User currentUser;

    private boolean isEditMode = false;
    private final String[] roles = {"PIN", "CSR", "User Admin"};

    private String launchMode = "MANAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        if (getIntent().hasExtra("MODE")) {
            launchMode = getIntent().getStringExtra("MODE");
        }

        // Instantiating all necessary controllers
        retrieveUserAccountController = new RetrieveUserAccountController();
        retrieveUserProfileController = new RetrieveUserProfileController(); // Added instantiation
        updateUserAccountController = new UpdateUserAccountController();
        suspendUserAccountController = new SuspendUserAccountController();
        suspendUserProfileController = new SuspendUserProfileController();
        createUserProfileController = new CreateUserProfileController();

        userId = getIntent().getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        applyLaunchMode();
        loadUserDetails();
    }

    private void initializeViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

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

    private void applyLaunchMode() {
        if ("VIEW_ONLY".equals(launchMode)) {
            if (tvToolbarTitle != null) tvToolbarTitle.setText("User Account Details");
            btnToggleEdit.setVisibility(View.GONE);
            etFullName.setEnabled(false);
            etContact.setEnabled(false);
            spinnerRole.setEnabled(false);
            btnToggleSuspend.setVisibility(View.GONE);
        } else {
            if (tvToolbarTitle != null) tvToolbarTitle.setText("User Profile Details");
            btnToggleEdit.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentScrollView.setVisibility(View.GONE);

        // Decide which controller to use based on the context (launchMode)
        if ("MANAGE".equals(launchMode)) {
            // Admin is managing an account, so use the ACCOUNT controller.
            retrieveUserAccountController.fetchUserById(userId, new RetrieveUserAccountController.UserCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    handleSuccess(user);
                }

                @Override
                public void onFailure(Exception e) {
                    handleFailure(e);
                }
            });
        } else {
            // A different context (e.g., viewing a profile) would use the PROFILE controller.
            retrieveUserProfileController.fetchUserById(userId, new RetrieveUserProfileController.UserCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    handleSuccess(user);
                }

                @Override
                public void onFailure(Exception e) {
                    handleFailure(e);
                }
            });
        }
    }

    // Helper method to handle successful data loading to avoid code duplication
    private void handleSuccess(User user) {
        currentUser = user;
        populateUI(user);
        progressBar.setVisibility(View.GONE);
        contentScrollView.setVisibility(View.VISIBLE);
        if (!"VIEW_ONLY".equals(launchMode)) {
            btnToggleEdit.setVisibility(View.VISIBLE);
            btnToggleSuspend.setVisibility(View.VISIBLE);
        }
    }

    // Helper method to handle failed data loading
    private void handleFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(UserDetailActivity.this, "Failed to load user: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void populateUI(User user) {
        etFullName.setText(user.getFullName());
        etContact.setText(user.getPhoneNumber());
        tvEmail.setText(user.getEmail());
        tvStatus.setText(user.getAccountStatus());
        spinnerRole.setSelection(Arrays.asList(roles).indexOf(user.getRole()));
        btnToggleSuspend.setText("Suspended".equals(user.getAccountStatus()) ? "Reinstate User" : "Suspend User");
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        etFullName.setEnabled(isEditMode);
        etContact.setEnabled(isEditMode);
        spinnerRole.setEnabled(isEditMode);
        btnToggleEdit.setText(isEditMode ? "Cancel" : "Edit");
        btnUpdateProfile.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        btnToggleSuspend.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        if (!isEditMode) populateUI(currentUser);
    }

    private void saveChanges() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", etFullName.getText().toString());
        updates.put("phoneNumber", etContact.getText().toString());
        updates.put("role", spinnerRole.getSelectedItem().toString());

        progressBar.setVisibility(View.VISIBLE);
        updateUserAccountController.updateUserAccount(userId, updates, new UpdateUserAccountController.UserCallback<Void>() {
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

        updateUserProfileController.updateUserProfile(userId, updates, new UpdateUserProfileController.UserCallback<Void>() {
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
                    if ("MANAGE".equals(launchMode)) {
                        if (isSuspending) {
                            suspendUserAccountController.suspendUserAccount(userId, new SuspendUserAccountController.UserCallback<Void>() {
                                @Override public void onSuccess(Void result) { loadUserDetails(); }
                                @Override public void onFailure(Exception e) { /* Handle error */ }
                            });
                        } else {
                            suspendUserAccountController.reinstateUserAccount(userId, new SuspendUserAccountController.UserCallback<Void>() {
                                @Override public void onSuccess(Void result) { loadUserDetails(); }
                                @Override public void onFailure(Exception e) { /* Handle error */ }
                            });
                        }
                    } else {
                        if (isSuspending) {
                            suspendUserProfileController.suspendUserProfile(userId, new SuspendUserProfileController.UserCallback<Void>() {
                                @Override public void onSuccess(Void result) { loadUserDetails(); }
                                @Override public void onFailure(Exception e) { /* Handle error */ }
                            });
                        } else {
                            suspendUserProfileController.reinstateUserProfile(userId, new SuspendUserProfileController.UserCallback<Void>() {
                                @Override public void onSuccess(Void result) { loadUserDetails(); }
                                @Override public void onFailure(Exception e) { /* Handle error */ }
                            });
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
