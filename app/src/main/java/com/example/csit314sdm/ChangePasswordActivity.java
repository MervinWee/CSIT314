package com.example.csit314sdm;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnUpdatePassword;
    private ChangePasswordController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        controller = new ChangePasswordController();
        initializeUI();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_change_password);
        topAppBar.setNavigationOnClickListener(v -> finish());

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        btnUpdatePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Show a loading indicator here if you have one
        Toast.makeText(this, "Updating password...", Toast.LENGTH_SHORT).show();

        controller.updateUserPassword(currentPassword, newPassword, confirmPassword, new ChangePasswordController.ChangePasswordCallback() {
            @Override
            public void onChangeSuccess() {
                // This runs on a background thread, so switch to the UI thread to show Toast and finish
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Close this screen and go back to the profile
                });
            }

            @Override
            public void onChangeFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
