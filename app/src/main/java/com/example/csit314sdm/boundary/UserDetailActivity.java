package com.example.csit314sdm.boundary;

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

import com.example.csit314sdm.R;
import com.example.csit314sdm.entity.User;
import com.example.csit314sdm.controller.CreateUserProfileController;
import com.example.csit314sdm.controller.RetrieveUserAccountController;
import com.example.csit314sdm.controller.RetrieveUserProfileController;
import com.example.csit314sdm.controller.SuspendUserAccountController;
import com.example.csit314sdm.controller.SuspendUserProfileController;
import com.example.csit314sdm.controller.UpdateUserAccountController;
import com.example.csit314sdm.controller.UpdateUserProfileController;
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

    // Cleaned up controller declarations
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
        retrieveUserProfileController = new RetrieveUserProfileController();
        updateUserAccountController = new UpdateUserAccountController();
        updateUserProfileController = new UpdateUserProfileController(); // Added instantiation
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
        if ("ACCOUNT_DETAILS".equals(launchMode)) {
            if (tvToolbarTitle != null) tvToolbarTitle.setText("User Account Details");
            btnToggleEdit.setVisibility(View.VISIBLE);
            etFullName.setEnabled(false); // Always disabled in this mode
            etContact.setEnabled(false); // Always disabled in this mode
            spinnerRole.setEnabled(false); // Disabled until edit is clicked
            btnToggleSuspend.setVisibility(View.VISIBLE);
        } else if ("VIEW_ONLY".equals(launchMode)) {
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

        if ("ACCOUNT_DETAILS".equals(launchMode)) {
             retrieveUserAccountController.fetchUserById(userId, new RetrieveUserAccountController.UserCallback<User>() {
                @Override public void onSuccess(User user) { handleSuccess(user); }
                @Override public void onFailure(Exception e) { handleFailure(e); }
            });
        } else {
             retrieveUserProfileController.fetchUserById(userId, new RetrieveUserProfileController.UserCallback<User>() {
                @Override public void onSuccess(User user) { handleSuccess(user); }
                @Override public void onFailure(Exception e) { handleFailure(e); }
            });
        }
    }

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
        
        if ("ACCOUNT_DETAILS".equals(launchMode)) {
            spinnerRole.setEnabled(isEditMode);
            etFullName.setEnabled(false);
            etContact.setEnabled(false);
        } else {
            etFullName.setEnabled(isEditMode);
            etContact.setEnabled(isEditMode);
            spinnerRole.setEnabled(isEditMode);
        }

        btnToggleEdit.setText(isEditMode ? "Cancel" : "Edit");
        btnUpdateProfile.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        btnToggleSuspend.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        if (!isEditMode) populateUI(currentUser);
    }

    private void saveChanges() {
        Map<String, Object> updates = new HashMap<>();

        if ("ACCOUNT_DETAILS".equals(launchMode)) {
            updates.put("role", spinnerRole.getSelectedItem().toString());
        } else {
            updates.put("fullName", etFullName.getText().toString());
            updates.put("phoneNumber", etContact.getText().toString());
            updates.put("role", spinnerRole.getSelectedItem().toString());
        }

        progressBar.setVisibility(View.VISIBLE);

        UpdateUserAccountController.UserCallback<Void> callback = new UpdateUserAccountController.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                isEditMode = false;
                loadUserDetails(); // Reload data to show changes
                toggleEditMode(); // Exit edit mode
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserDetailActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if ("ACCOUNT_DETAILS".equals(launchMode)) {
            updateUserAccountController.updateUserAccount(userId, updates, callback);
        } else {
            // Assuming the default mode uses the profile controller
            updateUserProfileController.updateUserProfile(userId, updates, new UpdateUserProfileController.UserCallback<Void>() {
                @Override
                public void onSuccess(Void result) { callback.onSuccess(result); }
                @Override
                public void onFailure(Exception e) { callback.onFailure(e); }
            });
        }
    }

    private void confirmAndToggleSuspend() {
        boolean isSuspending = !"Suspended".equals(currentUser.getAccountStatus());
        String action = isSuspending ? "suspend" : "reinstate";

        new AlertDialog.Builder(this)
                .setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " User")
                .setMessage("Are you sure you want to " + action + " this user?")
                .setPositiveButton(action.toUpperCase(), (dialog, which) -> {
                    if ("ACCOUNT_DETAILS".equals(launchMode)) {
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
