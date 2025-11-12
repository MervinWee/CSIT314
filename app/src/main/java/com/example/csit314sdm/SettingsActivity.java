package com.example.csit314sdm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    // Corrected to use the new specific controller
    private UpdateUserProfileController updateUserProfileController;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Instantiated the correct controller
        updateUserProfileController = new UpdateUserProfileController();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        initializeUI();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_settings);
        topAppBar.setNavigationOnClickListener(v -> finish());

        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?")
                .setPositiveButton("Yes, Delete It", (dialog, which) -> {
                    performDeleteAccount();
                })
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDeleteAccount() {
        if (currentUserId == null) {
            Toast.makeText(this, "Could not identify user.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Corrected to use UpdateUserProfileController
        updateUserProfileController.deleteUserAccount(currentUserId, new User.UserDeleteCallback() {
            @Override
            public void onDeleteSuccess() {
                Toast.makeText(SettingsActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                // Log out and return to login screen
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class); // Corrected to LoginActivity.class
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onDeleteFailure(String errorMessage) {
                Toast.makeText(SettingsActivity.this, "Failed to delete account: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
