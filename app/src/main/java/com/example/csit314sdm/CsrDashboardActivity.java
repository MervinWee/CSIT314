package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.ArrayList;

public class CsrDashboardActivity extends AppCompatActivity {

    // --- UI Elements ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnDrawer;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults, tvListTitle, tvWelcome;
    private MaterialCardView cardShortlisted;
    private TextInputEditText etSearchKeyword;
    private AutoCompleteTextView spinnerLocation, spinnerCategory;
    private Button btnSearch;

    private CategoryController categoryController;

    // --- Controllers ---
    private HelpRequestController controller;
    private UserProfileController userProfileController;
    private HelpRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_dashboard);

        controller = new HelpRequestController();
        userProfileController = new UserProfileController();
        categoryController = new CategoryController();

        initializeUI();
        setupNavigationDrawer();
        setupListeners();

        // --- CORRECTED LOAD ORDER ---
        // 1. First, load the user's details.
        // 2. ONLY AFTER user details are loaded, then populate filters and load requests.
        loadUserDetails();
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

        // Search UI
        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSearch = findViewById(R.id.btnSearch);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(CsrDashboardActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void populateFilterSpinners() {
        // --- Location spinner remains the same (hardcoded) ---
        String[] locations = new String[]{"All", "Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setText(locations[0], false); // Set default value


        // --- DYNAMICALLY LOAD CATEGORIES ---
        categoryController.getAllCategories(new CategoryController.CategoryFetchCallback() {
            @Override
            public void onCategoriesFetched(List<Category> categories) {
                // This runs when categories are successfully fetched from Firestore
                runOnUiThread(() -> {
                    // Create a list of strings for the dropdown
                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("All"); // Add the default "All" option first

                    // Add the names of the fetched categories
                    for (Category category : categories) {
                        categoryNames.add(category.getName());
                    }

                    // Create the adapter for the category spinner
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            CsrDashboardActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                    );
                    spinnerCategory.setAdapter(categoryAdapter);
                    spinnerCategory.setText(categoryNames.get(0), false); // Set default value
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                // This runs if fetching fails
                runOnUiThread(() -> {
                    Toast.makeText(CsrDashboardActivity.this, "Could not load categories: " + errorMessage, Toast.LENGTH_SHORT).show();
                    // As a fallback, load an empty list with "All"
                    List<String> fallbackCategories = new ArrayList<>();
                    fallbackCategories.add("All");
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            CsrDashboardActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            fallbackCategories
                    );
                    spinnerCategory.setAdapter(categoryAdapter);
                });
            }
        });
    }


    private void setupListeners() {
        cardShortlisted.setOnClickListener(v -> {
            tvListTitle.setText("My Shortlisted Requests");
            loadSavedRequests(); // Reloads the full list
        });

        btnSearch.setOnClickListener(v -> performSearch());

        etSearchKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String keyword = etSearchKeyword.getText().toString().trim();
        String location = spinnerLocation.getText().toString();
        String category = spinnerCategory.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.searchShortlistedRequests(keyword, location, category, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> { // Ensure UI updates are on the main thread
                    progressBar.setVisibility(View.GONE);
                    if (requests.isEmpty()) {
                        tvNoResults.setText("No matching requests found.");
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> { // FIX: Wrap UI updates in runOnUiThread
                    progressBar.setVisibility(View.GONE);
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(CsrDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                handleLogout();
            }
            // --- ADD THIS BLOCK ---
            else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(CsrDashboardActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
            // --------------------
            else {
                Toast.makeText(CsrDashboardActivity.this, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        btnDrawer.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setWelcomeMessage(User user) {
        String username = user.getFullName();
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Hello, " + username + "!");
        } else {
            tvWelcome.setText("Hello! Your Request");
        }
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        if (username != null && !username.isEmpty()) {
            navHeaderName.setText(username);
        } else {
            navHeaderName.setText("CSR Representative");
        }
        navHeaderEmail.setText(user.getEmail());
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CsrDashboardActivity.this, loginPage.class);
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
                runOnUiThread(() -> { // Ensure UI updates are on the main thread
                    setWelcomeMessage(user);

                    // --- FIX: Now that user is loaded, populate other UI ---
                    populateFilterSpinners();
                    loadSavedRequests(); // Load initial full list
                });
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> { // FIX: Wrap UI updates in runOnUiThread
                    Toast.makeText(CsrDashboardActivity.this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
                    // Even if profile fails, load the rest of the app
                    populateFilterSpinners();
                    loadSavedRequests();
                });
            }
        });
    }

    private void loadSavedRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> { // Ensure UI updates are on the main thread
                    progressBar.setVisibility(View.GONE);
                    if (requests.isEmpty()) {
                        tvNoResults.setText("You have no shortlisted requests.");
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> { // FIX: Wrap UI updates in runOnUiThread
                    progressBar.setVisibility(View.GONE);
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(CsrDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
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
