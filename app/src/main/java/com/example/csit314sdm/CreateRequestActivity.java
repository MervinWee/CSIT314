package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateRequestActivity extends AppCompatActivity {

    // --- UI Components ---
    private AutoCompleteTextView actvRequestType;
    private TextInputEditText etDescription, etLocation, etPreferredTime, etNotes;
    private AutoCompleteTextView actvUrgency;
    private AutoCompleteTextView spinnerRegion;
    private MaterialToolbar topAppBar;

    private PlatformDataAccount platformDataAccount;
    private final Calendar preferredDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        platformDataAccount = new PlatformDataAccount();
        initializeUI();
    }

    private void initializeUI() {
        topAppBar = findViewById(R.id.topAppBar);
        actvRequestType = findViewById(R.id.actvRequestType);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etPreferredTime = findViewById(R.id.etPreferredTime);

       
        etNotes = findViewById(R.id.etNotes);

        actvUrgency = findViewById(R.id.actvUrgency);
        spinnerRegion = findViewById(R.id.spinnerRegion);
        Button btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

       
        String[] urgencyLevels = getResources().getStringArray(R.array.urgency_levels);
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        actvUrgency.setAdapter(urgencyAdapter);

       
        String[] regions = {"Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, regions);
        spinnerRegion.setAdapter(regionAdapter);
        
        loadCategories();
        
        topAppBar.setNavigationOnClickListener(v -> finish());
        btnSubmitRequest.setOnClickListener(v -> submitHelpRequest());
        etPreferredTime.setOnClickListener(v -> showDateTimePicker());
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
                        CreateRequestActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );
                actvRequestType.setAdapter(categoryAdapter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CreateRequestActivity.this, "Failed to load request types: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitHelpRequest() {
        // --- Get User Input ---
        String requestType = actvRequestType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String selectedRegion = spinnerRegion.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String urgencyLevel = actvUrgency.getText().toString().trim();

        // --- Validation ---
        if (requestType.isEmpty() || urgencyLevel.isEmpty() || selectedRegion.isEmpty() || description.isEmpty() || location.isEmpty() || preferredTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String pinId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        CreateRequestController createRequestController = new CreateRequestController();

        createRequestController.createHelpRequest(
                requestType, description, location, selectedRegion, preferredTime, notes, urgencyLevel, pinId,
                new CreateRequestController.CreateRequestCallback() {
                    @Override
                    public void onRequestCreated(String documentId) {
                        Toast.makeText(CreateRequestActivity.this, "Help request submitted successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onRequestFailed(String errorMessage) {
                        Toast.makeText(CreateRequestActivity.this, "Submission failed. Please try again: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            preferredDateTime.set(Calendar.YEAR, year);
            preferredDateTime.set(Calendar.MONTH, month);
            preferredDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                preferredDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                preferredDateTime.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                etPreferredTime.setText(sdf.format(preferredDateTime.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (platformDataAccount != null) {
            platformDataAccount.detachCategoryListener();
        }
    }
}
