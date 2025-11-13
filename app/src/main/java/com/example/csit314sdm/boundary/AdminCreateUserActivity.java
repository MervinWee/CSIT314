package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csit314sdm.R;
import com.example.csit314sdm.controller.CreateUserProfileController;
import com.example.csit314sdm.entity.User;

public class AdminCreateUserActivity extends AppCompatActivity {

    private EditText etCreateUserEmail, etCreateUserPassword, etFullName, etPhoneNumber, etDob, etAddress;
    private Spinner spinnerCreateUserRole;
    private Button btnAdminCreateUser, btnGoToCreateRole;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private CreateUserProfileController createUserProfileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_user);

        try {
            createUserProfileController = new CreateUserProfileController();
            initializeUI();

            btnAdminCreateUser.setOnClickListener(v -> handleCreateUser());
            btnBack.setOnClickListener(v -> finish());
            btnGoToCreateRole.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCreateUserActivity.this, CreateUserRoleActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing the screen. Check layout IDs.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Initialization failed", e);
            finish();
        }
    }

    private void initializeUI() {
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
