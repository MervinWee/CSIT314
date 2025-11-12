package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PinMyRequestsAdapter adapter;
    private List<HelpRequestEntity> requestList;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoRequests;
    private MaterialToolbar topAppBar;

    private String currentUserId;
    private String userRole; // Added userRole field
    // FIX: The default filter is now "Active" to ensure only open and in-progress requests are shown initially.
    private String currentStatusFilter = "Active";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        // Get userRole from intent
        userRole = getIntent().getStringExtra("user_role");

        topAppBar = findViewById(R.id.topAppBar_my_requests);
        recyclerView = findViewById(R.id.recycler_view_my_requests);
        progressBar = findViewById(R.id.progressBar_my_requests);
        tvNoRequests = findViewById(R.id.tv_no_requests);

        controller = new HelpRequestController();

        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter_requests) {
                showFilterDialog();
                return true;
            }
            return false;
        });

        // This logic handles opening the screen from a specific context (like "History").
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("STATUS_FILTER")) {
            String filterValue = intent.getStringExtra("STATUS_FILTER");
            if ("History".equals(filterValue)) {
                currentStatusFilter = "History";
                topAppBar.setTitle("My History");
            } else if ("Active".equals(filterValue)) {
                currentStatusFilter = "Active";
                topAppBar.setTitle("My Active Requests");
            }
        } else {
            // FIX: Sets the default title to match the default filter.
            topAppBar.setTitle("My Active Requests");
        }

        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadHelpRequests();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        adapter = new PinMyRequestsAdapter(requestList, this, request -> {
            Intent detailIntent = new Intent(MyRequestsActivity.this, HelpRequestDetailActivity.class);
            detailIntent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            detailIntent.putExtra("user_role", userRole);
            startActivity(detailIntent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadHelpRequests() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoRequests.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        controller.getFilteredHelpRequests(currentStatusFilter, currentUserId, userRole, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    requestList.clear();
                    requestList.addAll(requests);

                    if (requestList.isEmpty()) {
                        tvNoRequests.setText("No requests found matching your filter.");
                        tvNoRequests.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoRequests.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyRequestsActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    tvNoRequests.setText(errorMessage);
                    tvNoRequests.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_requests, null);
        Spinner statusSpinner = dialogView.findViewById(R.id.spinner_status);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_filter_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Pre-select the current filter in the spinner
        if (currentStatusFilter != null) {
            int spinnerPosition = statusAdapter.getPosition(currentStatusFilter);
            statusSpinner.setSelection(spinnerPosition);
        }

        new AlertDialog.Builder(this)
                .setTitle("Filter Requests by Status")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    currentStatusFilter = statusSpinner.getSelectedItem().toString();
                    // FIX: Dynamically update the title based on the selected filter for a better user experience.
                    if ("All".equals(currentStatusFilter)) {
                        topAppBar.setTitle("All My Requests");
                    } else if ("History".equals(currentStatusFilter)) {
                        topAppBar.setTitle("My History");
                    } else {
                        topAppBar.setTitle("My " + currentStatusFilter + " Requests");
                    }
                    loadHelpRequests(); // Reload data with the new filter
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
