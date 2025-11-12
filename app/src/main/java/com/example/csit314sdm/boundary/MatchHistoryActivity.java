package com.example.csit314sdm.boundary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.controller.HelpRequestController;
import com.example.csit314sdm.entity.HelpRequest;
import com.example.csit314sdm.PinMyRequestsAdapter;
import com.example.csit314sdm.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchHistoryActivity extends AppCompatActivity {

    private static final String TAG = "MatchHistoryActivity";

    private RecyclerView recyclerView;
    private PinMyRequestsAdapter adapter;
    private List<HelpRequest> historyList = new ArrayList<>();
    private TextView tvNoHistory;
    private ProgressBar progressBar;

    private Spinner categorySpinner;
    private EditText etFromDate, etToDate;
    private Button btnApplyFilter;

    private HelpRequestController controller;
    private Date fromDate, toDate;
    private String currentUserId; // This will hold the logged-in user's ID
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        // FIX: Get the currently logged-in user's ID to ensure we only show their history.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view match history.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        controller = new HelpRequestController();

        initializeUI();
        setupFilters();
        loadMatchHistory("All", null, null);
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_history);
        topAppBar.setNavigationOnClickListener(v -> finish());

        tvNoHistory = findViewById(R.id.tv_no_history);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PinMyRequestsAdapter(historyList, this, request -> {
            Intent intent = new Intent(this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "PIN");
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        categorySpinner = findViewById(R.id.spinner_history_category);
        etFromDate = findViewById(R.id.et_history_from_date);
        etToDate = findViewById(R.id.et_history_to_date);
        btnApplyFilter = findViewById(R.id.btn_apply_history_filter);
    }

    private void setupFilters() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_filter_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        etToDate.setOnClickListener(v -> showDatePickerDialog(false));

        btnApplyFilter.setOnClickListener(v -> {
            String category = categorySpinner.getSelectedItem().toString();
            loadMatchHistory(category, fromDate, toDate);
        });
    }

    private void showDatePickerDialog(boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if (isFromDate) {
                fromDate = calendar.getTime();
                etFromDate.setText(dateFormat.format(fromDate));
            } else {
                toDate = calendar.getTime();
                etToDate.setText(dateFormat.format(toDate));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // This method now correctly uses the logged-in user's ID to fetch THEIR history only.
    private void loadMatchHistory(String category, Date fromDate, Date toDate) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        String categoryFilter = "All".equals(category) ? null : category;

        // FIX: Now passing the currentUserId to the query to fetch the correct user's history.
        Query query = controller.getMatchHistoryQuery(categoryFilter, fromDate, toDate, currentUserId);
        HelpRequest.getAllRequests(query, new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    historyList.clear();
                    historyList.addAll(requests);
                    adapter.notifyDataSetChanged();

                    if (historyList.isEmpty()) {
                        tvNoHistory.setText("No history found for the selected filters.");
                        tvNoHistory.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoHistory.setText("Failed to load history.");
                    tvNoHistory.setVisibility(View.VISIBLE);
                    Toast.makeText(MatchHistoryActivity.this, "Failed to load history: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading history: " + errorMessage);
                });
            }
        });
    }
}