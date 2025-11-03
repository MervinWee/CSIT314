package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private static final String TAG = "MyRequestsActivity";

    private RecyclerView recyclerView;
    private PinMyRequestsAdapter adapter;
    private List<HelpRequest> requestList;
    private HelpRequestController controller; // Changed from ViewRequestsController
    private ProgressBar progressBar;
    private TextView tvNoRequests;
    private MaterialToolbar topAppBar;
    private ListenerRegistration firestoreListener;

    private String currentStatusFilter = "All"; // Default filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        topAppBar = findViewById(R.id.topAppBar_my_requests);
        recyclerView = findViewById(R.id.recycler_view_my_requests);
        progressBar = findViewById(R.id.progressBar_my_requests);
        tvNoRequests = findViewById(R.id.tv_no_requests);

        controller = new HelpRequestController(); // Changed from ViewRequestsController

        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter_requests) {
                showFilterDialog();
                return true;
            }
            return false;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("STATUS_FILTER")) {
            String filterValue = intent.getStringExtra("STATUS_FILTER");
            if ("History".equals(filterValue)) {
                // If the homepage sent "History", set the filter and title
                currentStatusFilter = "History";
                topAppBar.setTitle("My History");
            } else if ("Active".equals(filterValue)) {
                // If the homepage sent "Active", set the filter and title
                currentStatusFilter = "Active";
                topAppBar.setTitle("My Active Requests");
            }
        }
        // If no filter is passed, it defaults to "All", showing "My Help Requests"

        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // This will now load either all requests or just the history, based on the filter
        loadHelpRequestsWithListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void setupRecyclerView() {
        // This method is correct and does not need changes
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        adapter = new PinMyRequestsAdapter(requestList, this, request -> {
            Intent detailIntent = new Intent(MyRequestsActivity.this, HelpRequestDetailActivity.class);
            detailIntent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            startActivity(detailIntent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadHelpRequestsWithListener() {
        // This method is correct and does not need changes
        progressBar.setVisibility(View.VISIBLE);
        tvNoRequests.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        Query query = controller.getFilteredHelpRequestsQuery(currentStatusFilter); // Changed from viewController
        if (query == null) { /* ... handle no user ... */ return; }

        firestoreListener = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);
            if (e != null) { /* ... handle error ... */ return; }

            requestList.clear();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    HelpRequest request = doc.toObject(HelpRequest.class);
                    request.setId(doc.getId());
                    requestList.add(request);
                }
            }

            if (requestList.isEmpty()) {
                tvNoRequests.setText("No requests found matching your filter.");
                tvNoRequests.setVisibility(View.VISIBLE);
            } else {
                adapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showFilterDialog() {
        // This method is correct and does not need changes
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_requests, null);
        Spinner statusSpinner = dialogView.findViewById(R.id.spinner_status);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_filter_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Filter Requests by Status")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    currentStatusFilter = statusSpinner.getSelectedItem().toString();
                    topAppBar.setTitle("My Help Requests"); // Reset title after filtering
                    loadHelpRequestsWithListener();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
