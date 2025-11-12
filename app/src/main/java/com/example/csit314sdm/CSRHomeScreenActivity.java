package com.example.csit314sdm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.google.firebase.messaging.FirebaseMessaging;
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
import androidx.annotation.NonNull;
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

public class CSRHomeScreenActivity extends AppCompatActivity implements HelpRequestAdapter.OnSaveClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnDrawer;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults, tvListTitle, tvWelcome;
    private MaterialCardView cardActiveRequests, cardShortlisted, cardCompletedRequests;
    private MaterialCardView cardMyInProgress;
    private TextInputEditText etSearchKeyword;
    private AutoCompleteTextView spinnerLocation, spinnerCategory;
    private Button btnSearch;

    private ViewCategoriesController viewCategoriesController;
    private HelpRequestController controller;
    private UserManagementController userManagementController;
    private LoginController loginController;
    private LogoutController logoutController;
    private HelpRequestAdapter adapter;
    private String currentCsrId;
    private boolean isShowingSaved = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications permission was denied. You will not receive reminders.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csrhome_screen);

        controller = new HelpRequestController();
        userManagementController = new UserManagementController();
        viewCategoriesController = new ViewCategoriesController();
        loginController = new LoginController();
        logoutController = new LogoutController();

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
        askNotificationPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewCategoriesController != null) {
            viewCategoriesController.cleanup();
        }
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_my_requests) {
                Intent intent = new Intent(CSRHomeScreenActivity.this, MyInProgressRequestsActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_history) {
                Intent intent = new Intent(CSRHomeScreenActivity.this, HistoryActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_my_matches) {
                Intent intent = new Intent(CSRHomeScreenActivity.this, MyMatchesActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(CSRHomeScreenActivity.this, CSRSettingsActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_logout) {
                handleLogout();
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

    private void handleLogout() {
        logoutController.logoutUser(currentCsrId);
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CSRHomeScreenActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShowingSaved) {
            loadSavedRequests();
        } else {
            loadActiveRequests();
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
        cardActiveRequests = findViewById(R.id.cardActiveRequests);
        cardShortlisted = findViewById(R.id.cardShortlisted);
        cardCompletedRequests = findViewById(R.id.cardCompletedRequests);
        cardMyInProgress = findViewById(R.id.cardMyInProgress);

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
        });
        adapter.setOnSaveClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void populateFilterSpinners() {
        String[] locations = {"All", "Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setText(locations[0], false);

        viewCategoriesController.getAllCategories(new ViewCategoriesController.CategoryFetchCallback() {
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
                });
            }
        });
    }

    private void setupListeners() {
        cardMyInProgress.setOnClickListener(v -> {
            Intent intent = new Intent(CSRHomeScreenActivity.this, MyInProgressRequestsActivity.class);
            startActivity(intent);
        });

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

        showLoading(true);

        controller.searchShortlistedRequests(keyword, location, category, new HelpRequestController.HelpRequestsLoadCallback() {
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

    private void setWelcomeMessage(User user) {
        String username = user.getFullName();
        tvWelcome.setText("Hello, " + (username != null && !username.isEmpty() ? username : "User") + "!");

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        navHeaderName.setText(username != null && !username.isEmpty() ? username : "CSR Representative");
        navHeaderEmail.setText(user.getEmail());
    }

    private void loadUserDetails() {
        userManagementController.fetchUserById(currentCsrId, new UserManagementController.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setWelcomeMessage(user);

                    FirebaseMessaging.getInstance().subscribeToTopic(currentCsrId)
                            .addOnCompleteListener(task -> {
                                String msg = "Subscribed to topic: " + currentCsrId;
                                if (!task.isSuccessful()) {
                                    msg = "Subscription to topic failed: " + currentCsrId;
                                }
                                Log.d("FCM_TOPIC", msg);
                            });

                    populateFilterSpinners();
                    tvListTitle.setText("Active Requests");
                    isShowingSaved = false;
                    loadActiveRequests();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CSRHomeScreenActivity.this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
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
        controller.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
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

    private void loadCompletedRequests() {
        showLoading(true);
        userManagementController.fetchUserById(currentCsrId, new UserManagementController.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                String companyId = user.getCompanyId();
                if (companyId == null || companyId.isEmpty()) {
                    showError("Cannot fetch history: Your user profile is missing a Company ID.");
                    return;
                }

                controller.getCompletedHistory(companyId, null, null, "All", new HelpRequestController.HelpRequestsLoadCallback() {
                    @Override
                    public void onRequestsLoaded(List<HelpRequest> requests) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            updateRecyclerView(requests, "You have no completed requests.");
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
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Could not load user profile to fetch history.");
                });
            }
        });
    }

    private void updateRecyclerView(List<HelpRequest> requests, String noResultsMessage) {
        if (requests.isEmpty()) {
            tvNoResults.setText(noResultsMessage);
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setRequests(requests);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvNoResults.setText(message);
        tvNoResults.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveClick(HelpRequest request, boolean isSaved) {
        String action = isSaved ? "unsave" : "save";
        HelpRequestController.SaveCallback callback = new HelpRequestController.SaveCallback() {
            @Override
            public void onSaveSuccess() {
                Toast.makeText(CSRHomeScreenActivity.this, "Request " + (isSaved ? "unsaved" : "saved"), Toast.LENGTH_SHORT).show();
                if (isShowingSaved) {
                    loadSavedRequests();
                }
            }

            @Override
            public void onSaveFailure(String errorMessage) {
                Toast.makeText(CSRHomeScreenActivity.this, "Failed to " + action + ": " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        if (isSaved) {
            controller.unsaveRequest(request.getId(), callback);
        } else {
            controller.saveRequest(request.getId(), callback);
        }
    }
}
