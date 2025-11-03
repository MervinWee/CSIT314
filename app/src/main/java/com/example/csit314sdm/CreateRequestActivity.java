package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter; // <-- IMPORT THIS
import android.widget.AutoCompleteTextView; // <-- IMPORT THIS
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateRequestActivity extends AppCompatActivity {

    // --- UI Components ---
    private TextInputEditText etRequestType, etDescription, etLocation, etPreferredTime, etPhoneNumber, etNotes;
    // FIX: This is now an AutoCompleteTextView for the dropdown
    private AutoCompleteTextView actvUrgency;
    private MaterialToolbar topAppBar;

    private final Calendar preferredDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        // Find all UI components and set up listeners
        initializeUI();
    }

    private void initializeUI() {
        // --- Find all UI Components ---
        topAppBar = findViewById(R.id.topAppBar);
        etRequestType = findViewById(R.id.etRequestType);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etPreferredTime = findViewById(R.id.etPreferredTime);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etNotes = findViewById(R.id.etNotes);
        // FIX: Find the new AutoCompleteTextView
        actvUrgency = findViewById(R.id.actvUrgency);
        Button btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        // --- FIX: Setup the Dropdown Menu ---
        // 1. Get the options from your arrays.xml file
        String[] urgencyLevels = getResources().getStringArray(R.array.urgency_levels);
        // 2. Create an adapter to display the options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        // 3. Set the adapter on the dropdown view
        actvUrgency.setAdapter(adapter);

        // --- Set up Click Listeners ---
        topAppBar.setNavigationOnClickListener(v -> finish());
        btnSubmitRequest.setOnClickListener(v -> submitHelpRequest());
        etPreferredTime.setOnClickListener(v -> showDateTimePicker());
    }

    private void submitHelpRequest() {
        // --- Get User Input ---
        String requestType = etRequestType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        // FIX: Get the selected value from the dropdown
        String urgencyLevel = actvUrgency.getText().toString().trim();

        // Validate that an urgency level was selected
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
}
