package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;

// 1. Implement the click listener interface
public class MyMatchesActivity extends AppCompatActivity implements MyMatchesAdapter.OnMatchClickListener {

    private MyMatchesController controller;
    private MyMatchesAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);

        controller = new MyMatchesController();

        MaterialToolbar toolbar = findViewById(R.id.toolbarMyMatches);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewMyMatches);
        progressBar = findViewById(R.id.progressBarMyMatches);
        tvNoMatches = findViewById(R.id.tvNoMatches);

        setupRecyclerView();
        loadMatches();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 2. Pass 'this' as the click listener to the adapter
        adapter = new MyMatchesAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void loadMatches() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoMatches.setVisibility(View.GONE);

        controller.getMatchesForCurrentUser(new HelpRequest.MyMatchesCallback() {
            @Override
            public void onMatchesLoaded(List<User> matchedUsers) {
                progressBar.setVisibility(View.GONE);
                if (matchedUsers == null || matchedUsers.isEmpty()) {
                    tvNoMatches.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setMatches(matchedUsers);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoMatches.setVisibility(View.VISIBLE);
                tvNoMatches.setText("Error: " + errorMessage);
                Toast.makeText(MyMatchesActivity.this, "Failed to load matches: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // 3. Implement the interface method
    @Override
    public void onMatchClick(User user) {
        // Create an intent to open UserDetailActivity in read-only mode
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("USER_ID", user.getId());
        intent.putExtra("MODE", "VIEW_ONLY"); // Pass a mode to indicate it's for viewing only
        startActivity(intent);
    }
}
