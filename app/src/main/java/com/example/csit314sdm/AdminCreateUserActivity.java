package com.example.csit314sdm;

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
            // Initialize the controller and all UI elements
            registrationController = new RegistrationController();
            initializeUI();

            // Set up the button click listeners
            btnAdminCreateUser.setOnClickListener(v -> handleCreateUser());

            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCreateUserActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });

        } catch (Exception e) {
            // This will catch any NullPointerException if an ID is still wrong in your XML
            Toast.makeText(this, "Error initializing the screen. Check layout IDs.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Initialization failed", e);
            finish(); // Close the broken screen
        }
    }

    private void initializeUI() {
        // --- STEP 1: Find all the views using their IDs from the XML ---
        etCreateUserEmail = findViewById(R.id.etCreateUserEmail);
        etCreateUserPassword = findViewById(R.id.etCreateUserPassword);
        spinnerCreateUserRole = findViewById(R.id.spinnerCreateUserRole);
        btnAdminCreateUser = findViewById(R.id.btnAdminCreateUser);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // --- STEP 2: Make the Admin-specific views VISIBLE ---
        // Your XML hides them by default, so the Java code must show them.
        findViewById(R.id.tvAdminRoleLabel).setVisibility(View.VISIBLE);
        spinnerCreateUserRole.setVisibility(View.VISIBLE);
        btnAdminCreateUser.setVisibility(View.VISIBLE);

        // --- STEP 3: Hide any other views that should not be on this screen ---
        // This ensures only the correct fields are visible.
        findViewById(R.id.layoutFullName).setVisibility(View.GONE);
        findViewById(R.id.layoutPhoneNumber).setVisibility(View.GONE);
        findViewById(R.id.layoutDob).setVisibility(View.GONE);
        findViewById(R.id.tvRoleLabel).setVisibility(View.GONE);
        findViewById(R.id.spinnerRole).setVisibility(View.GONE);
        findViewById(R.id.btnCreateAccount).setVisibility(View.GONE);

        // --- STEP 4: Set up the items (the "content") for your Spinner ---
        String[] userTypes = {"PIN", "CSR", "Admin"}; // These are the options that will appear
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

            if (registrationController == null) {
                throw new IllegalStateException("RegistrationController is not initialized.");
            }

            registrationController.registerUser(email, password, userType, new RegistrationController.RegistrationCallback() {
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
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAdminCreateUser.setEnabled(true);
            Toast.makeText(this, "An unexpected error occurred during user creation.", Toast.LENGTH_LONG).show();
            Log.e("AdminCreateUser", "Error in handleCreateUser", e);
        }
    }
}
