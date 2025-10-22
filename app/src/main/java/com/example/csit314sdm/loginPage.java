package com.example.csit314sdm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

// BOUNDARY: Manages the Login UI, captures input, and delegates all logic to the LoginController.
public class loginPage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView  tvForgetAccount;
    private ProgressDialog progressDialog;

    // The Boundary holds a reference to the Controller.
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);

        // Initialize the controller
        loginController = new LoginController();

        // Standard UI setup
        initializeUI();
        setupClickableSpans();

        // Let the controller check for an existing session
        loginController.checkForExistingSession(createLoginCallback());
    }

    private void initializeUI() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgetAccount = findViewById(R.id.tvForgetAccount);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(v -> handleLoginAttempt());
    }

    private void handleLoginAttempt() {
        // 1. Boundary's job: get raw input.
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressDialog.show();

        // 2. Boundary's job: delegate the work to the controller.
        loginController.loginUser(email, password, createLoginCallback());
    }

    // Creates a reusable callback object to handle results from the controller.
    private LoginController.LoginCallback createLoginCallback() {
        return new LoginController.LoginCallback() {
            @Override
            public void onLoginSuccess(String userType) {
                progressDialog.dismiss();
                // Boundary's job: handle success UI (navigate).
                navigateToDashboard(userType);
            }

            @Override
            public void onLoginFailure(String errorMessage) {
                progressDialog.dismiss();
                // Boundary's job: handle failure UI (show error).
                Toast.makeText(loginPage.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }

    private void navigateToDashboard(String userType) {
        Intent intent;

        // Check for the "Admin" user type first.
        if ("Admin".equals(userType)) {
            intent = new Intent(loginPage.this, AdminDashboardActivity.class);
        }
        // The existing checks follow.
        else if ("PIN".equals(userType)) {
            intent = new Intent(loginPage.this, PINHomeScreenActivity.class);
        }
        // --- THIS IS THE MODIFIED LINE ---
        else if ("CSR_Representative".equals(userType)) {
            // OLD: intent = new Intent(loginPage.this, CSRHomeScreenActivity.class);
            // NEW: Direct CSR users to their new dashboard.
            intent = new Intent(loginPage.this, CsrDashboardActivity.class);
        }
        else {
            // Controller should prevent this, but have a fallback just in case.
            Toast.makeText(this, "Unknown user type. Please contact support.", Toast.LENGTH_LONG).show();
            // It's a good idea to sign out a user with an invalid role for security.
            FirebaseAuth.getInstance().signOut();
            return; // Stay on the login page.
        }

        // These flags apply to all successful role-based navigations.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupClickableSpans() {



        // --- Forgot Password Span ---
        SpannableString forgotPasswordSpannable = new SpannableString("Forget Password? Click here");
        ClickableSpan forgotClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(loginPage.this, ForgotPasswordActivity.class));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        };
        forgotPasswordSpannable.setSpan(forgotClickable, 20, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvForgetAccount.setText(forgotPasswordSpannable);
        tvForgetAccount.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
