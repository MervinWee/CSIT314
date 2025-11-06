package com.example.csit314sdm;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EditHelpRequestActivity extends AppCompatActivity {

    private AutoCompleteTextView actvRequestType;
    private TextInputEditText etDescription, etPreferredTime, etEditLocation;
    private AutoCompleteTextView actvEditUrgency;
    private Button btnUpdateHelpRequest;

    private HelpRequestController helpRequestController;
    private String currentRequestId;
    private HelpRequest currentRequest;

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

        helpRequestController = new HelpRequestController();
        initializeUI();
        loadExistingData();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_edit_request);
        topAppBar.setNavigationOnClickListener(v -> finish());

        actvRequestType = findViewById(R.id.actvEditRequestType);
        etDescription = findViewById(R.id.etEditDescription);
        etPreferredTime = findViewById(R.id.etEditPreferredTime);
        etEditLocation = findViewById(R.id.etEditLocation);
        actvEditUrgency = findViewById(R.id.actvEditUrgency);
        btnUpdateHelpRequest = findViewById(R.id.btnUpdateHelpRequest);

        String[] urgencyLevels = getResources().getStringArray(R.array.urgency_levels);
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        actvEditUrgency.setAdapter(urgencyAdapter);

        loadCategories();

        btnUpdateHelpRequest.setOnClickListener(v -> updateHelpRequest());
    }

    private void loadCategories() {
        helpRequestController.getCategories(new HelpRequestController.CategoryListCallback() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
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
            public void onDataLoadFailed(String errorMessage) {
                Toast.makeText(EditHelpRequestActivity.this, "Failed to load request types: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingData() {
        helpRequestController.getHelpRequestById(currentRequestId, "PIN", new HelpRequestController.SingleRequestLoadCallback() {
            @Override
            public void onRequestLoaded(HelpRequest request) {
                currentRequest = request;
                runOnUiThread(() -> {
                    if (request != null) {
                        actvRequestType.setText(request.getCategory(), false);
                        etDescription.setText(request.getDescription());
                        etPreferredTime.setText(request.getPreferredTime());
                        etEditLocation.setText(request.getLocation());
                        actvEditUrgency.setText(request.getUrgencyLevel(), false);
                    }
                });
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(EditHelpRequestActivity.this, "Failed to load data: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateHelpRequest() {
        if (currentRequest == null) {
            Toast.makeText(this, "Error: Original request data not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestType = actvRequestType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();
        String location = etEditLocation.getText().toString().trim();
        String urgencyLevel = actvEditUrgency.getText().toString().trim();

        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty() || urgencyLevel.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentRequest.setCategory(requestType);
        currentRequest.setTitle(requestType);
        currentRequest.setDescription(description);
        currentRequest.setPreferredTime(preferredTime);
        currentRequest.setLocation(location);
        currentRequest.setUrgencyLevel(urgencyLevel);

        helpRequestController.updateHelpRequest(currentRequest, new HelpRequestController.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EditHelpRequestActivity.this, "Request updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(EditHelpRequestActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
