package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.entity.HelpRequest;
import com.example.csit314sdm.R;
import com.example.csit314sdm.controller.RetrieveUserAccountController;
import com.example.csit314sdm.SimpleRequestAdapter;
import com.example.csit314sdm.entity.User;
import com.example.csit314sdm.controller.HelpRequestController;
import com.example.csit314sdm.controller.LogoutController;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

// This Activity now uses Controllers for data handling instead of direct Firebase calls,
// respecting the app's architecture and fixing data filtering issues.
public class PINHomeScreenActivity extends AppCompatActivity {

    private static final String TAG = "PINHomeScreen";

    // Controllers for data handling
    private HelpRequestController helpRequestController;
    private RetrieveUserAccountController retrieveUserAccountController;
    private LogoutController logoutController;

    private String currentPinId;

    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private MaterialToolbar topAppBar;
    private TextView tvWelcomeMessage, tvActiveRequests, tvCompleted;
    private RecyclerView recyclerViewActiveRequests;
    private Button btnCreateNewRequest, btnLogout;
    private ImageButton btnProfile;
    private CardView cardActiveRequests, cardCompleted;

    private SimpleRequestAdapter requestAdapter;
    private List<HelpRequest> helpRequestList; // The list that the adapter uses

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinhome_screen);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        currentPinId = currentUser.getUid();

        // Initialize controllers
        helpRequestController = new HelpRequestController();
        retrieveUserAccountController = new RetrieveUserAccountController();
        logoutController = new LogoutController();

        initializeUI();
        setupListeners();
        setupNavigationDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check user status on resume
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            handleLogout();
            return;
        }
        // Load fresh data every time the screen is shown
        loadUserData();
        loadDashboardData();
    }

    private void initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        topAppBar = findViewById(R.id.topAppBar);
        btnProfile = findViewById(R.id.btnProfile);
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnCreateNewRequest = findViewById(R.id.btnCreateNewRequest);
        cardActiveRequests = findViewById(R.id.cardActiveRequests);
        cardCompleted = findViewById(R.id.cardCompleted);
        tvActiveRequests = findViewById(R.id.tvActiveRequests);
        tvCompleted = findViewById(R.id.tvCompleted);
        recyclerViewActiveRequests = findViewById(R.id.recyclerViewActiveRequests);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerViewActiveRequests.setLayoutManager(new LinearLayoutManager(this));
        helpRequestList = new ArrayList<>(); // Initialize the list
        requestAdapter = new SimpleRequestAdapter(helpRequestList, this, request -> {
            Intent intent = new Intent(this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "PIN");
            startActivity(intent);
        });
        recyclerViewActiveRequests.setAdapter(requestAdapter);
        recyclerViewActiveRequests.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PinProfileActivity.class)));
        btnCreateNewRequest.setOnClickListener(v -> startActivity(new Intent(this, CreateRequestActivity.class)));

        // FIX: Pass the user ID to the activities so they can load the correct data.
        cardActiveRequests.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRequestsActivity.class);
            intent.putExtra("USER_ID", currentPinId);
            intent.putExtra("user_role", "PIN");
            startActivity(intent);
        });

        cardCompleted.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchHistoryActivity.class);
            intent.putExtra("USER_ID", currentPinId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(view -> handleLogout());
    }

    private void setupNavigationDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, topAppBar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already home
            } else if (id == R.id.nav_my_requests || id == R.id.nav_filter_requests) {
                Intent intent = new Intent(this, MyRequestsActivity.class);
                intent.putExtra("USER_ID", currentPinId); // Pass user ID
                intent.putExtra("user_role", "PIN");
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PinProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, PINSettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        loadNavHeaderData();
    }

    private void handleLogout() {
        logoutController.logoutUser();
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        retrieveUserAccountController.fetchUserById(currentPinId, new RetrieveUserAccountController.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                    String[] names = user.getFullName().split(" ");
                    tvWelcomeMessage.setText("Hello, " + names[0] + "!");
                } else {
                    tvWelcomeMessage.setText("Hello!");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching user welcome data", e);
                tvWelcomeMessage.setText("Hello!");
            }
        });
    }

    private void loadNavHeaderData() {
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        retrieveUserAccountController.fetchUserById(currentPinId, new RetrieveUserAccountController.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    navHeaderName.setText(user.getFullName() != null ? user.getFullName() : "Valued User");
                    navHeaderEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load nav header data", e);
            }
        });
    }

    // This method now uses the controllers to fetch data, fixing the filtering issues.
    private void loadDashboardData() {
        // Fetch active requests to show the top 3 and get the count
        helpRequestController.getFilteredHelpRequests("Active", currentPinId, "PIN", new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    tvActiveRequests.setText("Active Requests\n(" + requests.size() + ")");

                    // The entity already sorts by date, so we just take the top 3
                    int limit = Math.min(3, requests.size());
                    helpRequestList.clear();
                    helpRequestList.addAll(requests.subList(0, limit));
                    requestAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> Log.e(TAG, "Failed to load active requests: " + errorMessage));
            }
        });

        // Fetch history requests just to get the count
        helpRequestController.getFilteredHelpRequests("History", currentPinId, "PIN", new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    tvCompleted.setText("History\n(" + requests.size() + ")");
                });
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> Log.e(TAG, "Failed to load history count: " + errorMessage));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
