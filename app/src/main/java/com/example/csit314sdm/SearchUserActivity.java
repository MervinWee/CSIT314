package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

        // Assuming SearchUserController uses LiveData, which is a good pattern
        searchController = new SearchUserController();

        initializeUI();
        setupObservers();

        // Perform an initial search to show all users
        performSearch();
    }

    private void initializeUI() {
        etSearchQuery = findViewById(R.id.etSearchQuery);
        chipGroupRoleFilter = findViewById(R.id.chipGroupRoleFilter);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnBack = findViewById(R.id.btnBack);

        UserAdapter.OnItemClickListener clickListener = user -> {
            Intent intent = new Intent(SearchUserActivity.this, UserDetailActivity.class);
            // *** FIX: Changed user.getUid() to user.getId() ***
            intent.putExtra("USER_ID", user.getId());
            startActivity(intent);
        };

        userAdapter = new UserAdapter(clickListener);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        btnBack.setOnClickListener(v -> finish());

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Search as the user types
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        chipGroupRoleFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedRole = "All";
                // Ensure the "All" chip is checked if nothing is, to provide clear user feedback
                group.check(R.id.chipAll);
            } else {
                int selectedChipId = checkedIds.get(0); // get(0) is safe because isEmpty() is false
                if (selectedChipId == R.id.chipAll) {
                    selectedRole = "All";
                } else if (selectedChipId == R.id.chipAdmin) {
                    selectedRole = "Admin";
                } else if (selectedChipId == R.id.chipPIN) {
                    selectedRole = "PIN";
                } else if (selectedChipId == R.id.chipCSR) {
                    selectedRole = "CSR";
                }
            }
            performSearch();
        });
    }

    private void setupObservers() {
        // This assumes your SearchUserController uses LiveData to communicate with the UI
        searchController.getUsersLiveData().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.setUsers(users);
                recyclerViewUsers.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
                // If the list is null or empty, clear the adapter and show "No results"
                userAdapter.setUsers(new ArrayList<>());
                recyclerViewUsers.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });

        searchController.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performSearch() {
        String query = etSearchQuery.getText() != null ? etSearchQuery.getText().toString().trim() : "";
        searchController.searchUsers(query, selectedRole);
    }
}
