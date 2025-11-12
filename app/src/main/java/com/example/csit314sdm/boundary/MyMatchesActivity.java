package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.MyMatchesAdapter;
import com.example.csit314sdm.R;
import com.example.csit314sdm.entity.User;
import com.example.csit314sdm.controller.MyMatchesController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MyMatchesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyMatchesAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoMatches;
    private MyMatchesController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Matches");
        }

        controller = new MyMatchesController();
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
        tvNoMatches.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            tvNoMatches.setText("You must be logged in to see your matches.");
            tvNoMatches.setVisibility(View.VISIBLE);
            return;
        }

        String csrId = currentUser.getUid();
        controller.getMatchedPINs(csrId, new MyMatchesController.MatchedPINsCallback() {
            @Override
            public void onMatchedPINsReceived(List<User> pins) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (pins == null || pins.isEmpty()) {
                        tvNoMatches.setText("No matches found.");
                        tvNoMatches.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        adapter.setMatches(pins);
                        tvNoMatches.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoMatches.setText("Error: " + message);
                    tvNoMatches.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
            }
        });
    }

    private void onMatchClicked(User user) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("USER_ID", user.getId());
        intent.putExtra("MODE", "VIEW_ONLY");
        startActivity(intent);
    }
}