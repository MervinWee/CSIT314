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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

// BOUNDARY: Displays a list of saved help requests to the CSR.
public final class ViewSavedRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelpRequestAdapter adapter;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    // The currentCsrId is no longer needed for the controller call,
    // but it's good to keep for other potential uses.
    private String currentCsrId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_saved_requests);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentCsrId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Error: No user is logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        controller = new HelpRequestController();
        initializeUI();
        loadSavedRequests();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HelpRequestAdapter(request -> {
            // Go to the detail screen when an item is clicked
            Intent intent = new Intent(ViewSavedRequestsActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadSavedRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        // --- START: THIS IS THE FIX ---
        // The currentCsrId argument is removed from the call, as the controller
        // now gets the ID internally.
        controller.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (requests.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        // Make sure to set a message when there are no results
                        tvNoResults.setText("You have no saved requests.");
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
        // --- END: THIS IS THE FIX ---
    }
}
