package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        loginController = new LoginController();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        loginController.loginUser(email, password, new LoginController.LoginCallback() {
            @Override
            public void onLoginSuccess(String userRole) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                if ("Admin".equals(userRole)) {
                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else if ("CSR".equalsIgnoreCase(userRole)) {
                    Intent intent = new Intent(LoginActivity.this, CSRHomeScreenActivity.class);
                    startActivity(intent);
                    finish();
                } else if ("Platform".equals(userRole)){
                    Intent intent = new Intent(LoginActivity.this, PlatformDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else if ("PIN".equals(userRole)) { // Corrected the redirection for PIN user
                    Intent intent = new Intent(LoginActivity.this, PINHomeScreenActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid User Role", Toast.LENGTH_SHORT).show();
                }
            }
            public void onLoginFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
