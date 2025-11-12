package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    // Controllers for all relevant use cases
    private SearchUserAccountController searchUserAccountController;
    private SearchUserProfileController searchUserProfileController;
    private RetrieveUserAccountController retrieveUserAccountController;
    private UserAdapter adapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ChipGroup chipGroupRoleFilter;

    private String currentSearchText = "";
    private String currentRole = "All";
    // This variable would determine which use case is active.
    private String searchMode = "ACCOUNT"; // Defaulting to ACCOUNT for this example

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        String screenTitle = getIntent().getStringExtra("SCREEN_TITLE");
        toolbar.setTitle(screenTitle != null ? screenTitle : "User Management");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Instantiating all necessary controllers
        searchUserAccountController = new SearchUserAccountController();
        searchUserProfileController = new SearchUserProfileController(); // Instantiated controller
        retrieveUserAccountController = new RetrieveUserAccountController();

        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        chipGroupRoleFilter = findViewById(R.id.chipGroupRoleFilter);

        setupRecyclerView();
        setupSearch();
        setupRoleFilter();

        performSearch();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(user -> {
            Intent intent = new Intent(UserManagementActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

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
            if (checkedId == R.id.chipAll) {
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

        // This method now dynamically calls the correct controller based on the context (searchMode)
        if ("ACCOUNT".equals(searchMode)) {
            searchUserAccountController.searchUsers(currentSearchText, currentRole, new SearchUserAccountController.UserCallback<List<User>>() {
                @Override public void onSuccess(List<User> users) { handleSuccess(users); }
                @Override public void onFailure(Exception e) { handleFailure(e); }
            });
        } else if ("PROFILE".equals(searchMode)) {
            searchUserProfileController.searchUsers(currentSearchText, currentRole, new SearchUserProfileController.UserCallback<List<User>>() {
                @Override public void onSuccess(List<User> users) { handleSuccess(users); }
                @Override public void onFailure(Exception e) { handleFailure(e); }
            });
        } else {
            // As a fallback, we can use the retrieve controller if it has the search method
            retrieveUserAccountController.searchUsers(currentSearchText, currentRole, new RetrieveUserAccountController.UserCallback<List<User>>() {
                @Override public void onSuccess(List<User> users) { handleSuccess(users); }
                @Override public void onFailure(Exception e) { handleFailure(e); }
            });
        }
    }

    // Helper method to handle successful data loading to avoid code duplication
    private void handleSuccess(List<User> users) {
        progressBar.setVisibility(View.GONE);
        adapter.setUsers(users);
    }

    // Helper method to handle failed data loading
    private void handleFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(UserManagementActivity.this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        performSearch();
    }
}
