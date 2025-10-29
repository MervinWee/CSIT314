// File: C:/Users/suhai/StudioProjects/CSIT314/app/src/main/java/com/example/csit314sdm/CsrDashboardActivity.java
// FINAL CORRECTED VERSION using the new CsrRequestAdapter.

package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
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
    private TextInputEditText etSearchKeyword;
    private AutoCompleteTextView spinnerLocation, spinnerCategory;
    private Button btnSearch;

    // --- Controllers & Adapters ---
    private HelpRequestController controller;
    private UserProfileController userProfileController;
    private CategoryController categoryController;
    // --- FIX: Use the new CsrRequestAdapter ---
    private CsrRequestAdapter adapter;

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
        loadUserDetails();
    }

    private void initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnDrawer = findViewById(R.id.btnDrawer);
        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);
        tvListTitle = findViewById(R.id.tvListTitle);
        tvWelcome = findViewById(R.id.tvWelcome);
        cardShortlisted = findViewById(R.id.cardShortlisted);
        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSearch = findViewById(R.id.btnSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // --- FIX: Initialize the new CsrRequestAdapter ---
        adapter = new CsrRequestAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // --- FIX: The errors are now resolved because CsrRequestAdapter has these methods ---
        adapter.setOnItemClickListener(request -> {
            Intent intent = new Intent(CsrDashboardActivity.this, HelpRequestDetailActivity.class);
            // Use the restored getter method 'request.getId()'
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(intent);
        });
    }

    private void populateFilterSpinners() {
        String[] locations = new String[]{"All", "Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setText(locations[0], false);

        categoryController.getAllCategories(new CategoryController.CategoryFetchCallback() {
            @Override
            public void onCategoriesFetched(List<Category> categories) {
                runOnUiThread(() -> {
                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("All");
                    for (Category category : categories) {
                        // Use the restored getter method
                        categoryNames.add(category.getName());
                    }
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            CsrDashboardActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                    );
                    spinnerCategory.setAdapter(categoryAdapter);
                    if (!categoryNames.isEmpty()) {
                        spinnerCategory.setText(categoryNames.get(0), false);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(CsrDashboardActivity.this, "Could not load categories: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setWelcomeMessage(User user) {
        // Use the restored getter methods
        String username = user.getFullName();
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Hello, " + username + "!");
        } else {
            tvWelcome.setText("Hello!");
        }
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        if (username != null && !username.isEmpty()) {
            navHeaderName.setText(username);
        }
        navHeaderEmail.setText(user.getEmail());
    }

    // --- All other methods below this line are correct and do not need changes ---
    private void setupListeners() { cardShortlisted.setOnClickListener(v -> { tvListTitle.setText("My Shortlisted Requests"); loadSavedRequests(); }); btnSearch.setOnClickListener(v -> performSearch()); etSearchKeyword.setOnEditorActionListener((v, actionId, event) -> { if (actionId == EditorInfo.IME_ACTION_SEARCH) { performSearch(); return true; } return false; }); }
    private void performSearch() { String keyword = etSearchKeyword.getText().toString().trim(); String location = spinnerLocation.getText().toString(); String category = spinnerCategory.getText().toString(); progressBar.setVisibility(View.VISIBLE); recyclerView.setVisibility(View.GONE); tvNoResults.setVisibility(View.GONE); controller.searchShortlistedRequests(keyword, location, category, new HelpRequestController.HelpRequestsLoadCallback() { @Override public void onRequestsLoaded(List<HelpRequest> requests) { runOnUiThread(() -> { progressBar.setVisibility(View.GONE); if (requests.isEmpty()) { tvNoResults.setText("No matching requests found."); tvNoResults.setVisibility(View.VISIBLE); } else { recyclerView.setVisibility(View.VISIBLE); adapter.setRequests(requests); } }); } @Override public void onDataLoadFailed(String errorMessage) { runOnUiThread(() -> { progressBar.setVisibility(View.GONE); tvNoResults.setText(errorMessage); tvNoResults.setVisibility(View.VISIBLE); Toast.makeText(CsrDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show(); }); } }); }
    private void setupNavigationDrawer() { navigationView.setNavigationItemSelectedListener(item -> { int itemId = item.getItemId(); if (itemId == R.id.nav_logout) { handleLogout(); } else if (itemId == R.id.nav_history) { Intent intent = new Intent(CsrDashboardActivity.this, HistoryActivity.class); startActivity(intent); } drawerLayout.closeDrawer(GravityCompat.START); return true; }); btnDrawer.setOnClickListener(v -> { if (drawerLayout.isDrawerOpen(GravityCompat.START)) { drawerLayout.closeDrawer(GravityCompat.START); } else { drawerLayout.openDrawer(GravityCompat.START); } }); }
    private void handleLogout() { FirebaseAuth.getInstance().signOut(); Intent intent = new Intent(CsrDashboardActivity.this, loginPage.class); intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent); finish(); }
    private void loadUserDetails() { FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); if (currentUser == null) return; userProfileController.getUserById(currentUser.getUid(), new UserProfileController.UserLoadCallback() { @Override public void onUserLoaded(User user) { runOnUiThread(() -> { setWelcomeMessage(user); populateFilterSpinners(); loadSavedRequests(); }); } @Override public void onDataLoadFailed(String errorMessage) { runOnUiThread(() -> { Toast.makeText(CsrDashboardActivity.this, "Could not load user profile.", Toast.LENGTH_SHORT).show(); populateFilterSpinners(); loadSavedRequests(); }); } }); }
    private void loadSavedRequests() { progressBar.setVisibility(View.VISIBLE); recyclerView.setVisibility(View.GONE); tvNoResults.setVisibility(View.GONE); controller.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() { @Override public void onRequestsLoaded(List<HelpRequest> requests) { runOnUiThread(() -> { progressBar.setVisibility(View.GONE); if (requests.isEmpty()) { tvNoResults.setText("You have no shortlisted requests."); tvNoResults.setVisibility(View.VISIBLE); } else { recyclerView.setVisibility(View.VISIBLE); adapter.setRequests(requests); } }); } @Override public void onDataLoadFailed(String errorMessage) { runOnUiThread(() -> { progressBar.setVisibility(View.GONE); tvNoResults.setText(errorMessage); tvNoResults.setVisibility(View.VISIBLE); Toast.makeText(CsrDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show(); }); } }); }
    @Override public void onBackPressed() { if (drawerLayout.isDrawerOpen(GravityCompat.START)) { drawerLayout.closeDrawer(GravityCompat.START); } else { super.onBackPressed(); } }
}
