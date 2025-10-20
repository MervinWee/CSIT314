package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.ProgressDialog; // Import for loading indicator

import androidx.appcompat.app.AppCompatActivity;

// BOUNDARY: Manages the UI, captures user input, and delegates logic to the Controller.
public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword;
    private Spinner registerUserTypeSpinner;
    private Button btnRegister;
    private ProgressDialog progressDialog;

    // The Boundary holds a reference to the Controller.
    private RegistrationController registrationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note: The EdgeToEdge code from your original file can be complex.
        // For simplicity in this BCE example, it's removed. You can add it back if needed.
        setContentView(R.layout.activity_register);

        // Initialize the controller.
        registrationController = new RegistrationController();

        // Standard UI setup.
        initializeUI();

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void initializeUI() {
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerUserTypeSpinner = findViewById(R.id.registerUserTypeSpinner);
        btnRegister = findViewById(R.id.btnRegister);

        // Setup Spinner
        String[] userTypes = {"PIN", "CSR_Representative"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registerUserTypeSpinner.setAdapter(adapter);

        // Setup loading indicator
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);
    }

    private void handleRegistration() {
        // 1. Boundary's job: get raw input from the UI.
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String userType = registerUserTypeSpinner.getSelectedItem().toString();

        // Show loading indicator.
        progressDialog.show();

        // 2. Boundary's job: delegate the work to the controller.
        registrationController.registerUser(email, password, userType, new RegistrationController.RegistrationCallback() {
            @Override
            public void onRegistrationSuccess(String returnedUserType) {
                // Hide loading indicator.
                progressDialog.dismiss();

                // Boundary's job: handle success UI (navigate).
                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                navigateToDashboard(returnedUserType);
            }

            @Override
            public void onRegistrationFailure(String errorMessage) {
                // Hide loading indicator.
                progressDialog.dismiss();

                // Boundary's job: handle failure UI (show error message).
                Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        if ("PIN".equals(userType)) {
            intent = new Intent(RegisterActivity.this, PINHomeScreenActivity.class);
        } else if ("CSR_Representative".equals(userType)) {
            intent = new Intent(RegisterActivity.this, CSRHomeScreenActivity.class);
        } else {
            // Default case, maybe go back to login
            intent = new Intent(RegisterActivity.this, loginPage.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
