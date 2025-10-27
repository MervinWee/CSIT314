package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomepageAsPinActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_as_pin);

        // Header Icons
        ImageView menuIcon = findViewById(R.id.menu_icon);
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        ImageView profileIcon = findViewById(R.id.profile_icon);

        // Main Buttons
        Button btnCreateNewRequest = findViewById(R.id.btn_create_new_request);
        Button btnActiveRequests = findViewById(R.id.btn_active_requests);
        Button btnShortlistedInterests = findViewById(R.id.btn_shortlisted_interests);
        Button btnCompletedMatches = findViewById(R.id.btn_completed_matches);

        // Request Card 1
        Button viewDetailsButton1 = findViewById(R.id.view_details_button_1);
        TextView editButton1 = findViewById(R.id.edit_button_1);

        // Request Card 2
        Button viewDetailsButton2 = findViewById(R.id.view_details_button_2);
        TextView cancelButton2 = findViewById(R.id.cancel_button_2);

        // Request Card 3
        Button viewDetailsButton3 = findViewById(R.id.view_details_button_3);
        TextView cancelButton3 = findViewById(R.id.cancel_button_3);

        // Set Click Listeners
        menuIcon.setOnClickListener(this);
        notificationIcon.setOnClickListener(this);
        profileIcon.setOnClickListener(this);
        btnCreateNewRequest.setOnClickListener(this);
        btnActiveRequests.setOnClickListener(this);
        btnShortlistedInterests.setOnClickListener(this);
        btnCompletedMatches.setOnClickListener(this);
        viewDetailsButton1.setOnClickListener(this);
        editButton1.setOnClickListener(this);
        viewDetailsButton2.setOnClickListener(this);
        cancelButton2.setOnClickListener(this);
        viewDetailsButton3.setOnClickListener(this);
        cancelButton3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.menu_icon) {
            showToast("Menu clicked");
        } else if (id == R.id.notification_icon) {
            showToast("Notifications clicked");
        } else if (id == R.id.profile_icon) {
            showToast("Profile clicked");
        } else if (id == R.id.btn_create_new_request) {
            showToast("Create New Request clicked");
        } else if (id == R.id.btn_active_requests) {
            showToast("Active Requests clicked");
        } else if (id == R.id.btn_shortlisted_interests) {
            showToast("Shortlisted Interests clicked");
        } else if (id == R.id.btn_completed_matches) {
            showToast("Completed Matches clicked");
        } else if (id == R.id.view_details_button_1 || id == R.id.view_details_button_2 || id == R.id.view_details_button_3) {
            showToast("View Details clicked");
        } else if (id == R.id.edit_button_1) {
            showToast("Edit clicked");
        } else if (id == R.id.cancel_button_2 || id == R.id.cancel_button_3) {
            showToast("Cancel clicked");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
