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
        // Corrected the layout file to the one that exists in your project
        setContentView(R.layout.loginpage);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        // This will now correctly find the ProgressBar in loginpage.xml
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

        // This line should no longer crash
        progressBar.setVisibility(View.VISIBLE);
        loginController.loginUser(email, password, new LoginController.LoginCallback() {
            @Override
            public void onLoginSuccess(String userRole) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                if ("User Admin".equals(userRole)) {
                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else if ("CSR".equals(userRole)) {
                    Intent intent = new Intent(LoginActivity.this, CsrDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Handle other roles or show a generic dashboard
                }
            }

            @Override
            public void onLoginFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
