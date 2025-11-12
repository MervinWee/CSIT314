package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private LogoutController logoutController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        logoutController = new LogoutController();

        MaterialCardView cardCreateUserAccount = findViewById(R.id.cardCreateUserAccount);
        MaterialCardView cardRetrieveUserAccount = findViewById(R.id.cardRetrieveUserAccount);
        MaterialCardView cardCreateUserProfile = findViewById(R.id.cardCreateUserProfile);
        MaterialCardView cardRetrieveUserProfile = findViewById(R.id.cardRetrieveUserProfile);
        MaterialCardView cardCreateUserRole = findViewById(R.id.cardCreateUserRole);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        if (cardCreateUserAccount != null) {
            cardCreateUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, CreateUserProfileActivity.class);
                startActivity(intent);
            });
        }

        if (cardCreateUserProfile != null) {
            cardCreateUserProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminCreateUserActivity.class);
                startActivity(intent);
            });
        }

        if (cardCreateUserRole != null) {
            cardCreateUserRole.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, CreateUserRoleActivity.class);
                startActivity(intent);
            });
        }

        if (cardRetrieveUserAccount != null) {
            cardRetrieveUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, UserAccountsActivity.class);
                intent.putExtra("MODE", "VIEW_ONLY");
                // This title is for the other screen and is correct.
                intent.putExtra("SCREEN_TITLE", "All User Profiles"); 
                startActivity(intent);
            });
        }

        if (cardRetrieveUserProfile != null) {
            cardRetrieveUserProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, UserManagementActivity.class);
                // Corrected the title for this screen.
                intent.putExtra("SCREEN_TITLE", "All User Accounts");
                startActivity(intent);
            });
        }

        if (btnAdminLogout != null) {
            btnAdminLogout.setOnClickListener(v -> {
                logoutController.logoutUser();
                Toast.makeText(AdminDashboardActivity.this, "You have been logged out.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
