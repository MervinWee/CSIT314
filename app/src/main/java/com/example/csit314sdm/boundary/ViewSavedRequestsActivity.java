package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.HelpRequestAdapter;
import com.example.csit314sdm.entity.HelpRequest;
import com.example.csit314sdm.R;
import com.example.csit314sdm.controller.HelpRequestController;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ViewSavedRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelpRequestAdapter adapter;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private String currentCsrId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_saved_requests);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentCsrId = currentUser.getUid();
        } else {
            Toast.makeText(this, "You must be logged in to view saved requests.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        controller = new HelpRequestController();
        initializeUI();
        loadSavedRequests();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        // FIX: Corrected the RecyclerView ID
        recyclerView = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(ViewSavedRequestsActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        }, currentCsrId);
        recyclerView.setAdapter(adapter);
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
                    if (requests.isEmpty()) {
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
                    Toast.makeText(ViewSavedRequestsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}