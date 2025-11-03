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

    private UserProfileController userProfileController;
    private TextView tvCsrId, tvCsrName, tvCsrEmail;
    private Button btnEditProfile, btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_profile);

        userProfileController = new UserProfileController();

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
            userProfileController.getUserById(currentUser.getUid(), new UserProfileController.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    tvCsrId.setText(user.getShortId());
                    tvCsrName.setText(user.getFullName());
                    tvCsrEmail.setText(user.getEmail());
                }

                @Override
                public void onDataLoadFailed(String errorMessage) {
                    Toast.makeText(CsrProfileActivity.this, "Failed to load profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
