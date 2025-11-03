package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;

// *** FIX: Changed the import from the internal Firebase user to your custom User class ***
import com.example.csit314sdm.User;

import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private UserManagementController controller;
    private UserAdapter adapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ChipGroup chipGroupRoleFilter;
    private Button btnMigrateUsers;
    private PlatformDataAccount platformDataAccount;

    // Variables to hold the current filter state
    private String currentSearchText = "";
    private String currentRole = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        String screenTitle = getIntent().getStringExtra("SCREEN_TITLE");
        toolbar.setTitle(screenTitle != null ? screenTitle : "User Management");
        toolbar.setNavigationOnClickListener(v -> finish());

        controller = new UserManagementController();
        platformDataAccount = new PlatformDataAccount();
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        chipGroupRoleFilter = findViewById(R.id.chipGroupRoleFilter);
        btnMigrateUsers = findViewById(R.id.btnMigrateUsers);

        setupRecyclerView();
        setupSearch();
        setupRoleFilter();

        btnMigrateUsers.setOnClickListener(v -> migrateUsers());

        // Initial load of all users
        performSearch();
    }

    private void migrateUsers() {
        progressBar.setVisibility(View.VISIBLE);
        platformDataAccount.migrateUserCreationDate(new PlatformDataAccount.MigrationCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this, "Migration failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(user -> {
            Intent intent = new Intent(UserManagementActivity.this, UserDetailActivity.class);
            // This now refers to your custom User object, so getId() works.
            intent.putExtra("USER_ID", user.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // No action needed on submit, as search is live
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchText = newText;
                performSearch();
                return true;
            }
        });
    }

    private void setupRoleFilter() {
        chipGroupRoleFilter.setOnCheckedChangeListener((group, checkedId) -> {
            // Default to "All" if no chip is selected.
            if (checkedId == View.NO_ID) {
                currentRole = "All";
            } else if (checkedId == R.id.chipAll) {
                currentRole = "All";
            } else if (checkedId == R.id.chipAdmin) {
                currentRole = "User Admin";
            } else if (checkedId == R.id.chipPIN) {
                currentRole = "PIN";
            } else if (checkedId == R.id.chipCSR) {
                currentRole = "CSR";
            }
            performSearch();
        });
    }

    private void performSearch() {
        progressBar.setVisibility(View.VISIBLE);
        // The generic <List<User>> now correctly refers to your custom User class
        controller.searchUsers(currentSearchText, currentRole, new UserManagementController.UserCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                // The 'users' parameter is now the correct type, so this works.
                adapter.setUsers(users);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the user list every time the activity is resumed,
        // in case details were changed on the detail screen.
        performSearch();
    }
}
