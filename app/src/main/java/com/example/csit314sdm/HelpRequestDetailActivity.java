package com.example.csit314sdm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HelpRequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "REQUEST_ID";

    private ProgressBar progressBar;
    private TextView tvRequestType, tvStatus, tvDescription, tvLocation, tvPreferredTime, tvUrgency, tvPostedDate, tvDetailViewCount, tvDetailShortlistCount;
    private Button btnCancelRequest;
    private HelpRequestController detailController;

    private MaterialToolbar topAppBar;

    private HelpRequest currentRequest;
    private String currentRequestId;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_request_detail);

        currentRequestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        userRole = getIntent().getStringExtra("user_role");

        if (currentRequestId == null || currentRequestId.isEmpty()) {
            Toast.makeText(this, "Error: Request ID was not received.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        detailController = new HelpRequestController();
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequestDetails(currentRequestId, userRole);
    }

    private void initializeUI() {
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_request) {
                handleEditClick();
                return true;
            }
            return false;
        });

        progressBar = findViewById(R.id.progressBar);
        tvRequestType = findViewById(R.id.tvDetailRequestType);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvPreferredTime = findViewById(R.id.tvDetailPreferredTime);
        tvUrgency = findViewById(R.id.tvDetailUrgency);
        tvPostedDate = findViewById(R.id.tvDetailPostedDate);
        tvDetailViewCount = findViewById(R.id.tvDetailViewCount);
        tvDetailShortlistCount = findViewById(R.id.tvDetailShortlistCount);

        btnCancelRequest = findViewById(R.id.btnCancelRequest);
        btnCancelRequest.setOnClickListener(v -> showCancelConfirmationDialog());
    }

    private void loadRequestDetails(String requestId, String userRole) {
        progressBar.setVisibility(View.VISIBLE);
        detailController.getHelpRequestById(requestId, userRole, new HelpRequestController.SingleRequestLoadCallback() {
            @Override
            public void onRequestLoaded(HelpRequest request) {
                currentRequest = request;
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
        tvPreferredTime.setText(request.getPreferredTime());
        tvUrgency.setText(request.getUrgency());

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

        tvDetailViewCount.setText(numberFormat.format(request.getViewCount()));
        int shortlistCount = request.getSavedByCsrId() != null ? request.getSavedByCsrId().size() : 0;
        tvDetailShortlistCount.setText(String.format("%s companies", numberFormat.format(shortlistCount)));

        if (request.getCreationTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a z", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(request.getCreationTimestamp());
            if (cal.get(Calendar.ERA) == 0) { // BCE
                sdf.applyPattern("dd MMMM yyyy G 'at' hh:mm a z");
            }
            tvPostedDate.setText("Posted on: " + sdf.format(request.getCreationTimestamp()));
        } else {
            tvPostedDate.setText("Date not available");
        }

        boolean isPinUser = "PIN".equals(userRole);

        if (isPinUser) {
            if ("Open".equals(request.getStatus())) {
                btnCancelRequest.setVisibility(View.VISIBLE);
                topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(true);
            } else {
                btnCancelRequest.setVisibility(View.GONE);
                topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(false);
            }
        } else { // CSR user
            btnCancelRequest.setVisibility(View.GONE);
            topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(false);
        }
    }

    private void handleEditClick() {
        if (currentRequest == null) { return; }
        if ("Open".equals(currentRequest.getStatus())) {
            Intent intent = new Intent(this, EditHelpRequestActivity.class);
            intent.putExtra("REQUEST_ID", currentRequestId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Only 'Open' requests can be edited.", Toast.LENGTH_LONG).show();
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to permanently cancel this help request?")
                .setPositiveButton("Yes, Cancel It", (dialog, which) -> {
                    performDeleteRequest();
                })
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDeleteRequest() {
        Toast.makeText(this, "Cancelling request...", Toast.LENGTH_SHORT).show();
        detailController.cancelRequest(currentRequestId, new HelpRequestController.DeleteCallback() {
            @Override
            public void onDeleteSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(HelpRequestDetailActivity.this, "Request successfully cancelled.", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onDeleteFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(HelpRequestDetailActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
