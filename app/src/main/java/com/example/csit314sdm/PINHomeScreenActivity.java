package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PINHomeScreenActivity extends AppCompatActivity {

    private static final String TAG = "PINHomeScreen";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private MaterialToolbar topAppBar;

    private TextView tvWelcomeMessage, tvActiveRequests, tvCompleted;
    private RecyclerView recyclerViewActiveRequests;
    private Button btnLogout, btnCreateNewRequest;
    private ImageButton btnNotifications, btnProfile;

    private CardView cardActiveRequests, cardCompleted;

    private SimpleRequestAdapter requestAdapter;
    private List<HelpRequest> helpRequestList;
    private ListenerRegistration requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinhome_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
        setupListeners();
        setupNavigationDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            logoutUser();
            return;
        }
        loadUserData(currentUser.getUid());
        loadAllRequestDataWithListener(currentUser.getUid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (requestListener != null) {
            requestListener.remove();
        }
    }

    private void initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        topAppBar = findViewById(R.id.topAppBar);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnProfile = findViewById(R.id.btnProfile);
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnCreateNewRequest = findViewById(R.id.btnCreateNewRequest);
        cardActiveRequests = findViewById(R.id.cardActiveRequests);
        cardCompleted = findViewById(R.id.cardCompleted);
        tvActiveRequests = findViewById(R.id.tvActiveRequests);
        tvCompleted = findViewById(R.id.tvCompleted);
        recyclerViewActiveRequests = findViewById(R.id.recyclerViewActiveRequests);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerViewActiveRequests.setLayoutManager(new LinearLayoutManager(this));
        helpRequestList = new ArrayList<>();
        requestAdapter = new SimpleRequestAdapter(helpRequestList, this, request -> {
            Intent intent = new Intent(this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "PIN");
            startActivity(intent);
        });
        recyclerViewActiveRequests.setAdapter(requestAdapter);
        recyclerViewActiveRequests.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        btnNotifications.setOnClickListener(v -> Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show());
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PinProfileActivity.class)));
        btnCreateNewRequest.setOnClickListener(v -> startActivity(new Intent(this, CreateRequestActivity.class)));

        cardActiveRequests.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRequestsActivity.class);
            intent.putExtra("STATUS_FILTER", "Active");
            startActivity(intent);
        });

        cardCompleted.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(view -> logoutUser());
    }

    private void setupNavigationDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, topAppBar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "Already on Home screen", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_my_requests || id == R.id.nav_filter_requests) {
                startActivity(new Intent(this, MyRequestsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PinProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, PINSettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getEmail() != null) {
                navHeaderEmail.setText(currentUser.getEmail());
            }
            // --- START: FIX FOR CRASH ---
            // Manually build the user object to ensure the ID is set.
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                user.setId(documentSnapshot.getId()); // This prevents crashes
                                if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                                    navHeaderName.setText(user.getFullName());
                                } else {
                                    navHeaderName.setText("Valued User"); // Fallback text
                                }
                            }
                        }
                    });
            // --- END: FIX FOR CRASH ---
        }
    }

    private void loadUserData(String userId) {
        // --- START: FIX FOR CRASH ---
        // Manually build the user object here as well for safety and consistency.
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId()); // This prevents crashes
                            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                                // Greet user by first name if available
                                String[] names = user.getFullName().split(" ");
                                tvWelcomeMessage.setText("Hello, " + names[0] + "!");
                            } else {
                                tvWelcomeMessage.setText("Hello!");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user data", e));
        // --- END: FIX FOR CRASH ---
    }

    private void loadAllRequestDataWithListener(String userId) {
        requestListener = db.collection("help_requests").whereEqualTo("submittedBy", userId)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }
                    if (queryDocumentSnapshots == null) return;

                    int activeCount = 0;
                    int completedCount = 0; // This is the "History" count
                    List<HelpRequest> openRequests = new ArrayList<>();

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        HelpRequest request = snapshot.toObject(HelpRequest.class);
                        if (request != null) {
                            request.setId(snapshot.getId());
                            switch (request.getStatus()) {
                                case "Open":
                                case "In-progress":
                                    activeCount++;
                                    openRequests.add(request);
                                    break;
                                case "Completed":
                                case "Cancelled":
                                    completedCount++;
                                    break;
                            }
                        }
                    }

                    tvActiveRequests.setText("Active Requests\n(" + activeCount + ")");
                    tvCompleted.setText("History\n(" + completedCount + ")");

                    // Sort the list of open requests by date to show the most recent ones
                    Collections.sort(openRequests, (r1, r2) -> {
                        if (r1.getCreationTimestamp() == null || r2.getCreationTimestamp() == null) return 0;
                        return r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp());
                    });

                    // Limit the home screen to show a maximum of 3 recent requests
                    int limit = Math.min(3, openRequests.size());
                    helpRequestList.clear();
                    helpRequestList.addAll(openRequests.subList(0, limit));
                    requestAdapter.notifyDataSetChanged();
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, loginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
