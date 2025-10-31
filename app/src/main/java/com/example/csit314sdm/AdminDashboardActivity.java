package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- Find all the clickable cards from the layout ---
        MaterialCardView cardCreateUserAccount = findViewById(R.id.cardCreateUserAccount);
        MaterialCardView cardRetrieveUserAccount = findViewById(R.id.cardRetrieveUserAccount);
        MaterialCardView cardCreateUserProfile = findViewById(R.id.cardCreateUserProfile);
        MaterialCardView cardRetrieveUserProfile = findViewById(R.id.cardRetrieveUserProfile);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        // --- Set OnClick Listeners ---

        // 1. Create User Account -> Navigates to the user creation page
        if (cardCreateUserAccount != null) {
            cardCreateUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminCreateUserActivity.class);
                startActivity(intent);
            });
        }

        if (cardCreateUserProfile != null) {
            cardCreateUserProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, CreateUserProfileActivity.class);
                startActivity(intent);
            });
        }



        if (cardRetrieveUserAccount != null) {
            cardRetrieveUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, UserAccountsActivity.class);

                intent.putExtra("MODE", "VIEW_ONLY");

                startActivity(intent);
            });
        }

        if (cardRetrieveUserProfile != null) {
            cardRetrieveUserProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, UserManagementActivity.class);
                intent.putExtra("SCREEN_TITLE", "Retrieve User Profiles");
                startActivity(intent);
            });
        }


        if (btnAdminLogout != null) {
            btnAdminLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminDashboardActivity.this, "You have been logged out.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboardActivity.this, loginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
