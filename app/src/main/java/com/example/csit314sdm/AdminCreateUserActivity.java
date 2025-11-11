package com.example.csit314sdm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminCreateUserActivity extends AppCompatActivity {

    private EditText etCreateUserEmail, etCreateUserPassword, etFullName, etPhoneNumber, etDob, etAddress;
    private Button btnAdminCreateUser;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private RegistrationController registrationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_user);

        try {
            registrationController = new RegistrationController();
            initializeUI();

            btnAdminCreateUser.setOnClickListener(v -> handleCreateUser());
            btnBack.setOnClickListener(v -> finish());

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing the screen. Check layout IDs.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Initialization failed", e);
            finish();
        }
    }

    private void initializeUI() {
        etCreateUserEmail = findViewById(R.id.etCreateUserEmail);
        etCreateUserPassword = findViewById(R.id.etCreateUserPassword);
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDob = findViewById(R.id.etDob);
        etAddress = findViewById(R.id.etAddress);
        btnAdminCreateUser = findViewById(R.id.btnAdminCreateUser);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.layoutFullName).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutPhoneNumber).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutDob).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutAddress).setVisibility(View.VISIBLE);
        btnAdminCreateUser.setVisibility(View.VISIBLE);

        findViewById(R.id.tvAdminRoleLabel).setVisibility(View.GONE);
        findViewById(R.id.spinnerCreateUserRole).setVisibility(View.GONE);
        findViewById(R.id.tvRoleLabel).setVisibility(View.GONE);
        findViewById(R.id.spinnerRole).setVisibility(View.GONE);
        findViewById(R.id.btnCreateAccount).setVisibility(View.GONE);
    }

    private void handleCreateUser() {
        try {
            String email = etCreateUserEmail.getText().toString().trim();
            String password = etCreateUserPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Email, password, and full name are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnAdminCreateUser.setEnabled(false);

            RegistrationController.RegistrationCallback callback = new RegistrationController.RegistrationCallback() {
                @Override
                public void onRegistrationSuccess(String returnedUserType) {
                    progressBar.setVisibility(View.GONE);
                    btnAdminCreateUser.setEnabled(true);
                    Toast.makeText(AdminCreateUserActivity.this, "User '" + email + "' created successfully.", Toast.LENGTH_LONG).show();
                    etCreateUserEmail.setText("");
                    etCreateUserPassword.setText("");
                    etFullName.setText("");
                    etPhoneNumber.setText("");
                    etDob.setText("");
                    etAddress.setText("");
                }

                @Override
                public void onRegistrationFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnAdminCreateUser.setEnabled(true);
                    Toast.makeText(AdminCreateUserActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            };

            registrationController.registerUser(email, password, "PIN", fullName, phoneNumber, dob, address, callback);

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAdminCreateUser.setEnabled(true);
            Toast.makeText(this, "An unexpected error occurred during user creation.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Error in handleCreateUser", e);
        }
    }
}
