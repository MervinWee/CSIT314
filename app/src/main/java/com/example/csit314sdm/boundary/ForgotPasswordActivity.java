package com.example.csit314sdm.boundary;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.csit314sdm.controller.ForgotPasswordController;
import com.example.csit314sdm.R;


public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotPasswordEmail;
    private Button btnSendResetLink;
    private ProgressDialog progressDialog;

    private ForgotPasswordController forgotPasswordController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        forgotPasswordController = new ForgotPasswordController();


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

        String email = etForgotPasswordEmail.getText().toString().trim();

        progressDialog.show();


        forgotPasswordController.sendPasswordResetEmail(email, new ForgotPasswordController.ForgotPasswordCallback() {
            @Override
            public void onResetLinkSent() {
                progressDialog.dismiss();

                Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent successfully! Please check your email.", Toast.LENGTH_LONG).show();

                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();

                Toast.makeText(ForgotPasswordActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
