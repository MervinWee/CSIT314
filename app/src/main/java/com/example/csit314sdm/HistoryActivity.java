// File: C:/Users/suhai/StudioProjects/CSIT314/app/src/main/java/com/example/csit314sdm/HistoryActivity.java
// FINAL CORRECTED VERSION using the new HistoryAdapter.

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

import androidx.appcompat.app.ActionBar;
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

    // UI Elements
    private TextInputEditText etDateFrom, etDateTo;
    private AutoCompleteTextView spinnerCategory;
    private Button btnApplyFilters;
    private RecyclerView recyclerViewHistory;
    private ProgressBar progressBarHistory;
    private TextView tvNoHistoryResults;

    // Logic and Data
    private HelpRequestController controller;
    // --- FIX: Use the new HistoryAdapter ---
    private HistoryAdapter adapter;
    private Calendar fromDateCalendar = Calendar.getInstance();
    private Calendar toDateCalendar = Calendar.getInstance();
    private Date fromDate, toDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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

        btnApplyFilters.setOnClickListener(v -> fetchHistory());
        fetchHistory();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        // --- FIX: Initialize the new HistoryAdapter ---
        adapter = new HistoryAdapter(new ArrayList<>());

        // --- FIX: The errors are now resolved because HistoryAdapter has these methods ---
        adapter.setOnItemClickListener(request -> {
            Intent intent = new Intent(HistoryActivity.this, HelpRequestDetailActivity.class);
            // Use the getter method 'request.getId()'
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(intent);
        });

        recyclerViewHistory.setAdapter(adapter);
    }

    // --- All other methods below this line are correct and do not need changes ---

    private void fetchHistory() {
        setLoadingState(true);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            setLoadingState(false);
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_LONG).show();
            tvNoHistoryResults.setText("Please log in to view history.");
            tvNoHistoryResults.setVisibility(View.VISIBLE);
            return;
        }

        String userId = currentUser.getUid();
        String category = spinnerCategory.getText().toString();

        controller.getCompletedHistory(userId, fromDate, toDate, category, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    if (requests.isEmpty()) {
                        tvNoHistoryResults.setVisibility(View.VISIBLE);
                        recyclerViewHistory.setVisibility(View.GONE);
                    } else {
                        tvNoHistoryResults.setVisibility(View.GONE);
                        recyclerViewHistory.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvNoHistoryResults.setText("Error loading data.");
                    tvNoHistoryResults.setVisibility(View.VISIBLE);
                    Toast.makeText(HistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

    private void initializeViews() {
        etDateFrom = findViewById(R.id.etDateFrom);
        etDateTo = findViewById(R.id.etDateTo);
        spinnerCategory = findViewById(R.id.spinnerHistoryCategory);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        progressBarHistory = findViewById(R.id.progressBarHistory);
        tvNoHistoryResults = findViewById(R.id.tvNoHistoryResults);
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
