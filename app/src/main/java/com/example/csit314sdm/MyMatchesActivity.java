package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MyMatchesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyMatchesAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoMatches;
    private MyMatchesController controller;
    private SearchMyMatchesController searchController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Matches");

        controller = new MyMatchesController();
        searchController = new SearchMyMatchesController();
        recyclerView = findViewById(R.id.recyclerViewMatches);
        progressBar = findViewById(R.id.progressBar);
        tvNoMatches = findViewById(R.id.tvNoMatches);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyMatchesAdapter(this::onMatchClicked);
        recyclerView.setAdapter(adapter);

        loadMatches();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadMatches() {
        progressBar.setVisibility(View.VISIBLE);
        String csrId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        controller.getMatchedPINs(csrId, new MyMatchesController.MatchedPINsCallback() {
            @Override
            public void onMatchedPINsReceived(List<User> pins) {
                progressBar.setVisibility(View.GONE);
                if (pins.isEmpty()) {
                    tvNoMatches.setVisibility(View.VISIBLE);
                } else {
                    adapter.setMatches(pins);
                    tvNoMatches.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                tvNoMatches.setText(message);
                tvNoMatches.setVisibility(View.VISIBLE);
            }
        });
    }

    private void searchMatches(String keyword, String location, String category) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoMatches.setVisibility(View.GONE);

        searchController.searchMyMatches(keyword, location, category, new SearchMyMatchesController.SearchMatchesCallback() {
            @Override
            public void onMatchesFound(List<HelpRequest> requests) {
                progressBar.setVisibility(View.GONE);
                if (requests.isEmpty()) {
                    tvNoMatches.setText("No matches found for your search.");
                    tvNoMatches.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    adapter.setMatches(new ArrayList<>()); // clear the adapter
                    tvNoMatches.setText(requests.size() + " completed requests found.");
                    tvNoMatches.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSearchFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoMatches.setText(errorMessage);
                tvNoMatches.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }



    private void onMatchClicked(User user) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("USER_ID", user.getId());
        intent.putExtra("MODE", "VIEW_ONLY"); // Add this line
        startActivity(intent);
    }
}
