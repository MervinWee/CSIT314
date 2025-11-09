package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CsrProfileActivity extends AppCompatActivity {

    private UserManagementController userManagementController; // Corrected controller
    private TextView tvCsrId, tvCsrName, tvCsrEmail;
    private Button btnEditProfile, btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_profile);

        userManagementController = new UserManagementController(); // Corrected instantiation

        MaterialToolbar toolbar = findViewById(R.id.toolbarCsrProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCsrId = findViewById(R.id.tvCsrId);
        tvCsrName = findViewById(R.id.tvCsrName);
        tvCsrEmail = findViewById(R.id.tvCsrEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CsrProfileActivity.this, EditCsrProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(CsrProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        loadCsrProfile();
    }

    private void loadCsrProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Corrected to use UserManagementController and its UserCallback
            userManagementController.fetchUserById(currentUser.getUid(), new UserManagementController.UserCallback<User>() {
                @Override
                public void onSuccess(User user) { // Corrected method name
                    tvCsrId.setText(user.getShortId());
                    tvCsrName.setText(user.getFullName());
                    tvCsrEmail.setText(user.getEmail());
                }

                @Override
                public void onFailure(Exception e) { // Corrected method name and parameter
                    Toast.makeText(CsrProfileActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
