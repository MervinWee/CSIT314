package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateRequestActivity extends AppCompatActivity {

    // --- UI Components (with modifications) ---
    private AutoCompleteTextView actvRequestType; // Changed from TextInputEditText
    private TextInputEditText etDescription, etLocation, etPreferredTime, etPhoneNumber, etNotes;
    private AutoCompleteTextView actvUrgency;
    private MaterialToolbar topAppBar;

    private PlatformDataAccount platformDataAccount; // Controller to get categories
    private final Calendar preferredDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        // Initialize controller
        platformDataAccount = new PlatformDataAccount();

        // Find all UI components and set up listeners
        initializeUI();
    }

    private void initializeUI() {
        // --- Find all UI Components ---
        topAppBar = findViewById(R.id.topAppBar);
        actvRequestType = findViewById(R.id.actvRequestType); // Find the new dropdown
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etPreferredTime = findViewById(R.id.etPreferredTime);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etNotes = findViewById(R.id.etNotes);
        actvUrgency = findViewById(R.id.actvUrgency);
        Button btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        // --- Setup the Urgency Dropdown ---
        String[] urgencyLevels = getResources().getStringArray(R.array.urgency_levels);
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        actvUrgency.setAdapter(urgencyAdapter);

        // --- NEW: Load categories for the Request Type dropdown ---
        loadCategories();

        // --- Set up Click Listeners ---
        topAppBar.setNavigationOnClickListener(v -> finish());
        btnSubmitRequest.setOnClickListener(v -> submitHelpRequest());
        etPreferredTime.setOnClickListener(v -> showDateTimePicker());
    }

    /**
     * NEW: Fetches categories from Firestore and populates the Request Type dropdown.
     */
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
        // --- Get User Input (with modifications) ---
        String requestType = actvRequestType.getText().toString().trim(); // Get text from the dropdown
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String urgencyLevel = actvUrgency.getText().toString().trim();

        // Validate that a request type and urgency level were selected
        if (requestType.isEmpty()) {
            Toast.makeText(this, "Please select a request type.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (urgencyLevel.isEmpty()) {
            Toast.makeText(this, "Please select an urgency level.", Toast.LENGTH_SHORT).show();
            return;
        }

        String pinId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- Instantiate Controller and Pass Data ---
        CreateRequestController createController = new CreateRequestController();
        Task<DocumentReference> creationTask = createController.createNewRequest(
                requestType, description, location, preferredTime, phoneNumber, notes, urgencyLevel, pinId);

        // --- Handle the Result ---
        if (creationTask == null) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        creationTask.addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Help request submitted successfully!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show();
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

    /**
     * NEW: Detaches the Firestore listener when the activity is stopped to prevent memory leaks.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (platformDataAccount != null) {
            platformDataAccount.detachCategoryListener();
        }
    }
}
