package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    // Add variables for the new cards
    private MaterialCardView cardCreateUserAccount, cardCreateUserProfile, cardRetrieveUserAccount, cardRetrieveUserProfile;
    private Button btnAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Find the new views by their IDs
        cardCreateUserAccount = findViewById(R.id.cardCreateUserAccount);
        cardCreateUserProfile = findViewById(R.id.cardCreateUserProfile);
        cardRetrieveUserAccount = findViewById(R.id.cardRetrieveUserAccount);
        cardRetrieveUserProfile = findViewById(R.id.cardRetrieveUserProfile);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        cardCreateUserAccount.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminCreateUserActivity.class));
        });

        // Add listeners for the new cards
        cardCreateUserProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Create User Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Replace it with this:
        cardRetrieveUserAccount.setOnClickListener(v -> {
            // Launch the new search activity
            startActivity(new Intent(AdminDashboardActivity.this, SearchUserActivity.class));
        });

        cardRetrieveUserProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Retrieve User Profiles feature coming soon!", Toast.LENGTH_SHORT).show();
        });


        btnAdminLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, loginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
