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


public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword;
    private Spinner registerUserTypeSpinner;
    private Button btnRegister;
    private ProgressDialog progressDialog;


    private RegistrationController registrationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);


        registrationController = new RegistrationController();


        initializeUI();

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void initializeUI() {
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerUserTypeSpinner = findViewById(R.id.registerUserTypeSpinner);
        btnRegister = findViewById(R.id.btnRegister);


        String[] userTypes = {"PIN", "CSR_Representative"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registerUserTypeSpinner.setAdapter(adapter);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);
    }

    private void handleRegistration() {

        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String userType = registerUserTypeSpinner.getSelectedItem().toString();


        progressDialog.show();


        registrationController.registerUser(email, password, userType, new RegistrationController.RegistrationCallback() {
            @Override
            public void onRegistrationSuccess(String returnedUserType) {

                progressDialog.dismiss();


                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                navigateToDashboard(returnedUserType);
            }

            @Override
            public void onRegistrationFailure(String errorMessage) {

                progressDialog.dismiss();


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

            intent = new Intent(RegisterActivity.this, loginPage.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
