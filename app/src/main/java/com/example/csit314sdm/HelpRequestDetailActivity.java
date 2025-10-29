package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class HelpRequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID";

    private ProgressBar progressBar;
    private TextView tvRequestType, tvStatus, tvDescription, tvLocation, tvPreferredTime, tvUrgency, tvPostedDate;
    private HelpRequestDetailController detailController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_request_detail);

        String requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Error: Request ID was not received.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        detailController = new HelpRequestDetailController();
        initializeUI();
        loadRequestDetails(requestId);
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);
        tvRequestType = findViewById(R.id.tvDetailRequestType);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvPreferredTime = findViewById(R.id.tvDetailPreferredTime);
        tvUrgency = findViewById(R.id.tvDetailUrgency);
        tvPostedDate = findViewById(R.id.tvDetailPostedDate);
    }

    private void loadRequestDetails(String requestId) {
        progressBar.setVisibility(View.VISIBLE);
        detailController.getRequestById(requestId, new HelpRequestDetailController.RequestDetailCallback() {
            @Override
            public void onRequestLoaded(HelpRequest request) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    populateUI(request);
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void populateUI(HelpRequest request) {
        tvRequestType.setText(request.getCategory());
        tvStatus.setText(request.getStatus());
        tvDescription.setText(request.getDescription());
        tvLocation.setText(request.getLocation());
        // tvPreferredTime.setText(request.getPreferredTime()); // This method doesn't exist
        tvUrgency.setText(request.getUrgency());

        if (request.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a", Locale.getDefault());
            tvPostedDate.setText("Posted on: " + sdf.format(request.getCreatedAt()));
        } else {
            tvPostedDate.setText("Date not available");
        }
    }
}
