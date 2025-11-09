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
    private UserProfileController userProfileController;
    private HelpRequestAdapter adapter;
    private String currentCsrId; // Added to store the user's ID safely

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_dashboard);

        controller = new HelpRequestController();
        userProfileController = new UserProfileController();
        categoryController = new CategoryController();

        // Safely get the user ID once on creation
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentCsrId = currentUser.getUid();
        } else {
            handleLogout(); // If user is null, they shouldn't be here
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
            intent.putExtra("user_role", "CSR"); // Pass the role
            startActivity(intent);
        });
        adapter.setOnSaveClickListener(this); // Set the save listener
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
                    // Provide a fallback with just "All" if loading fails
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
                    Toast.makeText(CsrDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_my_requests) {
                loadSavedRequests();
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
            // If there's no user ID, we can't proceed.
            Toast.makeText(this, "Critical error: User is not logged in.", Toast.LENGTH_LONG).show();
            handleLogout(); // Log out the user as a safety measure.
            return;
        }

        // Call the method with the correct callback interface from the User class
        userProfileController.getUserById(currentCsrId, new User.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // This is the success method
                runOnUiThread(() -> {
                    setWelcomeMessage(user);
                    populateFilterSpinners();
                    loadSavedRequests();
                });
            }

            @Override
            public void onFailure(Exception e) {
                // This is the failure method
                runOnUiThread(() -> {
                    Toast.makeText(CsrDashboardActivity.this, "Could not load user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Still try to load other components so the app doesn't just sit blank
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

    @Override
    public void onSaveClick(HelpRequest request, boolean isSaved) {
        if (isSaved) {

            controller.unsaveRequest(request.getId(), new HelpRequestController.SaveCallback() {
                @Override
                public void onSaveSuccess() {
                    Toast.makeText(CsrDashboardActivity.this, "Request unsaved", Toast.LENGTH_SHORT).show();
                    loadSavedRequests(); // Refresh the list
                }

                @Override
                public void onSaveFailure(String errorMessage) {
                    Toast.makeText(CsrDashboardActivity.this, "Failed to unsave: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            controller.saveRequest(request.getId(), new HelpRequestController.SaveCallback() {
                @Override
                public void onSaveSuccess() {
                    Toast.makeText(CsrDashboardActivity.this, "Request saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSaveFailure(String errorMessage) {
                    Toast.makeText(CsrDashboardActivity.this, "Failed to save: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
