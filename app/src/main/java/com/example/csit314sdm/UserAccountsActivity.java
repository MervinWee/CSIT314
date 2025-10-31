// File: app/src/main/java/com/example/csit314sdm/UserAccountsActivity.java
package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;

public class UserAccountsActivity extends AppCompatActivity {

    private UserManagementController controller;
    private SimpleUserAdapter adapter;
    private ProgressBar progressBar;

    private String launchMode = "MANAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accounts);

        if (getIntent().hasExtra("MODE")) {
            launchMode = getIntent().getStringExtra("MODE");
        }

        // --- THIS IS THE FIX ---
        // The toolbar variable must be initialized before it can be used.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        // ----------------------

        // Change the title based on the mode
        if ("VIEW_ONLY".equals(launchMode)) {
            toolbar.setTitle("All User Accounts");
        } else {
            toolbar.setTitle("User Management"); // A sensible default
        }

        // The rest of your code is correct and remains the same
        controller = new UserManagementController();
        progressBar = findViewById(R.id.progressBar);

        setupRecyclerView();
        loadUsers();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleUserAdapter(user -> {
            Intent intent = new Intent(UserAccountsActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            intent.putExtra("MODE", launchMode);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        controller.fetchAllUsers(new UserManagementController.UserCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                adapter.setUsers(users);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserAccountsActivity.this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
