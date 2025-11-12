package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyCompletedRequestsActivity extends AppCompatActivity {

    private MyCompletedRequestsController controller;
    private HelpRequestAdapter adapter;
    private PlatformDataAccount platformDataAccount;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private TextInputEditText etSearchKeyword;
    private AutoCompleteTextView spinnerLocation, spinnerCategory;
    private Button btnSearch;
    private String currentCsrId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_completed_requests);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentCsrId = currentUser.getUid();

        controller = new MyCompletedRequestsController();
        platformDataAccount = new PlatformDataAccount();

        initializeUI();
        setupListeners();
        populateFilterSpinners();
        performSearch(); // Initial search with no filters
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (platformDataAccount != null) {
            platformDataAccount.detachCategoryListener();
        }
    }

    private void initializeUI() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarMyCompletedRequests);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewMyCompleted);
        progressBar = findViewById(R.id.progressBarMyCompleted);
        tvNoResults = findViewById(R.id.tvNoResults);
        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSearch = findViewById(R.id.btnSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // FIX: Added currentCsrId to the constructor
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(MyCompletedRequestsActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        }, currentCsrId);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        etSearchKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void populateFilterSpinners() {
        String[] locations = {"All", "Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setText(locations[0], false);

        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                runOnUiThread(() -> {
                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("All");
                    for (Category category : categories) {
                        categoryNames.add(category.getName());
                    }
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            MyCompletedRequestsActivity.this,
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
            public void onError(String errorMessage) {
                Toast.makeText(MyCompletedRequestsActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String keyword = etSearchKeyword.getText().toString().trim();
        // Note: The controller currently only supports filtering by keyword.
        // The 'location' and 'category' spinners are ignored in the search logic.

        showLoading(true);

        // FIX: Corrected controller method call and callback type.
        controller.searchMyCompletedRequests(currentCsrId, keyword, new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (requests == null || requests.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    tvNoResults.setVisibility(View.VISIBLE);
                    tvNoResults.setText("Error: " + errorMessage);
                    Toast.makeText(MyCompletedRequestsActivity.this, "Failed to load requests: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
        }
    }
}
