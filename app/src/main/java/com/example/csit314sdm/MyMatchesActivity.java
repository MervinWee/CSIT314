package com.example.csit314sdm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyMatchesActivity extends AppCompatActivity {

    private static final String TAG = "MyMatchesActivity";
    private RecyclerView recyclerView;
    private MyMatchesAdapter adapter;
    private final List<Match> matchList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoMatches;
    private FirebaseFirestore db;
    private String currentCsrId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to see your matches.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentCsrId = currentUser.getUid();

        initializeUI();
        loadMatches();
    }

    private void initializeUI() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarMyMatches);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewMatches);
        progressBar = findViewById(R.id.progressBarMatches);
        tvNoMatches = findViewById(R.id.tvNoMatches);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyMatchesAdapter(matchList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadMatches() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoMatches.setVisibility(View.GONE);
        Log.d(TAG, "Starting to load matches for CSR ID: " + currentCsrId);

        db.collection("users").document(currentCsrId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String companyId = documentSnapshot.getString("companyId");
                if (companyId != null && !companyId.isEmpty()) {
                    Log.d(TAG, "Found company ID: " + companyId);
                    fetchCompletedRequestsForCompany(companyId);
                } else {
                    showError("Your user profile does not have a company ID.");
                }
            } else {
                showError("User profile not found.");
            }
        }).addOnFailureListener(e -> showError("Failed to load user profile: " + e.getMessage()));
    }

    private void fetchCompletedRequestsForCompany(String companyId) {
        Log.d(TAG, "Querying 'help_requests' where 'companyId' == '" + companyId + "' AND 'status' == 'Completed'");
        db.collection("help_requests")
                .whereEqualTo("companyId", companyId)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query successful. Found " + queryDocumentSnapshots.size() + " completed requests for this company.");
                    if (queryDocumentSnapshots.isEmpty()) {
                        showError("No completed requests found for your company.");
                        return;
                    }

                    Map<String, List<HelpRequest>> requestsByPin = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HelpRequest request = document.toObject(HelpRequest.class);
                        String pinId = request.getSubmittedBy();
                        if (pinId != null && !pinId.isEmpty()) {
                            requestsByPin.computeIfAbsent(pinId, k -> new ArrayList<>()).add(request);
                        }
                    }
                    Log.d(TAG, "Grouped requests by PIN. Resulting map size: " + requestsByPin.size());

                    if (requestsByPin.isEmpty()) {
                        showError("No valid PINs found in completed requests.");
                        return;
                    }

                    processMatches(requestsByPin);

                }).addOnFailureListener(e -> showError("Failed to load completed requests: " + e.getMessage()));
    }

    private void processMatches(Map<String, List<HelpRequest>> requestsByPin) {
        List<String> pinIds = requestsByPin.keySet().stream()
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toList());
        Log.d(TAG, "Processing " + pinIds.size() + " unique PIN IDs: " + pinIds.toString());

        if (pinIds.isEmpty()) {
            showError("No valid PINs to display.");
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < pinIds.size(); i += 10) {
            List<String> chunk = pinIds.subList(i, Math.min(i + 10, pinIds.size()));
            Log.d(TAG, "Fetching user profiles for chunk: " + chunk.toString());
            tasks.add(db.collection("users").whereIn(FieldPath.documentId(), chunk).get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
            Map<String, String> pinIdToNameMap = new HashMap<>();
            for (Task<?> task : allTasks.getResult()) {
                if (task.isSuccessful()) {
                    QuerySnapshot userSnaps = (QuerySnapshot) task.getResult();
                    if (userSnaps != null) {
                        for (QueryDocumentSnapshot userDoc : userSnaps) {
                            pinIdToNameMap.put(userDoc.getId(), userDoc.getString("fullName"));
                        }
                    }
                } else {
                    Log.e(TAG, "A task to fetch user names failed.", task.getException());
                }
            }
            Log.d(TAG, "Successfully fetched details for " + pinIdToNameMap.size() + " PINs.");

            matchList.clear();
            for (Map.Entry<String, List<HelpRequest>> entry : requestsByPin.entrySet()) {
                String pinId = entry.getKey();
                List<HelpRequest> requests = entry.getValue();

                if (pinIdToNameMap.containsKey(pinId)) {
                    int matchCount = requests.size();
                    Date lastInteraction = requests.stream()
                            .map(HelpRequest::getCreationTimestamp)
                            .filter(Objects::nonNull)
                            .max(Date::compareTo)
                            .orElse(null);

                    String pinName = pinIdToNameMap.get(pinId);
                    matchList.add(new Match(pinId, pinName, matchCount, lastInteraction));
                }
            }
            Log.d(TAG, "Final match list size: " + matchList.size());

            if (matchList.size() > 1) {
                Collections.sort(matchList, (m1, m2) -> Integer.compare(m2.getMatchCount(), m1.getMatchCount()));
            }

            updateUIAfterLoading();
        });
    }

    private void updateUIAfterLoading() {
        progressBar.setVisibility(View.GONE);
        if (matchList.isEmpty()) {
            Log.d(TAG, "Match list is empty. Showing 'No matches found'.");
            tvNoMatches.setText("No matches found.");
            tvNoMatches.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Match list has items. Displaying RecyclerView.");
            tvNoMatches.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showError(String message) {
        Log.e(TAG, "Displaying error: " + message);
        progressBar.setVisibility(View.GONE);
        tvNoMatches.setText(message);
        tvNoMatches.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}
