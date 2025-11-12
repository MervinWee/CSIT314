package com.example.csit314sdm.boundary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.csit314sdm.R;
import com.example.csit314sdm.entity.User;
import com.example.csit314sdm.controller.UserProfileController;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreateUserProfileActivity extends AppCompatActivity {

    private UserProfileController CreateUserProfileController;
    private AutoCompleteTextView spinnerUserAccount;
    private TextInputEditText etFullName, etContactNumber, etDateOfBirth, etAddress;
    private Spinner spinnerRole;
    private Button btnSaveProfile;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private List<User> userList = new ArrayList<>();
    private User selectedUser;

    private static final String TAG = "CreateUserProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_profile);

        CreateUserProfileController = new UserProfileController();
        initializeUI();
        loadUsersWithoutProfiles();
    }

    private void initializeUI() {
        spinnerUserAccount = findViewById(R.id.spinnerUserAccount);
        etFullName = findViewById(R.id.etFullName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etAddress = findViewById(R.id.etAddress);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        etFullName.setEnabled(false);
        etContactNumber.setEnabled(false);
        etDateOfBirth.setEnabled(false);
        etAddress.setEnabled(false);

        String[] userTypes = {"PIN", "CSR", "User Admin", "Platform Manager"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> handleCreateUser());

        spinnerUserAccount.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEmail = (String) parent.getItemAtPosition(position);
            for (User user : userList) {
                if (user.getEmail().equals(selectedEmail)) {
                    selectedUser = user;
                    break;
                }
            }
            if (selectedUser != null) {
                etFullName.setText(selectedUser.getFullName());
                etContactNumber.setText(selectedUser.getPhoneNumber());
                etDateOfBirth.setText(selectedUser.getDob());
                etAddress.setText(selectedUser.getAddress());
            }
        });
    }

    private void loadUsersWithoutProfiles() {
        progressBar.setVisibility(View.VISIBLE);

        CreateUserProfileController.getAllUsersWithProfileCheck(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> allUsers) {
                progressBar.setVisibility(View.GONE);

                if (allUsers == null || allUsers.isEmpty()) {
                    Toast.makeText(CreateUserProfileActivity.this, "No users found in the system.", Toast.LENGTH_LONG).show();
                    return;
                }

                userList.clear();
                List<String> userEmails = new ArrayList<>();

                for (User user : allUsers) {
                    userList.add(user);
                    userEmails.add(user.getEmail());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateUserProfileActivity.this, android.R.layout.simple_dropdown_item_1line, userEmails);
                spinnerUserAccount.setAdapter(adapter);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onDataLoadFailed: " + errorMessage);
                Toast.makeText(CreateUserProfileActivity.this, "Error loading users: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleCreateUser() {
        String currentText = spinnerUserAccount.getText().toString();
        boolean userIsValid = false;
        if (selectedUser != null && selectedUser.getEmail().equals(currentText)) {
            userIsValid = true;
        }

        if (!userIsValid) {
            Toast.makeText(this, "Please select a valid user from the dropdown list.", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = spinnerRole.getSelectedItem().toString();

        progressBar.setVisibility(View.VISIBLE);

        CreateUserProfileController.updateUserRole(selectedUser.getId(), role, new UserProfileController.ProfileCallback() {
            @Override
            public void onProfileSaveSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateUserProfileActivity.this, "User role updated successfully!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onProfileSaveFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateUserProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
