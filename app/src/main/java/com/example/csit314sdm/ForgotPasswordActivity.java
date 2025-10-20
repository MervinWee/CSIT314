package com.example.csit314sdm;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// BOUNDARY: Manages the 'Forgot Password' UI and delegates logic to the Controller.
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotPasswordEmail;
    private Button btnSendResetLink;
    private ProgressDialog progressDialog;

    private ForgotPasswordController forgotPasswordController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Initialize the controller
        forgotPasswordController = new ForgotPasswordController();

        // Standard UI setup
        initializeUI();

        btnSendResetLink.setOnClickListener(v -> handleSendResetLink());
    }

    private void initializeUI() {
        etForgotPasswordEmail = findViewById(R.id.etForgotPasswordEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending reset link...");
        progressDialog.setCancelable(false);
    }

    private void handleSendResetLink() {
        // 1. Boundary's job: get raw input from the UI.
        String email = etForgotPasswordEmail.getText().toString().trim();

        progressDialog.show();

        // 2. Boundary's job: delegate the work to the controller.
        forgotPasswordController.sendPasswordResetEmail(email, new ForgotPasswordController.ForgotPasswordCallback() {
            @Override
            public void onResetLinkSent() {
                progressDialog.dismiss();
                // Boundary's job: handle success UI.
                Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent successfully! Please check your email.", Toast.LENGTH_LONG).show();
                // Finish this activity and go back to the login page.
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                // Boundary's job: handle failure UI.
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
