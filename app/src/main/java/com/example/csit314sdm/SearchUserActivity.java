package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

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

        // Initialize Controller, Adapter, and UI
        searchController = new SearchUserController();
        userAdapter = new UserAdapter();
        initializeUI();

        // Set up observers to listen for data changes from the controller
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

        // Setup RecyclerView
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
                performSearch();
            }
        });
    }

    private void setupObservers() {
        // Observe user list from the controller
        searchController.getUsersLiveData().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.setUsers(users);
                recyclerViewUsers.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
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
