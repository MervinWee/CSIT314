// File: C:/Users/suhai/StudioProjects/CSIT314/app/src/main/java/com/example/csit314sdm/ViewSavedRequestsActivity.java
// FINAL CORRECTED VERSION using the new SavedRequestsAdapter.

package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

// BOUNDARY: Displays a list of saved help requests to the CSR.
public class ViewSavedRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    // --- FIX: Use the new SavedRequestsAdapter ---
    private SavedRequestsAdapter adapter;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_saved_requests);

        controller = new HelpRequestController();
        initializeUI();
        loadSavedRequests();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- FIX: Initialize the new SavedRequestsAdapter ---
        adapter = new SavedRequestsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // --- FIX: The errors are now resolved because SavedRequestsAdapter has these methods ---
        adapter.setOnItemClickListener(request -> {
            Intent intent = new Intent(ViewSavedRequestsActivity.this, HelpRequestDetailActivity.class);
            // Use the getter method 'request.getId()'
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(intent);
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
                    if (requests == null || requests.isEmpty()) {
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
                    tvNoResults.setVisibility(View.VISIBLE);
                    tvNoResults.setText(errorMessage);
                    Toast.makeText(ViewSavedRequestsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
