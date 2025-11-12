package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.R;
import com.example.csit314sdm.SimpleUserAdapter;
import com.example.csit314sdm.entity.User;
import com.example.csit314sdm.controller.SearchUserProfileController;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UserAccountsActivity extends AppCompatActivity {

    // Corrected to use the new specific controller
    private SearchUserProfileController controller;
    private SimpleUserAdapter adapter;
    private ProgressBar progressBar;
    private TextInputEditText etSearchUsers;

    private List<User> allUsers = new ArrayList<>();
    private String launchMode = "MANAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accounts);

        if (getIntent().hasExtra("MODE")) {
            launchMode = getIntent().getStringExtra("MODE");
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarUserAccounts);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Changed the title as requested
        if ("VIEW_ONLY".equals(launchMode)) {
            toolbar.setTitle("All User Profiles");
        } else {
            toolbar.setTitle("User Management");
        }

        // Corrected instantiation to the new controller
        controller = new SearchUserProfileController();
        progressBar = findViewById(R.id.progressBarUserAccounts);
        etSearchUsers = findViewById(R.id.etSearchUsers);

        setupRecyclerView();
        loadUsers();
        setupSearch();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewUserAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleUserAdapter(user -> {
            Intent intent = new Intent(UserAccountsActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getId());
            intent.putExtra("MODE", launchMode);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        // Corrected to use SearchUserProfileController and its callback
        controller.searchUsers("", "All", new SearchUserProfileController.UserCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                allUsers.clear();
                allUsers.addAll(users);
                adapter.setUsers(allUsers);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserAccountsActivity.this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        if (query.isEmpty()) {
            filteredList.addAll(allUsers);
        } else {
            for (User user : allUsers) {
                boolean nameMatches = user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerCaseQuery);
                boolean emailMatches = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery);

                if (nameMatches || emailMatches) {
                    filteredList.add(user);
                }
            }
        }
        adapter.setUsers(filteredList);
    }
}
