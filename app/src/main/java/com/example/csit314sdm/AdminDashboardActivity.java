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
    private Button btnAdminLogout, btnViewAllProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialCardView cardCreateUserAccount = findViewById(R.id.cardCreateUserAccount);
        MaterialCardView cardCreateUserProfile = findViewById(R.id.cardCreateUserProfile);
        MaterialCardView cardRetrieveUserAccount = findViewById(R.id.cardRetrieveUserAccount);
        MaterialCardView cardRetrieveUserProfile = findViewById(R.id.cardRetrieveUserProfile); // The card for searching
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        // --- ADD THIS LINE TO FIND THE NEW CARD ---
        MaterialCardView cardManageCategories = findViewById(R.id.cardManageCategories);


// Set up the click listeners
        cardCreateUserAccount.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminCreateUserActivity.class));
        });

        cardCreateUserProfile.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, CreateUserProfileActivity.class));
        });

// THIS IS THE FIX: Make the "Retrieve User Profiles" card open the Search Activity
        cardRetrieveUserProfile.setOnClickListener(v -> {
            // The SearchUserActivity is the best screen for this, as it allows searching and viewing.
            startActivity(new Intent(AdminDashboardActivity.this, SearchUserActivity.class));
        });

// This card will open the screen that shows only roles (ViewAllUsersActivity)
        cardRetrieveUserAccount.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ViewAllUsersActivity.class));
        });

        btnAdminLogout.setOnClickListener(v -> {
            // Your logout logic here...
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, loginPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardManageCategories.setOnClickListener(v -> {
            // This is where we will navigate to the Category Management screen.
            Toast.makeText(this, "Opening Category Management...", Toast.LENGTH_SHORT).show();

            // The next step will be to create CategoryManagementActivity and then uncomment this:
            Intent intent = new Intent(AdminDashboardActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });
    }
}
