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

public class CSRHomeScreenActivity extends AppCompatActivity implements HelpRequestAdapter.OnSaveClickListener {

    // --- UI Elements ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnDrawer;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults, tvListTitle, tvWelcome;
    private MaterialCardView cardShortlisted, cardCompletedRequests, cardActiveRequests;
    private TextInputEditText etSearchKeyword;
    private AutoCompleteTextView spinnerLocation, spinnerCategory;
    private Button btnSearch;

    private CategoryController categoryController;

    // --- Controllers ---
    private HelpRequestController controller;
    private UserProfileController userProfileController;
    private HelpRequestAdapter adapter;

    private boolean isShowingSaved = true; // Track if we are showing saved requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csrhome_screen);

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
        cardCompletedRequests = findViewById(R.id.cardCompletedRequests);
        cardActiveRequests = findViewById(R.id.cardActiveRequests);

        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSearch = findViewById(R.id.btnSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(CSRHomeScreenActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        }, this);
        adapter.setOnSaveClickListener(this);
        recyclerView.setAdapter(adapter);
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
                        categoryNames.add(category.getName());
                    }
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            CSRHomeScreenActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                    );
                    spinnerCategory.setAdapter(categoryAdapter);
                    spinnerCategory.setText(categoryNames.get(0), false);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(CSRHomeScreenActivity.this, "Could not load categories: " + errorMessage, Toast.LENGTH_SHORT).show();
                    List<String> fallbackCategories = new ArrayList<>();
                    fallbackCategories.add("All");
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            CSRHomeScreenActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            fallbackCategories
                    );
                    spinnerCategory.setAdapter(categoryAdapter);
                });
            }
        });
    }


    private void setupListeners() {
        cardActiveRequests.setOnClickListener(v -> {
            tvListTitle.setText("Active Requests");
            isShowingSaved = false;
            loadActiveRequests();
        });

        cardShortlisted.setOnClickListener(v -> {
            tvListTitle.setText("Shortlisted Requests");
            isShowingSaved = true;
            loadSavedRequests();
        });

        cardCompletedRequests.setOnClickListener(v -> {
            tvListTitle.setText("Completed Requests");
            isShowingSaved = false;
            loadCompletedRequests();
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
                runOnUiThread(() -> {
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
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(CSRHomeScreenActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                handleLogout();
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(CSRHomeScreenActivity.this, HistoryActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(CSRHomeScreenActivity.this, CSRSettingsActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(CSRHomeScreenActivity.this, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(CSRHomeScreenActivity.this, loginPage.class);
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
                runOnUiThread(() -> {
                    setWelcomeMessage(user);
                    populateFilterSpinners();
                    loadSavedRequests();
                });
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(CSRHomeScreenActivity.this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> {
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
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(CSRHomeScreenActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadActiveRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.getActiveHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (requests.isEmpty()) {
                        tvNoResults.setText("There are no active requests at the moment.");
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(CSRHomeScreenActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadCompletedRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.getCompletedHistory(FirebaseAuth.getInstance().getUid(), null, null, "All", new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (requests.isEmpty()) {
                        tvNoResults.setText("You have no completed requests.");
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(CSRHomeScreenActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onSaveClick(HelpRequest request, boolean isSaved) {
        if (isSaved) {
            controller.unsaveRequest(request.getId(), new HelpRequestController.SaveCallback() {
                @Override
                public void onSaveSuccess() {
                    Toast.makeText(CSRHomeScreenActivity.this, "Unsaved successfully", Toast.LENGTH_SHORT).show();
                    if (isShowingSaved) {
                        loadSavedRequests();
                    }
                }

                @Override
                public void onSaveFailure(String errorMessage) {
                    Toast.makeText(CSRHomeScreenActivity.this, "Failed to unsave", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            controller.saveRequest(request.getId(), new HelpRequestController.SaveCallback() {
                @Override
                public void onSaveSuccess() {
                    Toast.makeText(CSRHomeScreenActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSaveFailure(String errorMessage) {
                    Toast.makeText(CSRHomeScreenActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
