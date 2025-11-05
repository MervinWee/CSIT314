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


public class loginPage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView  tvForgetAccount;
    private ProgressDialog progressDialog;


    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);


        loginController = new LoginController();


        initializeUI();
        setupClickableSpans();


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

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressDialog.show();


        loginController.loginUser(email, password, createLoginCallback());
    }


    private LoginController.LoginCallback createLoginCallback() {
        return new LoginController.LoginCallback() {
            @Override
            public void onLoginSuccess(String userType) {
                progressDialog.dismiss();

                navigateToDashboard(userType);
            }

            @Override
            public void onLoginFailure(String errorMessage) {
                progressDialog.dismiss();

                Toast.makeText(loginPage.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }


    private void navigateToDashboard(String userRole) {
        Intent intent;


        if ("Admin".equals(userRole)) {

            Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
            intent = new Intent(loginPage.this, AdminDashboardActivity.class);
        }
        else if ("PIN".equals(userRole)) {

            intent = new Intent(loginPage.this, PINHomeScreenActivity.class);
        }
        else if ("CSR".equals(userRole)) {

            intent = new Intent(loginPage.this, CSRHomeScreenActivity.class);
        }
        else if ("Platform".equals(userRole)) {

            intent = new Intent ( loginPage.this, PlatformDashboardActivity.class);
        }
        else {

            Toast.makeText(this, "Unknown user role. Please contact support.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut(); // Sign out for security
            return;
        }


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private void setupClickableSpans() {




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
