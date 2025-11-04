package com.example.csit314sdm;

import android.content.DialogInterface; // Added for the confirmation dialog
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class HelpRequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "request_id";

    private MaterialToolbar topAppBar;
    private ProgressBar progressBar;
    private TextView tvRequestType, tvStatus, tvDescription, tvLocation, tvPreferredTime, tvUrgency, tvPostedDate, tvDetailViewCount, tvDetailShortlistCount, tvDetailPinName, tvDetailPinId, tvDetailPinPhone;
    private Button btnCancelRequest, btnCompleteRequest, btnAcceptRequest;
    private View layoutPinContactInfo; // The container for sensitive info

    private HelpRequestController detailController;
    private UserProfileController userProfileController;
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

        layoutPinContactInfo = findViewById(R.id.layoutPinContactInfo);
        tvDetailPinName = findViewById(R.id.tvDetailPinName);
        tvDetailPinId = findViewById(R.id.tvDetailPinId);
        tvDetailPinPhone = findViewById(R.id.tvDetailPinPhone);

        btnCancelRequest = findViewById(R.id.btnCancelRequest);
        btnCancelRequest.setOnClickListener(v -> handleCancelClick());

        btnCompleteRequest = findViewById(R.id.btnCompleteRequest);
        btnCompleteRequest.setOnClickListener(v -> showCompleteConfirmationDialog());

        btnAcceptRequest = findViewById(R.id.btnAcceptRequest);
        btnAcceptRequest.setOnClickListener(v -> showAcceptConfirmationDialog());
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

        btnCancelRequest.setVisibility(View.GONE);
        btnCompleteRequest.setVisibility(View.GONE);
        btnAcceptRequest.setVisibility(View.GONE);
        layoutPinContactInfo.setVisibility(View.GONE);
        topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(false);

        boolean isPinUser = "PIN".equals(userRole);
        boolean isCsrUser = "CSR".equals(userRole);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        // --- START: MODIFIED LOGIC ---
        if (isPinUser) {
            // Logic for the Person-in-Need (PIN)
            if ("Open".equals(request.getStatus())) {
                btnCancelRequest.setText("Cancel This Request"); // Set text for permanent cancel
                btnCancelRequest.setVisibility(View.VISIBLE);
                topAppBar.getMenu().findItem(R.id.action_edit_request).setVisible(true);
            } else if ("In-progress".equals(request.getStatus())) {
                // *** THIS IS THE NEW FEATURE ***
                // Show a button allowing the PIN to release the CSR.
                btnCancelRequest.setText("Release Assigned CSR"); // Change button text
                btnCancelRequest.setVisibility(View.VISIBLE); // Show the button
            }

        } else if (isCsrUser) {
            // Logic for the Customer Service Representative (CSR)
            if ("Open".equals(request.getStatus())) {
                btnAcceptRequest.setVisibility(View.VISIBLE);
            } else if ("In-progress".equals(request.getStatus())) {
                if (currentUserId.equals(request.getAcceptedByCsrId())) {
                    btnCompleteRequest.setVisibility(View.VISIBLE);
                    btnCancelRequest.setText("Cancel This Request"); // For CSR, it means they are cancelling
                    btnCancelRequest.setVisibility(View.VISIBLE);

                    // Also show PIN contact details to the assigned CSR
                    layoutPinContactInfo.setVisibility(View.VISIBLE);
                    tvDetailPinName.setText(request.getPinName());
                    tvDetailPinPhone.setText(request.getPinPhoneNumber());
                    if (request.getPinShortId() != null) {
                        tvDetailPinId.setText("PIN ID: " + request.getPinShortId());
                    }
                }
            }
        }
        // --- END: MODIFIED LOGIC ---
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

    // --- START: MODIFIED CLICK HANDLER ---
    private void handleCancelClick() {
        if ("PIN".equals(userRole)) {
            // If the request is 'In-progress', the PIN is releasing the CSR.
            // If it's 'Open', they are permanently cancelling their own request.
            if ("In-progress".equals(currentRequest.getStatus())) {
                showPinReleaseConfirmationDialog(); // A new confirmation dialog
            } else {
                showPinCancelConfirmationDialog(); // The original permanent cancel
            }
        } else if ("CSR".equals(userRole)) {
            // If the user is a CSR, they are releasing it back to the pool.
            showCsrReleaseConfirmationDialog();
        }
    }
    // --- END: MODIFIED CLICK HANDLER ---

    // --- START: ADD THESE TWO NEW METHODS ---

    /**
     * Shows a confirmation dialog to the PIN user before they release an assigned CSR.
     */
    private void showPinReleaseConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Release Assigned CSR")
                .setMessage("Are you sure you want to release the current representative? This will make your request available for other representatives to accept.")
                .setPositiveButton("Yes, Release", (dialog, which) -> performPinReleaseRequest())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Calls the new controller method for a PIN to release a CSR from a request.
     */
    private void performPinReleaseRequest() {
        progressBar.setVisibility(View.VISIBLE);
        detailController.releaseRequestByPin(currentRequestId, new HelpRequestController.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HelpRequestDetailActivity.this, "CSR Released. Your request is now active again.", Toast.LENGTH_LONG).show();
                    // Close the detail screen and go back to the list.
                    // The onResume() of the previous activity will show the updated list.
                    finish();
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
    // --- END: ADD THESE TWO NEW METHODS ---

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
                    finish();
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
                if (user != null && user.getCompanyId() != null && !user.getCompanyId().isEmpty()) {
                    String companyId = user.getCompanyId();

                    detailController.acceptRequest(currentRequestId, companyId, currentUserId, new HelpRequestController.UpdateCallback() {
                        @Override
                        public void onUpdateSuccess() {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                btnAcceptRequest.setEnabled(true);
                                Toast.makeText(HelpRequestDetailActivity.this, "Request Accepted!", Toast.LENGTH_SHORT).show();
                                loadRequestDetails(currentRequestId, userRole);
                            });
                        }

                        @Override
                        public void onUpdateFailure(String errorMessage) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                btnAcceptRequest.setEnabled(true);
                                Toast.makeText(HelpRequestDetailActivity.this, "Failed to accept request: " + errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    });

                } else {
                    progressBar.setVisibility(View.GONE);
                    btnAcceptRequest.setEnabled(true);
                    Toast.makeText(HelpRequestDetailActivity.this, "Could not find your company information.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                btnAcceptRequest.setEnabled(true);
                Toast.makeText(HelpRequestDetailActivity.this, "Failed to get user profile: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
