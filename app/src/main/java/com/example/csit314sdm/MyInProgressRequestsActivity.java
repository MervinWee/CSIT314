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
import java.util.List;

public class MyInProgressRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelpRequestAdapter adapter;
    private HelpRequestController controller;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_in_progress_requests);

        controller = new HelpRequestController();
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInProgressRequests();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar); // Assuming your layout has a toolbar with this ID
        topAppBar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewRequests); // Assuming this ID
        progressBar = findViewById(R.id.progressBar); // Assuming this ID
        tvNoResults = findViewById(R.id.tvNoResults); // Assuming this ID

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // The adapter is the same one you use elsewhere. It navigates to HelpRequestDetailActivity.
        adapter = new HelpRequestAdapter(request -> {
            Intent intent = new Intent(MyInProgressRequestsActivity.this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "CSR");
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadInProgressRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        // Use the new controller method
        controller.getInProgressRequestsForCsr(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (requests.isEmpty()) {
                        tvNoResults.setText("You have no active requests.");
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequests(requests); // The adapter now shows the sorted list
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
    