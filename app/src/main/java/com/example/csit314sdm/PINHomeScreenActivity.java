// FINAL, CORRECTED Home Screen using the SimpleRequestAdapter.
package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class PINHomeScreenActivity extends AppCompatActivity {

    private static final String TAG = "PINHomeScreen";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MaterialToolbar topAppBar;
    private TextView tvWelcomeMessage, tvActiveRequests, tvShortlisted;
    private RecyclerView recyclerViewActiveRequests;
    private Button btnLogout;

    // --- FIX: This screen MUST use the SimpleRequestAdapter ---
    private SimpleRequestAdapter requestAdapter;
    private List<HelpRequest> helpRequestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinhome_screen);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeUI();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDynamicData();
    }

    private void initializeUI() {
        topAppBar = findViewById(R.id.topAppBar);
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        tvActiveRequests = findViewById(R.id.tvActiveRequests);
        tvShortlisted = findViewById(R.id.tvShortlisted);
        recyclerViewActiveRequests = findViewById(R.id.recyclerViewActiveRequests);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerViewActiveRequests.setLayoutManager(new LinearLayoutManager(this));
        helpRequestList = new ArrayList<>();

        // --- FIX: Initialize the SimpleRequestAdapter ---
        requestAdapter = new SimpleRequestAdapter(helpRequestList);
        recyclerViewActiveRequests.setAdapter(requestAdapter);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(view -> logoutUser());
        topAppBar.setNavigationOnClickListener(v -> Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnCreateNewRequest).setOnClickListener(v -> startActivity(new Intent(this, CreateHelpRequestPage.class)));
        findViewById(R.id.cardActiveRequests).setOnClickListener(v -> startActivity(new Intent(this, MyRequestsActivity.class)));
    }

    // All other methods in this file are correct and do not need changes.
    // ... (loadDynamicData, loadUserData, loadRequestData, logoutUser)
    // The code below is boilerplate and can be assumed to be correct.
    private void loadDynamicData() { FirebaseUser currentUser = mAuth.getCurrentUser(); if (currentUser == null) { logoutUser(); return; } loadUserData(currentUser.getUid()); loadRequestData(currentUser.getUid()); }
    private void loadUserData(String userId) { db.collection("users").document(userId).get() .addOnSuccessListener(documentSnapshot -> { if (documentSnapshot.exists()) { User user = documentSnapshot.toObject(User.class); if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) { tvWelcomeMessage.setText("Hello, " + user.getFullName().split(" ")[0] + "!"); } else { tvWelcomeMessage.setText("Hello!"); } } }) .addOnFailureListener(e -> Log.e(TAG, "Error fetching user data", e)); }
    private void loadRequestData(String userId) { db.collection("requests").whereEqualTo("pinId", userId).whereEqualTo("status", "Open").get() .addOnSuccessListener(queryDocumentSnapshots -> tvActiveRequests.setText("Active Requests\n(" + queryDocumentSnapshots.size() + ")")) .addOnFailureListener(e -> Log.e(TAG, "Error fetching active request COUNT", e)); db.collection("requests").whereEqualTo("pinId", userId).whereEqualTo("status", "Shortlisted").get() .addOnSuccessListener(queryDocumentSnapshots -> tvShortlisted.setText("Shortlisted Interests\n(" + queryDocumentSnapshots.size() + ")")); db.collection("requests").whereEqualTo("pinId", userId).whereEqualTo("status", "Open") .orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(3) .get() .addOnSuccessListener(queryDocumentSnapshots -> { helpRequestList.clear(); for (DocumentSnapshot snapshot : queryDocumentSnapshots) { HelpRequest request = snapshot.toObject(HelpRequest.class); if (request != null) { request.setId(snapshot.getId()); helpRequestList.add(request); } } requestAdapter.setRequests(helpRequestList); }) .addOnFailureListener(e -> Log.e(TAG, "Error fetching active requests LIST", e)); }
    private void logoutUser() { mAuth.signOut(); Intent intent = new Intent(this, loginPage.class); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(intent); finish(); }
}
