package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// BOUNDARY: This screen displays the Match History for a CSR's company.
public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity"; // TAG for logging

    // UI Elements
    private TextInputEditText etDateFrom, etDateTo;
    private AutoCompleteTextView spinnerCategory;
    private Button btnApplyFilters;
    private RecyclerView recyclerViewHistory;
    private ProgressBar progressBarHistory;
    private TextView tvNoHistoryResults;

    // Logic and Data
    private HelpRequestController controller;
    private HelpRequestAdapter adapter;
    private List<HelpRequest> historyList = new ArrayList<>();
    private Calendar fromDateCalendar = Calendar.getInstance();
    private Calendar toDateCalendar = Calendar.getInstance();
    private Date fromDate, toDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // --- Setup Toolbar & Back Button ---
        MaterialToolbar toolbar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        controller = new HelpRequestController();
        setupRecyclerView();
        setupSpinners();
        setupDatePickers();

        // The "Apply" button will now trigger the full fetch sequence
        btnApplyFilters.setOnClickListener(v -> fetchCsrCompanyIdAndLoadHistory());

        // Perform an initial fetch when the screen loads (with no date/category filters)
        fetchCsrCompanyIdAndLoadHistory();
    }

    // Handles the click on the back arrow in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * --- NEW METHOD ---
     * This is now the main entry point. It first fetches the CSR's companyId from their
     * profile, and only then proceeds to load the history.
     */
    private void fetchCsrCompanyIdAndLoadHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoadingState(true);

        // Get the companyId from the logged-in CSR's user document in Firestore
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String companyId = documentSnapshot.getString("companyId");
                        if (companyId != null && !companyId.isEmpty()) {
                            // Once we have the companyId, proceed to fetch the actual history data
                            fetchHistory(companyId);
                        } else {
                            setLoadingState(false);
                            Toast.makeText(HistoryActivity.this, "Could not find company ID for this user.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setLoadingState(false);
                        Toast.makeText(HistoryActivity.this, "User profile not found.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(HistoryActivity.this, "Failed to fetch user profile.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error fetching user profile for companyId", e);
                });
    }

    /**
     * --- UPDATED METHOD ---
     * This method is now called by fetchCsrCompanyIdAndLoadHistory and takes the companyId as a parameter.
     */
    private void fetchHistory(String companyId) {
        String category = spinnerCategory.getText().toString();
        // 'fromDate' and 'toDate' are already set by the DatePickerDialogs

        // Call the CSR-specific controller method with the correct companyId
        controller.getCompletedHistory(companyId, fromDate, toDate, category, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    historyList.clear();
                    historyList.addAll(requests);

                    if (historyList.isEmpty()) {
                        tvNoHistoryResults.setVisibility(View.VISIBLE);
                        recyclerViewHistory.setVisibility(View.GONE);
                    } else {
                        tvNoHistoryResults.setVisibility(View.GONE);
                        recyclerViewHistory.setVisibility(View.VISIBLE);
                        adapter.setRequests(historyList);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvNoHistoryResults.setText("Error loading data. Check logs for index errors.");
                    tvNoHistoryResults.setVisibility(View.VISIBLE);
                    Toast.makeText(HistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error from getCompletedHistory: " + errorMessage);
                });
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBarHistory.setVisibility(View.VISIBLE);
            tvNoHistoryResults.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            progressBarHistory.setVisibility(View.GONE);
        }
    }

    // --- Helper Methods for UI Initialization (No Changes Here) ---

    private void initializeViews() {
        etDateFrom = findViewById(R.id.etDateFrom);
        etDateTo = findViewById(R.id.etDateTo);
        spinnerCategory = findViewById(R.id.spinnerHistoryCategory);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        progressBarHistory = findViewById(R.id.progressBarHistory);
        tvNoHistoryResults = findViewById(R.id.tvNoHistoryResults);
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(HistoryActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(intent);
        }, this);
        recyclerViewHistory.setAdapter(adapter);
    }

    private void setupSpinners() {
        String[] categories = {"All", "Medical Transport", "Grocery Shopping Help", "Prescription Pickup", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setText("All", false);
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener fromDateListener = (view, year, month, dayOfMonth) -> {
            fromDateCalendar.set(Calendar.YEAR, year);
            fromDateCalendar.set(Calendar.MONTH, month);
            fromDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            fromDate = fromDateCalendar.getTime();
            updateLabel(etDateFrom, fromDateCalendar);
        };

        DatePickerDialog.OnDateSetListener toDateListener = (view, year, month, dayOfMonth) -> {
            toDateCalendar.set(Calendar.YEAR, year);
            toDateCalendar.set(Calendar.MONTH, month);
            toDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            toDate = toDateCalendar.getTime();
            updateLabel(etDateTo, toDateCalendar);
        };

        etDateFrom.setOnClickListener(v -> new DatePickerDialog(HistoryActivity.this, fromDateListener,
                fromDateCalendar.get(Calendar.YEAR), fromDateCalendar.get(Calendar.MONTH), fromDateCalendar.get(Calendar.DAY_OF_MONTH)).show());

        etDateTo.setOnClickListener(v -> new DatePickerDialog(HistoryActivity.this, toDateListener,
                toDateCalendar.get(Calendar.YEAR), toDateCalendar.get(Calendar.MONTH), toDateCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateLabel(TextInputEditText editText, Calendar calendar) {
        String myFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(sdf.format(calendar.getTime()));
    }
}
