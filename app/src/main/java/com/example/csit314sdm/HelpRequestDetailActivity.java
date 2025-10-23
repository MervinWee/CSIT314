package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// BOUNDARY: Displays the full details of a single help request.
public class HelpRequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "com.example.csit314sdm.REQUEST_ID";

    private HelpRequestController controller;
    private ProgressBar progressBar;

    private TextView tvTitle, tvOrganization, tvCategory, tvDescription, tvUrgency, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_request_detail);

        controller = new HelpRequestController();
        initializeUI();

        String requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Error: Request ID is missing.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no ID
            return;
        }

        loadRequestDetails(requestId);
    }

    private void initializeUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
        getSupportActionBar().setTitle("Request Details");

        progressBar = findViewById(R.id.detail_progress_bar);
        tvTitle = findViewById(R.id.detail_tv_title);
        tvOrganization = findViewById(R.id.detail_tv_organization);
        tvCategory = findViewById(R.id.detail_tv_category);
        tvDescription = findViewById(R.id.detail_tv_description);
        tvUrgency = findViewById(R.id.detail_tv_urgency);
        tvStatus = findViewById(R.id.detail_tv_status);
    }

    // In HelpRequestDetailActivity.java

    private void loadRequestDetails(String requestId) {
        progressBar.setVisibility(View.VISIBLE);

        // --- THIS IS THE FIX ---
        // The callback interface was renamed to SingleRequestLoadCallback in the controller.
        // We must use the new name here.
        controller.getHelpRequestById(requestId, new HelpRequestController.SingleRequestLoadCallback() {
            @Override
            public void onRequestLoaded(HelpRequest request) {
                // This part is correct. We must ensure UI updates run on the main thread.
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    populateUI(request);
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                // This part is correct. We must ensure UI updates run on the main thread.
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Failed to load details: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }


    private void populateUI(HelpRequest request) {
        tvTitle.setText(request.getTitle());
        tvOrganization.setText(request.getOrganization());
        tvCategory.setText(request.getCategory());
        tvDescription.setText(request.getDescription());
        tvUrgency.setText(request.getUrgency());
        tvStatus.setText(request.getStatus());
    }

    // Handle clicks on the toolbar's back arrow
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
