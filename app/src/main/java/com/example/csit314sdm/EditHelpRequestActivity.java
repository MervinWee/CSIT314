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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditHelpRequestActivity extends AppCompatActivity {

    // --- START: MODIFIED VARIABLE DECLARATIONS ---
    private AutoCompleteTextView actvRequestType; // Changed from TextInputEditText
    private TextInputEditText etDescription, etPreferredTime, etEditLocation;
    private AutoCompleteTextView actvEditUrgency;
    private Button btnUpdateHelpRequest;

    private FirebaseFirestore db;
    private PlatformDataAccount platformDataAccount; // Added controller for categories
    private String currentRequestId;
    // --- END: MODIFIED VARIABLE DECLARATIONS ---

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
        platformDataAccount = new PlatformDataAccount(); // Initialize the controller
        initializeUI();
        loadExistingData();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_edit_request);
        topAppBar.setNavigationOnClickListener(v -> finish());

        // --- START: MODIFIED INITIALIZATION ---
        // Use the new ID and type from your updated XML
        actvRequestType = findViewById(R.id.actvEditRequestType);
        // --- END: MODIFIED INITIALIZATION ---

        etDescription = findViewById(R.id.etEditDescription);
        etPreferredTime = findViewById(R.id.etEditPreferredTime);
        etEditLocation = findViewById(R.id.etEditLocation);
        actvEditUrgency = findViewById(R.id.actvEditUrgency);
        btnUpdateHelpRequest = findViewById(R.id.btnUpdateHelpRequest);

        // --- Setup the Urgency Dropdown ---
        String[] urgencyLevels = getResources().getStringArray(R.array.urgency_levels);
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        actvEditUrgency.setAdapter(urgencyAdapter);

        // --- NEW: Load categories for the Request Type dropdown ---
        loadCategories();

        btnUpdateHelpRequest.setOnClickListener(v -> updateHelpRequest());
    }


    private void loadCategories() {
        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }

                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        EditHelpRequestActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );
                actvRequestType.setAdapter(categoryAdapter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditHelpRequestActivity.this, "Failed to load request types: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingData() {
        db.collection("help_requests").document(currentRequestId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HelpRequest request = documentSnapshot.toObject(HelpRequest.class);
                        if (request != null) {
                            // --- START: MODIFIED DATA LOADING ---
                            // Set dropdown text. setFilter to false is crucial.
                            actvRequestType.setText(request.getCategory(), false);
                            // --- END: MODIFIED DATA LOADING ---
                            etDescription.setText(request.getDescription());
                            etPreferredTime.setText(request.getPreferredTime());
                            etEditLocation.setText(request.getLocation());
                            actvEditUrgency.setText(request.getUrgencyLevel(), false);
                        }
                    } else {
                        Toast.makeText(this, "Request not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show());
    }

    private void updateHelpRequest() {
        // --- START: MODIFIED VALUE RETRIEVAL ---
        String requestType = actvRequestType.getText().toString().trim();
        // --- END: MODIFIED VALUE RETRIEVAL ---

        String description = etDescription.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();
        String location = etEditLocation.getText().toString().trim();
        String urgencyLevel = actvEditUrgency.getText().toString().trim();

        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty() || urgencyLevel.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("category", requestType);
        updates.put("title", requestType); // It's common to have title and category be the same
        updates.put("description", description);
        updates.put("preferredTime", preferredTime);
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

    // --- NEW METHOD ---
    /**
     * Detaches the Firestore listener when the activity is stopped to prevent memory leaks.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (platformDataAccount != null) {
            platformDataAccount.detachCategoryListener();
        }
    }
}
