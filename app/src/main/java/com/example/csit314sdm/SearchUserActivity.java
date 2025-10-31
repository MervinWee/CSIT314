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

        searchController = new SearchUserController();

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

        UserAdapter.OnItemClickListener clickListener = user -> {
            Intent intent = new Intent(SearchUserActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            startActivity(intent);
        };

        userAdapter = new UserAdapter(clickListener);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        btnBack.setOnClickListener(v -> finish());

        // --- FIX: Replaced the keyboard listener with a TextWatcher for instant search ---
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        chipGroupRoleFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedRole = "All";
            } else {
                int selectedChipId = checkedIds.get(0);
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
        searchController.getUsersLiveData().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.setUsers(users);
                recyclerViewUsers.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
                userAdapter.setUsers(new ArrayList<>());
                recyclerViewUsers.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });

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
