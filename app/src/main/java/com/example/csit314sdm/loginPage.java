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

    // Replace the existing navigateToDashboard method with this one.
    private void navigateToDashboard(String userRole) {
        Intent intent;

        // Use if/else if to check the role string returned by the controller.
        if ("Admin".equals(userRole)) {
            // This is the correct case for your Administrator.
            Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
            intent = new Intent(loginPage.this, AdminDashboardActivity.class);
        }
        else if ("PIN".equals(userRole)) {
            // Your existing PIN role
            intent = new Intent(loginPage.this, PINHomeScreenActivity.class);
        }
        else if ("CSR".equals(userRole)) {
            // Your existing CSR role
            intent = new Intent(loginPage.this, CsrDashboardActivity.class);
        }
        else if ("Platform".equals(userRole)) {
            // Your existing Platform management role
            intent = new Intent ( loginPage.this, PlatformDashboardActivity.class);
        }
        else {
            // Fallback for any unknown or null role.
            Toast.makeText(this, "Unknown user role. Please contact support.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut(); // Sign out for security
            return; // Do not navigate anywhere.
        }

        // These flags apply to all successful navigations.
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
