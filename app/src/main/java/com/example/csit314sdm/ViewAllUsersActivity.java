package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewAllUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserRoleAdapter userRoleAdapter;
    private ProgressBar progressBar;
    private UserProfileController userProfileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_users);

        // Initialization of views
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

        // Define the click listener for when an item in the list is tapped
        UserRoleAdapter.OnItemClickListener clickListener = user -> {
            // Launch the UserDetailActivity for the clicked user
            Intent intent = new Intent(ViewAllUsersActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            startActivity(intent);
        };

        // Create an instance of the UserRoleAdapter, passing it the listener
        userRoleAdapter = new UserRoleAdapter(clickListener);

        // Attach the empty adapter to the RecyclerView
        recyclerView.setAdapter(userRoleAdapter);
    }

    private void loadAllUsers() {
        progressBar.setVisibility(View.VISIBLE);

        // Call the correct controller method to fetch all users
        userProfileController.getAllUsersWithProfileCheck(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                progressBar.setVisibility(View.GONE);
                // If the list is loaded successfully, pass it to the adapter
                userRoleAdapter.setUsers(users);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                // If loading fails, show an error message
                Toast.makeText(ViewAllUsersActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
