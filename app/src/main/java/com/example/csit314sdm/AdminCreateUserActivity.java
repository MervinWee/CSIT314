package com.example.csit314sdm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class AdminCreateUserActivity extends AppCompatActivity {

    private TextInputEditText etCreateUserEmail, etCreateUserPassword;
    private Spinner spinnerCreateUserRole;
    private Button btnAdminCreateUser, btnBack;
    private ProgressDialog progressDialog;

    // We can reuse the same RegistrationController!
    private RegistrationController registrationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_user);

        // Initialize the controller
        registrationController = new RegistrationController();

        initializeUI();

        btnAdminCreateUser.setOnClickListener(v -> handleCreateUser());
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to explicitly go back to the AdminDashboardActivity
                Intent intent = new Intent(AdminCreateUserActivity.this, AdminDashboardActivity.class);

                // Optional but good practice: Clear the activity stack above the dashboard
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

    }

    private void initializeUI() {
        etCreateUserEmail = findViewById(R.id.etCreateUserEmail);
        etCreateUserPassword = findViewById(R.id.etCreateUserPassword);
        spinnerCreateUserRole = findViewById(R.id.spinnerCreateUserRole);
        btnAdminCreateUser = findViewById(R.id.btnAdminCreateUser);
        btnBack = findViewById(R.id.btnBack);

        // Setup Spinner
        String[] userTypes = {"PIN", "CSR","Admin"}; // Add Admin role
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCreateUserRole.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating New User...");
        progressDialog.setCancelable(false);
    }

    private void handleCreateUser() {
        String email = etCreateUserEmail.getText().toString().trim();
        String password = etCreateUserPassword.getText().toString().trim();
        String userType = spinnerCreateUserRole.getSelectedItem().toString();

        progressDialog.show();

        // The controller does the hard work.
        registrationController.registerUser(email, password, userType, new RegistrationController.RegistrationCallback() {
            @Override
            public void onRegistrationSuccess(String returnedUserType) {
                progressDialog.dismiss();
                Toast.makeText(AdminCreateUserActivity.this, "User '" + email + "' created successfully as " + returnedUserType, Toast.LENGTH_LONG).show();
                // After creating, just clear the fields for the next creation.
                etCreateUserEmail.setText("");
                etCreateUserPassword.setText("");
            }

            @Override
            public void onRegistrationFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(AdminCreateUserActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
