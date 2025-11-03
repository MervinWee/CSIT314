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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.csit314sdm.User; // Ensure this correct import is present

import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HelpRequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "REQUEST_ID";

    private ProgressBar progressBar;
    private TextView tvRequestType, tvStatus, tvDescription, tvLocation, tvPreferredTime, tvUrgency, tvPostedDate, tvDetailViewCount, tvDetailShortlistCount, tvDetailPinName, tvDetailPinId;
    // REMOVED btnCsrCancelRequest from declarations
    private Button btnCancelRequest, btnCompleteRequest, btnAcceptRequest;
    private HelpRequestController detailController;
    private UserProfileController userProfileController;

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
        userProfileController = new UserProfileController();

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
        tvDetailPinName = findViewById(R.id.tvDetailPinName);
        tvDetailPinId = findViewById(R.id.tvDetailPinId);

        // --- START: MODIFIED INITIALIZATION ---
        // This button now handles BOTH PIN cancel and CSR release actions.
        btnCancelRequest = findViewById(R.id.btnCancelRequest);
        btnCancelRequest.setOnClickListener(v -> handleCancelClick()); // Use a new handler method

        btnCompleteRequest = findViewById(R.id.btnCompleteRequest);
        btnCompleteRequest.setOnClickListener(v -> showCompleteConfirmationDialog());

        btnAcceptRequest = findViewById(R.id.btnAcceptRequest);
        btnAcceptRequest.setOnClickListener(v -> showAcceptConfirmationDialog());
        // --- END: MODIFIED INITIALIZATION ---
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
        tvUrgency.setText(request.getUrgencyLevel());
        tvDetailPinName.setText(request.getPinName());
        tvDetailPinId.setText(request.getPinShortId());

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        tvDetailViewCount.setText(numberFormat.format(request.getViewCount()));
        int shortlistCount = request.getSavedByCsrId() != null ? request.getSavedByCsrId().size() : 0;
        tvDetailShortlistCount.setText(String.format("%s companies", numberFormat.format(shortlistCount)));

        if (request.getCreationTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a", Locale.getDefault());
            tvPostedDate.setText("Posted on: " + sdf.format(request.getCreationTimestamp()));
        } else {
            tvPostedDate.setText("Date not available");
        }

        // --- START: UPDATED BUTTON VISIBILITY LOGIC ---
        // Hide all buttons by default
        btnCancelRequest.setVisibility(View.GONE);
        btnCompleteRequest.setVisibility(View.GONE);
        btnAcceptRequest.setVisibility(View.GONE);
        topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(false);

        boolean isPinUser = "PIN".equals(userRole);
        boolean isCsrUser = "CSR".equals(userRole);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        if (isPinUser) {
            // Logic for the Person-in-Need (PIN)
            if ("Open".equals(request.getStatus())) {
                btnCancelRequest.setVisibility(View.VISIBLE); // PIN can cancel their own open request
                topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(true);
            }
        } else if (isCsrUser) {
            // Logic for the Customer Service Representative (CSR)
            if ("Open".equals(request.getStatus())) {
                // Any CSR can accept an "Open" request
                btnAcceptRequest.setVisibility(View.VISIBLE);
            } else if ("In-progress".equals(request.getStatus())) {
                // For an "In-progress" request, check if it was accepted by the current CSR's company
                userProfileController.getUserById(currentUserId, new UserProfileController.UserLoadCallback() {
                    @Override
                    public void onUserLoaded(User user) {
                        runOnUiThread(() -> {
                            if (user.getCompanyId() != null && user.getCompanyId().equals(request.getCompanyId())) {
                                // This CSR's company accepted it, so show them the action buttons.
                                btnCompleteRequest.setVisibility(View.VISIBLE);
                                btnCancelRequest.setVisibility(View.VISIBLE); // Show the multi-purpose cancel button
                            }
                        });
                    }
                    @Override
                    public void onDataLoadFailed(String errorMessage) {
                        // Could not load profile, do nothing. Buttons remain hidden.
                    }
                });
            }
        }
        // --- END: UPDATED BUTTON VISIBILITY LOGIC ---
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

    // --- START: MODIFIED CANCEL/RELEASE ACTION METHODS ---

    /**
     * This new handler method checks the user's role and decides which cancel action to perform.
     */
    private void handleCancelClick() {
        if ("PIN".equals(userRole)) {
            // If the user is a PIN, show the permanent delete confirmation.
            showPinCancelConfirmationDialog();
        } else if ("CSR".equals(userRole)) {
            // If the user is a CSR, show the release confirmation.
            showCsrReleaseConfirmationDialog();
        }
    }

    // Action for the PIN to permanently cancel
    private void showPinCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to permanently cancel this help request?")
                .setPositiveButton("Yes, Cancel It", (dialog, which) -> performPinCancelRequest())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performPinCancelRequest() {
        progressBar.setVisibility(View.VISIBLE);
        detailController.cancelRequest(currentRequestId, new HelpRequestController.DeleteCallback() {
            @Override
            public void onDeleteSuccess() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Request successfully cancelled.", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onDeleteFailure(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // Action for the CSR to release the request back to the active list
    private void showCsrReleaseConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Release Request")
                .setMessage("Are you sure you want to release this request? It will become available for other CSRs again.")
                .setPositiveButton("Yes, Release It", (dialog, which) -> performCsrReleaseRequest())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performCsrReleaseRequest() {
        progressBar.setVisibility(View.VISIBLE);
        detailController.releaseRequestByCsr(currentRequestId, new HelpRequestController.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Request released successfully.", Toast.LENGTH_LONG).show();
                    finish(); // Close the activity and go back to the list
                });
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    // --- END: MODIFIED CANCEL/RELEASE ACTION METHODS ---

    // --- Methods for Complete and Accept remain the same ---
    private void showCompleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Complete Request")
                .setMessage("Are you sure you want to mark this request as completed?")
                .setPositiveButton("Yes, Complete It", (dialog, which) -> performCompleteRequest())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void performCompleteRequest() {
        progressBar.setVisibility(View.VISIBLE);
        detailController.updateRequestStatus(currentRequestId, "Completed", new HelpRequestController.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Request successfully marked as complete.", Toast.LENGTH_LONG).show();
                    loadRequestDetails(currentRequestId, userRole);
                });
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showAcceptConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Accept Request")
                .setMessage("Are you sure you want to accept this help request?")
                .setPositiveButton("Yes, Accept It", (dialog, which) -> performAcceptRequest())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void performAcceptRequest() {
        progressBar.setVisibility(View.VISIBLE);
        btnAcceptRequest.setEnabled(false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: You are not logged in.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            btnAcceptRequest.setEnabled(true);
            return;
        }

        String currentUserId = currentUser.getUid();
        userProfileController.getUserById(currentUserId, new UserProfileController.UserLoadCallback() {
            @Override
            public void onUserLoaded(User user) {
                String companyId = user.getCompanyId();
                if (companyId == null || companyId.isEmpty()) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnAcceptRequest.setEnabled(true);
                        Toast.makeText(HelpRequestDetailActivity.this, "Error: Your user profile is missing a Company ID.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                detailController.acceptRequest(currentRequestId, companyId, new HelpRequestController.UpdateCallback() {
                    @Override
                    public void onUpdateSuccess() {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnAcceptRequest.setEnabled(true);
                            Toast.makeText(HelpRequestDetailActivity.this, "Request successfully accepted!", Toast.LENGTH_LONG).show();
                            loadRequestDetails(currentRequestId, userRole);
                        });
                    }

                    @Override
                    public void onUpdateFailure(String errorMessage) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnAcceptRequest.setEnabled(true);
                            Toast.makeText(HelpRequestDetailActivity.this, "Error accepting request: " + errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnAcceptRequest.setEnabled(true);
                    Toast.makeText(HelpRequestDetailActivity.this, "Error: Could not load your user profile to get Company ID.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
