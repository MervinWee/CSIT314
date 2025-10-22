package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CsrDashboardActivity extends AppCompatActivity {

    // --- UI Elements ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnDrawer;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults, tvListTitle, tvWelcome;
    private MaterialCardView cardShortlisted;

    // --- Controllers ---
    private HelpRequestController controller;
    private UserProfileController userProfileController;
    private HelpRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_dashboard);

        // --- Initialize Controllers ---
        controller = new HelpRequestController();
        userProfileController = new UserProfileController();

        // --- Initialize UI and Setup Listeners ---
        initializeUI();
        setupNavigationDrawer();
        setupListeners();

        // --- Load Initial Data ---
        loadUserDetails();
        loadSavedRequests();
    }

    private void initializeUI() {
        // Drawer and Navigation
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnDrawer = findViewById(R.id.btnDrawer);

        // Main content
        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);
        tvListTitle = findViewById(R.id.tvListTitle);
        tvWelcome = findViewById(R.id.tvWelcome);
        cardShortlisted = findViewById(R.id.cardShortlisted);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Replace the old Toast with the logic to start the new activity
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(CsrDashboardActivity.this, HelpRequestDetailActivity.class);
            // Pass the unique ID of the clicked request to the detail activity
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                handleLogout();
            } else {
                // Handle other menu items here with Toasts for now
                Toast.makeText(CsrDashboardActivity.this, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }

            // Close the drawer after an item is tapped
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Make the hamburger icon open the drawer
        btnDrawer.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setWelcomeMessage(User user) {
        // Update main screen welcome text
        String username = user.getFullName();
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Hello, " + username + "!");
        } else {
            tvWelcome.setText("Hello! Your Request");
        }

        // Update the header in the navigation drawer
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        if (username != null && !username.isEmpty()) {
            navHeaderName.setText(username);
        } else {
            navHeaderName.setText("CSR Representative"); // Fallback
        }
        navHeaderEmail.setText(user.getEmail());
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CsrDashboardActivity.this, loginPage.class);
        // Clear the activity stack to prevent user from going back to the dashboard
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadUserDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        userProfileController.getUserById(currentUser.getUid(), new UserProfileController.UserLoadCallback() {
            @Override
            public void onUserLoaded(User user) {
                setWelcomeMessage(user);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                Toast.makeText(CsrDashboardActivity.this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        cardShortlisted.setOnClickListener(v -> {
            tvListTitle.setText("My Shortlisted Requests");
            loadSavedRequests();
        });
    }

    private void loadSavedRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                progressBar.setVisibility(View.GONE);
                if (requests.isEmpty()) {
                    tvNoResults.setText("You have no shortlisted requests.");
                    tvNoResults.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setRequests(requests);
                }
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoResults.setText(errorMessage);
                tvNoResults.setVisibility(View.VISIBLE);
                Toast.makeText(CsrDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Handle the back button press to close the drawer if it's open
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
