package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- FIX 1: Import Firestore classes instead of Realtime Database classes ---
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAllUsersActivity extends AppCompatActivity implements UserAdapter.OnItemClickListener {

    private static final String TAG = "ViewAllUsersActivity";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;

    // --- FIX 2: Use Firestore references ---
    private FirebaseFirestore db;
    private CollectionReference usersCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_users);

        recyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // --- FIX 3: Initialize Firestore ---
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users"); // Get a reference to the "users" collection

        setupRecyclerView();
        loadAllUsers();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this);
        recyclerView.setAdapter(userAdapter);
    }

    private void loadAllUsers() {
        Log.d(TAG, "loadAllUsers: Starting to load users from Firestore.");
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // --- FIX 4: Create and execute a Firestore query ---
        usersCollection.get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE); // Always hide the progress bar
                    recyclerView.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        Log.d(TAG, "onComplete: Successfully fetched user documents.");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert the document into a User object
                            User user = document.toObject(User.class);
                            // The document ID is the UID
                            user.setUid(document.getId());
                            userList.add(user);
                            Log.d(TAG, "onComplete: Parsed user: " + user.getEmail());
                        }
                        userAdapter.setUsers(userList);
                        Log.d(TAG, "onComplete: Finished processing " + userList.size() + " users.");
                    } else {
                        // This will be called if your Firestore rules block access
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(ViewAllUsersActivity.this, "Failed to load users: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onItemClick(User user) {
        Log.d(TAG, "onItemClick: Clicked on user with UID: " + user.getUid());
        // Go to the detail activity, passing the user's UID
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("USER_ID", user.getUid());
        startActivity(intent);
    }
}
