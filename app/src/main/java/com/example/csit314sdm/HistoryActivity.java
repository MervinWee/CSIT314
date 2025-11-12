package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelpRequestAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private TextInputEditText etFromDate, etToDate;
    private AutoCompleteTextView spinnerCategory;
    private Button btnSearchHistory;

    private HelpRequestController helpRequestController;
    private PlatformDataAccount platformDataAccount; // FIX: Changed from CategoryController
    private RetrieveUserAccountController retrieveUserAccountController;

    private Date fromDate, toDate;
    private String currentCsrId;
    private String companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentCsrId = currentUser.getUid();
        } else {
            Toast.makeText(this, "You must be logged in to view history.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        helpRequestController = new HelpRequestController();
        platformDataAccount = new PlatformDataAccount(); // FIX: Initialized PlatformDataAccount
        retrieveUserAccountController = new RetrieveUserAccountController();

        initializeUI();
        setupDatePickerListeners();
        populateCategorySpinner();
        fetchCompanyAndLoadHistory();

        btnSearchHistory.setOnClickListener(v -> loadCompletedRequests());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // FIX: Added onDestroy to detach the listener and prevent memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (platformDataAccount != null) {
            platformDataAccount.detachCategoryListener();
        }
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(topAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        progressBar = findViewById(R.id.progressBarHistory);
        tvNoResults = findViewById(R.id.tvNoHistoryResults);
        etFromDate = findViewById(R.id.etDateFrom);
        etToDate = findViewById(R.id.etDateTo);
        spinnerCategory = findViewById(R.id.spinnerHistoryCategory);
        btnSearchHistory = findViewById(R.id.btnApplyFilters);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDatePickerListeners() {
        etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        etToDate.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate.getTime());
            if (isFromDate) {
                fromDate = selectedDate.getTime();
                etFromDate.setText(formattedDate);
            } else {
                toDate = selectedDate.getTime();
                etToDate.setText(formattedDate);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // FIX: Updated to use PlatformDataAccount
    private void populateCategorySpinner() {
        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                runOnUiThread(() -> {
                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("All");
                    for (Category cat : categories) {
                        categoryNames.add(cat.getName());
                    }
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(HistoryActivity.this, android.R.layout.simple_dropdown_item_1line, categoryNames);
                    spinnerCategory.setAdapter(categoryAdapter);
                    if (!categoryNames.isEmpty()) {
                        spinnerCategory.setText(categoryNames.get(0), false);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HistoryActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCompanyAndLoadHistory() {
        showLoading(true);
        retrieveUserAccountController.fetchUserById(currentCsrId, new RetrieveUserAccountController.UserCallback<User>() {
            @Override
            public void onSuccess(User user) {
                companyId = user.getCompanyId();
                if (companyId == null || companyId.isEmpty()) {
                    showError("Cannot fetch history: Your user profile is missing a Company ID.");
                    return;
                }
                loadCompletedRequests();
            }

            @Override
            public void onFailure(Exception e) {
                showError("Cannot fetch history: Could not load your user profile.");
            }
        });
    }

    private void loadCompletedRequests() {
        showLoading(true);

        String category = spinnerCategory.getText().toString();
        if ("All".equals(category)) {
            category = null;
        }

        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(HistoryActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        }, currentCsrId);
        recyclerView.setAdapter(adapter);

        // FIX: Passing currentCsrId to the history method
        helpRequestController.getCompletedHistory(companyId, currentCsrId, fromDate, toDate, category, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (requests.isEmpty()) {
                        showError("No completed requests found for the selected criteria.");
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> showError(errorMessage));
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setText(message);
        tvNoResults.setVisibility(View.VISIBLE);
    }
}