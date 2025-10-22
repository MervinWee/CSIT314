package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// BOUNDARY: Displays a list of saved help requests to the CSR.
public class ViewSavedRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelpRequestAdapter adapter;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_saved_requests);

        // Initialize Controller and UI
        controller = new HelpRequestController();
        initializeUI();

        // Load the data
        loadSavedRequests();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HelpRequestAdapter(request -> {
            // TODO: Handle click to view request details
            Toast.makeText(this, "Clicked on: " + request.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadSavedRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                progressBar.setVisibility(View.GONE);
                if (requests.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setRequests(requests);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText(errorMessage); // Show error message
                Toast.makeText(ViewSavedRequestsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
