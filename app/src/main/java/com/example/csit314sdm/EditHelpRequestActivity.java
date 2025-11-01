package com.example.csit314sdm;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditHelpRequestActivity extends AppCompatActivity {

    // FIX: Add variables for the new fields
    private TextInputEditText etRequestType, etDescription, etPreferredTime, etEditLocation;
    private AutoCompleteTextView actvEditUrgency;
    private Button btnUpdateHelpRequest;

    private FirebaseFirestore db;
    private String currentRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_help_request);

        currentRequestId = getIntent().getStringExtra("REQUEST_ID");
        if (currentRequestId == null || currentRequestId.isEmpty()) {
            Toast.makeText(this, "Error: Could not get request ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initializeUI();
        loadExistingData();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_edit_request);
        topAppBar.setNavigationOnClickListener(v -> finish());

        etRequestType = findViewById(R.id.etEditRequestType);
        etDescription = findViewById(R.id.etEditDescription);
        etPreferredTime = findViewById(R.id.etEditPreferredTime);
        // FIX: Find the new views
        etEditLocation = findViewById(R.id.etEditLocation);
        actvEditUrgency = findViewById(R.id.actvEditUrgency);

        btnUpdateHelpRequest = findViewById(R.id.btnUpdateHelpRequest);

        // --- Setup the Urgency Dropdown ---
        String[] urgencyLevels = getResources().getStringArray(R.array.urgency_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        actvEditUrgency.setAdapter(adapter);

        btnUpdateHelpRequest.setOnClickListener(v -> updateHelpRequest());
    }

    private void loadExistingData() {
        db.collection("help_requests").document(currentRequestId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HelpRequest request = documentSnapshot.toObject(HelpRequest.class);
                        if (request != null) {
                            etRequestType.setText(request.getCategory());
                            etDescription.setText(request.getDescription());
                            etPreferredTime.setText(request.getPreferredTime());
                            // FIX: Populate the new fields
                            etEditLocation.setText(request.getLocation());
                            // Set dropdown text. Must also setFilter to false to prevent dropdown from filtering.
                            actvEditUrgency.setText(request.getUrgencyLevel(), false);
                        }
                    } else {
                        Toast.makeText(this, "Request not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show());
    }

    private void updateHelpRequest() {
        String requestType = etRequestType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();
        // FIX: Get the new values from the new fields
        String location = etEditLocation.getText().toString().trim();
        String urgencyLevel = actvEditUrgency.getText().toString().trim();

        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty() || urgencyLevel.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("category", requestType);
        updates.put("title", requestType);
        updates.put("description", description);
        updates.put("preferredTime", preferredTime);
        // FIX: Add the new values to the update map
        updates.put("location", location);
        updates.put("urgencyLevel", urgencyLevel);

        db.collection("help_requests").document(currentRequestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the edit screen and go back to the details
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show());
    }
}
