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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class MyInProgressRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelpRequestAdapter adapter;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private String currentCsrId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_in_progress_requests);

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in. Returning to dashboard.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentCsrId = currentUser.getUid();

        controller = new HelpRequestController();
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInProgressRequests();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // FIX: Added currentCsrId as the second argument to the adapter's constructor
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(MyInProgressRequestsActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        }, currentCsrId);

        recyclerView.setAdapter(adapter);
    }

    private void loadInProgressRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        controller.getInProgressRequestsForCsr(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (requests == null || requests.isEmpty()) {
                        tvNoResults.setText("You have no in-progress requests.");
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
                    tvNoResults.setText(errorMessage);
                    tvNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(MyInProgressRequestsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}