package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        MaterialCardView cardUpdateUserAccount = findViewById(R.id.cardUpdateUserAccount);
        MaterialCardView cardManageCategories = findViewById(R.id.cardManageCategories);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        // --- FIX #1: Find the Views for User Profile Management ---
        // Make sure the IDs here EXACTLY match the IDs in your activity_admin_dashboard.xml
        MaterialCardView cardCreateUserProfile = findViewById(R.id.cardCreateUserProfile);
        MaterialCardView cardRetrieveUserProfile = findViewById(R.id.cardRetrieveUserProfile);


        // --- Set Listeners for Existing Functionality ---

        // 1. Create User Account -> Navigates to the user creation page
        if (cardCreateUserAccount != null) {
            // NOTE: Your original code pointed to 'CreateUserAccountActivity.class'.
            // Based on our previous conversations, the correct file is 'AdminCreateUserActivity.class'.
            // I have corrected this for you.
            cardCreateUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminCreateUserActivity.class);
                startActivity(intent);
            });
        }

        // 2. Retrieve User Account -> Navigates to the user list
        if (cardRetrieveUserAccount != null) {
            cardRetrieveUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, ViewAllUsersActivity.class);
                startActivity(intent);
            });
        }

        // 3. Update User Account -> Also navigates to the user list
        if (cardUpdateUserAccount != null) {
            cardUpdateUserAccount.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, ViewAllUsersActivity.class);
                startActivity(intent);
            });
        }

        // 4. Manage Categories -> Navigates to the categories management page
        if (cardManageCategories != null) {
            cardManageCategories.setOnClickListener(v -> {
                // Make sure you have a 'CategoryManagementActivity.class' file for this to work
                Intent intent = new Intent(AdminDashboardActivity.this, CategoryManagementActivity.class);
                startActivity(intent);
            });
        }

        // --- FIX #2: Set Listeners for the User Profile Cards ---

        // This sets the listener for the "Create User Profile" card
        if (cardCreateUserProfile != null) {
            cardCreateUserProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, CreateUserProfileActivity.class);
                startActivity(intent);
            });
        }

        // This sets the listener for the "Retrieve User Profile" card
        if (cardRetrieveUserProfile != null) {
            // "Retrieving" profiles means viewing the list of all users.
            cardRetrieveUserProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, ViewAllUsersActivity.class);
                startActivity(intent);
            });
        }


        // 5. Logout Button
        if (btnAdminLogout != null) {
            btnAdminLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminDashboardActivity.this, "You have been logged out.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(AdminDashboardActivity.this, loginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish(); // Close the AdminDashboardActivity
            });
        }
    }
}
