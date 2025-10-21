package com.example.csit314sdm;

import android.content.Intent; // Import for future navigation
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Import for LayoutManager
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class SearchUserActivity extends AppCompatActivity {

    private SearchUserController searchController;
    private UserAdapter userAdapter;

    private TextInputEditText etSearchQuery;
    private ChipGroup chipGroupRoleFilter;
    private RecyclerView recyclerViewUsers;
    private TextView tvNoResults;
    private ImageButton btnBack;

    private String selectedRole = "All"; // Default filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        // --- CHANGE 1: Initialize the Adapter correctly BEFORE initializing the UI ---
        searchController = new SearchUserController();
        // The adapter is now initialized inside initializeUI() where it has access to the click listener logic.

        initializeUI();
        setupObservers();

        // Perform an initial search to show all users
        searchController.searchUsers("", selectedRole);
    }

    private void initializeUI() {
        etSearchQuery = findViewById(R.id.etSearchQuery);
        chipGroupRoleFilter = findViewById(R.id.chipGroupRoleFilter);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnBack = findViewById(R.id.btnBack);

        // --- CHANGE 2: Correctly set up the Adapter and RecyclerView ---
        // Define what happens when a user item is clicked
        UserAdapter.OnItemClickListener clickListener = user -> {
            // This is where you handle the click.
// NEW: Launch the UserDetailActivity
            Intent intent = new Intent(SearchUserActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid()); // Pass the unique ID of the clicked user
            startActivity(intent);


        };

        // Create the adapter instance, passing the listener to the constructor
        userAdapter = new UserAdapter(clickListener);

        // Setup RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this)); // Don't forget to set a layout manager!
        recyclerViewUsers.setAdapter(userAdapter);

        // Back button listener
        btnBack.setOnClickListener(v -> finish());

        // Search text listener
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // Chip group listener for role filtering
        chipGroupRoleFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                selectedRole = selectedChip.getText().toString();
            } else {
                // Handle case where all chips are deselected, if applicable
                selectedRole = "All";
            }
            performSearch();
        });
    }

    private void setupObservers() {
        // Observe user list from the controller
        searchController.getUsersLiveData().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.setUsers(users); // This method correctly updates the adapter
                recyclerViewUsers.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
                // If the user list is null or empty, clear the adapter
                userAdapter.setUsers(new ArrayList<>()); // Clear previous results
                recyclerViewUsers.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });

        // Observe error messages from the controller
        searchController.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        searchController.searchUsers(query, selectedRole);
    }
}
