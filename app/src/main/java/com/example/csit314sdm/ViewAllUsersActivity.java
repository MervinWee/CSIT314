package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewAllUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    // --- CHANGE 1: Use the new UserRoleAdapter ---
    private UserRoleAdapter userRoleAdapter;
    private ProgressBar progressBar;
    private UserProfileController userProfileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_users);

        // Initialization of views is correct
        recyclerView = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        userProfileController = new UserProfileController();
        setupRecyclerView();
        loadAllUsers();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- CHANGE 2: Define the click listener for the UserRoleAdapter ---
        UserRoleAdapter.OnItemClickListener clickListener = user -> {
            // This logic is correct: Launch the UserDetailActivity
            Intent intent = new Intent(ViewAllUsersActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            startActivity(intent);
        };

        // --- CHANGE 3: Create an instance of the new UserRoleAdapter ---
        userRoleAdapter = new UserRoleAdapter(clickListener);

        // Attach the empty adapter to the RecyclerView
        recyclerView.setAdapter(userRoleAdapter);
    }

    private void loadAllUsers() {
        progressBar.setVisibility(View.VISIBLE);
        userProfileController.getAllUsers(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                progressBar.setVisibility(View.GONE);
                // --- CHANGE 4: Populate the new adapter ---
                // The setUsers method exists on both adapters, so this line works perfectly.
                userRoleAdapter.setUsers(users);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ViewAllUsersActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
