package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csit314sdm.R;
import com.example.csit314sdm.controller.CreateUserProfileController;
import com.example.csit314sdm.controller.UserProfileController;
import com.example.csit314sdm.entity.User;

import java.util.ArrayList;
import java.util.List;

public class AdminCreateUserActivity extends AppCompatActivity {

    private EditText etCreateUserEmail, etCreateUserPassword, etFullName, etPhoneNumber, etDob, etAddress;
    private Spinner spinnerCreateUserRole;
    private Button btnAdminCreateUser, btnGoToCreateRole;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private AutoCompleteTextView acUserSearchEmail;

    private CreateUserProfileController createUserProfileController;
    private UserProfileController userProfileController;
    private List<User> userList = new ArrayList<>();
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_user);

        try {
            createUserProfileController = new CreateUserProfileController();
            userProfileController = new UserProfileController();
            initializeUI();
            loadUsers();

            btnAdminCreateUser.setOnClickListener(v -> handleCreateUser());
            btnBack.setOnClickListener(v -> finish());
            btnGoToCreateRole.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCreateUserActivity.this, CreateUserRoleActivity.class);
                startActivity(intent);
            });

            acUserSearchEmail.setOnItemClickListener((parent, view, position, id) -> {
                String selectedEmail = (String) parent.getItemAtPosition(position);
                for (User user : userList) {
                    if (user.getEmail().equals(selectedEmail)) {
                        selectedUser = user;
                        break;
                    }
                }
                if (selectedUser != null) {
                    etFullName.setText(selectedUser.getFullName());
                    etCreateUserEmail.setText(selectedUser.getEmail());
                    etPhoneNumber.setText(selectedUser.getPhoneNumber());
                    etDob.setText(selectedUser.getDob());
                    etAddress.setText(selectedUser.getAddress());
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing the screen. Check layout IDs.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Initialization failed", e);
            finish();
        }
    }

    private void initializeUI() {
        acUserSearchEmail = findViewById(R.id.acUserSearchEmail);
        etFullName = findViewById(R.id.etFullName);
        etCreateUserEmail = findViewById(R.id.etCreateUserEmail);
        etCreateUserPassword = findViewById(R.id.etCreateUserPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDob = findViewById(R.id.etDob);
        etAddress = findViewById(R.id.etAddress);
        spinnerCreateUserRole = findViewById(R.id.spinnerCreateUserRole);
        btnAdminCreateUser = findViewById(R.id.btnAdminCreateUser);
        btnGoToCreateRole = findViewById(R.id.btnGoToCreateRole);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Set visibility for all relevant UI components
        findViewById(R.id.layoutFullName).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutEmail).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutPassword).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutPhoneNumber).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutDob).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutAddress).setVisibility(View.VISIBLE);
        spinnerCreateUserRole.setVisibility(View.VISIBLE);
        btnAdminCreateUser.setVisibility(View.VISIBLE);
        btnGoToCreateRole.setVisibility(View.VISIBLE);

        // Set up the spinner with roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCreateUserRole.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        userProfileController.getAllUsersWithProfileCheck(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> allUsers) {
                progressBar.setVisibility(View.GONE);
                if (allUsers != null && !allUsers.isEmpty()) {
                    userList.clear();
                    userList.addAll(allUsers);
                    List<String> userEmails = new ArrayList<>();
                    for (User user : allUsers) {
                        userEmails.add(user.getEmail());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminCreateUserActivity.this, android.R.layout.simple_dropdown_item_1line, userEmails);
                    acUserSearchEmail.setAdapter(adapter);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminCreateUserActivity.this, "Error loading users: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCreateUser() {
        try {
            String fullName = etFullName.getText().toString().trim();
            String email = etCreateUserEmail.getText().toString().trim();
            String password = etCreateUserPassword.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String role = spinnerCreateUserRole.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Full name, email and password are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnAdminCreateUser.setEnabled(false);
            btnGoToCreateRole.setEnabled(false);

            User.RegistrationCallback callback = new User.RegistrationCallback() {
                @Override
                public void onRegistrationSuccess(String returnedUserType) {
                    progressBar.setVisibility(View.GONE);
                    btnAdminCreateUser.setEnabled(true);
                    btnGoToCreateRole.setEnabled(true);
                    Toast.makeText(AdminCreateUserActivity.this, "User '" + email + "' created successfully.", Toast.LENGTH_LONG).show();
                    // Clear the fields
                    etFullName.setText("");
                    etCreateUserEmail.setText("");
                    etCreateUserPassword.setText("");
                    etPhoneNumber.setText("");
                    etDob.setText("");
                    etAddress.setText("");
                    spinnerCreateUserRole.setSelection(0);
                    acUserSearchEmail.setText("");
                }

                @Override
                public void onRegistrationFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnAdminCreateUser.setEnabled(true);
                    btnGoToCreateRole.setEnabled(true);
                    Toast.makeText(AdminCreateUserActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            };

            createUserProfileController.createUserProfile(email, password, role, fullName, phoneNumber, dob, address, callback);

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAdminCreateUser.setEnabled(true);
            btnGoToCreateRole.setEnabled(true);
            Toast.makeText(this, "An unexpected error occurred during user creation.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Error in handleCreateUser", e);
        }
    }
}
