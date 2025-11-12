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

public class CsrDashboardActivity extends AppCompatActivity implements HelpRequestAdapter.OnSaveClickListener {

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
    private ShortlistHelpRequestController shortlistController;
    private UserProfileController userProfileController;
    private HelpRequestAdapter adapter;
    private String currentCsrId;
    private boolean isShowingSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_dashboard);

        controller = new HelpRequestController();
        shortlistController = new ShortlistHelpRequestController();
        userProfileController = new UserProfileController();
        categoryController = new CategoryController();

        // Safely get the user ID once on creation
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentCsrId = currentUser.getUid();
        } else {
            handleLogout();
            return;
        }

        initializeUI();
        setupNavigationDrawer();
        setupListeners();
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
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        });
        adapter.setOnSaveClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (isLoading) {
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (tvNoResults != null) tvNoResults.setVisibility(View.GONE);
        } else {
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        if (tvNoResults != null) {
            tvNoResults.setText(message);
            tvNoResults.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateRecyclerView(List<HelpRequest> requests, String noResultsMessage) {
        if (requests == null || requests.isEmpty()) {
            if (tvNoResults != null) {
                tvNoResults.setText(noResultsMessage);
                tvNoResults.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else {
            if (tvNoResults != null) tvNoResults.setVisibility(View.GONE);
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
                if (adapter != null) adapter.setRequests(requests);
            }
        }
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void populateFilterSpinners() {
        // Location spinner
        String[] locations = new String[]{"All", "Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setText(locations[0], false);

        // Dynamically load categories
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
                            CsrDashboardActivity.this,
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
                    Toast.makeText(CsrDashboardActivity.this, "Could not load categories: " + errorMessage, Toast.LENGTH_SHORT).show();
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
            tvListTitle.setText("Shortlisted Requests");
            isShowingSaved = true;
            loadSavedRequests();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (isShowingSaved) {
            loadSavedRequests();
        } else {
            loadActiveRequests(); // ADDED: Load active requests when not showing saved
        }
    }

    // ADDED: Method to load active requests
    private void loadActiveRequests() {
        showLoading(true);
        controller.getActiveHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateRecyclerView(requests, "There are no new active requests at the moment.");
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(errorMessage);
                });
            }
        });
    }

    private void performSearch() {
        String keyword = etSearchKeyword.getText().toString().trim();
        String location = spinnerLocation.getText().toString();
        String category = spinnerCategory.getText().toString();

        showLoading(true);

        shortlistController.searchShortlistedRequests(keyword, location, category, new ShortlistHelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateRecyclerView(requests, "No matching requests found.");
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(errorMessage);
                });
            }
        });
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_my_requests) {
                // CHANGED: Navigate to MyInProgressRequestsActivity instead of loading saved requests
                Intent intent = new Intent(CsrDashboardActivity.this, MyInProgressRequestsActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_logout) {
                handleLogout();
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(CsrDashboardActivity.this, HistoryActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(CsrDashboardActivity.this, CSRSettingsActivity.class);
                startActivity(intent);
            } else {
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
            tvWelcome.setText("Hello!");
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
    }

    private void loadUserDetails() {
        if (currentCsrId == null) {
            Toast.makeText(this, "Critical error: User is not logged in.", Toast.LENGTH_LONG).show();
            handleLogout();
            return;
        }

        // CORRECTED: Use User.UserCallback as expected by getUserById method
        userProfileController.getUserById(currentCsrId, new User.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setWelcomeMessage(user);
                    populateFilterSpinners();
                    tvListTitle.setText("Active Requests");
                    isShowingSaved = false;
                    loadActiveRequests();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CsrDashboardActivity.this, "Could not load user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    populateFilterSpinners();
                    tvListTitle.setText("Active Requests");
                    isShowingSaved = false;
                    loadActiveRequests();
                });
            }
        });
    }
    private void loadSavedRequests() {
        showLoading(true);
        shortlistController.getSavedHelpRequests(new ShortlistHelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateRecyclerView(requests, "You have no shortlisted requests.");
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(errorMessage);
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

    @Override
    public void onSaveClick(HelpRequest request, boolean isSaved) {
        if (isSaved) {
            shortlistController.unsaveRequest(request.getId(), new ShortlistHelpRequestController.ShortlistCallback() {
                @Override
                public void onShortlistSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(CsrDashboardActivity.this, "Request unsaved", Toast.LENGTH_SHORT).show();
                        if (isShowingSaved) {
                            loadSavedRequests();
                        }
                    });
                }

                @Override
                public void onShortlistFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(CsrDashboardActivity.this, "Failed to unsave: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            shortlistController.saveRequest(request.getId(), new ShortlistHelpRequestController.ShortlistCallback() {
                @Override
                public void onShortlistSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(CsrDashboardActivity.this, "Request saved to shortlist", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onShortlistFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(CsrDashboardActivity.this, "Failed to save: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
}