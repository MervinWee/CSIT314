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

        searchController = new SearchUserController();

        initializeUI();
        setupObservers();

        searchController.searchUsers("", selectedRole);
    }

    private void initializeUI() {
        etSearchQuery = findViewById(R.id.etSearchQuery);
        chipGroupRoleFilter = findViewById(R.id.chipGroupRoleFilter);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnBack = findViewById(R.id.btnBack);


        UserAdapter.OnItemClickListener clickListener = user -> {

            Intent intent = new Intent(SearchUserActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            intent.putExtra("MODE", "ProfileMode");
            startActivity(intent);


        };


        userAdapter = new UserAdapter(clickListener);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        btnBack.setOnClickListener(v -> finish());

        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        chipGroupRoleFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                selectedRole = selectedChip.getText().toString();
            } else {
                selectedRole = "All";
            }
            performSearch();
        });
    }

    private void setupObservers() {
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
