package com.example.csit314sdm;

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

public class AdminCreateUserActivity extends AppCompatActivity {

    private EditText etCreateUserEmail, etCreateUserPassword;
    private Spinner spinnerCreateUserRole;
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
            btnBack.setOnClickListener(v -> {
                // Go back to the dashboard without creating a new instance
                finish();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing the screen. Check layout IDs.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Initialization failed", e);
            finish();
        }
    }

    private void initializeUI() {
        etCreateUserEmail = findViewById(R.id.etCreateUserEmail);
        etCreateUserPassword = findViewById(R.id.etCreateUserPassword);
        spinnerCreateUserRole = findViewById(R.id.spinnerCreateUserRole);
        btnAdminCreateUser = findViewById(R.id.btnAdminCreateUser);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.tvAdminRoleLabel).setVisibility(View.VISIBLE);
        spinnerCreateUserRole.setVisibility(View.VISIBLE);
        btnAdminCreateUser.setVisibility(View.VISIBLE);

        // Hide UI elements not relevant to this admin screen
        findViewById(R.id.layoutFullName).setVisibility(View.GONE);
        findViewById(R.id.layoutPhoneNumber).setVisibility(View.GONE);
        findViewById(R.id.layoutDob).setVisibility(View.GONE);
        findViewById(R.id.tvRoleLabel).setVisibility(View.GONE);
        findViewById(R.id.spinnerRole).setVisibility(View.GONE);
        findViewById(R.id.btnCreateAccount).setVisibility(View.GONE);

        // Set up the spinner with user roles
        String[] userTypes = {"PIN", "CSR", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCreateUserRole.setAdapter(adapter);
    }

    private void handleCreateUser() {
        try {
            String email = etCreateUserEmail.getText().toString().trim();
            String password = etCreateUserPassword.getText().toString().trim();
            String userType = spinnerCreateUserRole.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnAdminCreateUser.setEnabled(false);

            RegistrationController.RegistrationCallback callback = new RegistrationController.RegistrationCallback() {
                @Override
                public void onRegistrationSuccess(String returnedUserType) {
                    progressBar.setVisibility(View.GONE);
                    btnAdminCreateUser.setEnabled(true);
                    Toast.makeText(AdminCreateUserActivity.this, "User '" + email + "' created as " + returnedUserType, Toast.LENGTH_LONG).show();
                    etCreateUserEmail.setText("");
                    etCreateUserPassword.setText("");
                }

                @Override
                public void onRegistrationFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnAdminCreateUser.setEnabled(true);
                    Toast.makeText(AdminCreateUserActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            };

            registrationController.registerUser(email, password, userType, callback);

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAdminCreateUser.setEnabled(true);
            Toast.makeText(this, "An unexpected error occurred during user creation.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Error in handleCreateUser", e);
        }
    }
}
