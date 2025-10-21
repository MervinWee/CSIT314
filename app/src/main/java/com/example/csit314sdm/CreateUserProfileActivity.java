package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log; // <-- IMPORT THE LOG CLASS
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.List;

// BOUNDARY: Manages the Create User Profile UI
public class CreateUserProfileActivity extends AppCompatActivity {

    private UserProfileController profileController;
    private AutoCompleteTextView spinnerUserAccount;
    private TextInputEditText etFullName, etContactNumber, etDateOfBirth, etAddress;
    private Button btnSaveProfile;
    private ImageButton btnBack;
    private ProgressDialog progressDialog;

    private List<User> userList;
    private User selectedUser;

    // Define a tag for logging
    private static final String TAG = "CreateUserProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_profile);

        profileController = new UserProfileController();
        initializeUI();
        loadUsersWithoutProfiles();
    }

    private void initializeUI() {
        spinnerUserAccount = findViewById(R.id.spinnerUserAccount);
        etFullName = findViewById(R.id.etFullName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etAddress = findViewById(R.id.etAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnBack);

        progressDialog = new ProgressDialog(this);
        btnBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());

        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog());

        spinnerUserAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = (User) parent.getItemAtPosition(position);
                Log.d(TAG, "User selected: " + selectedUser.toString());
            }
        });
    }

    private void loadUsersWithoutProfiles() {
        progressDialog.setMessage("Loading Users...");
        progressDialog.show();
        profileController.getUsersWithoutProfiles(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                progressDialog.dismiss();

                // --- THIS IS THE NEW DEBUGGING BLOCK ---
                if (users == null) {
                    Log.d(TAG, "onUsersLoaded: The user list from Firestore is null.");
                    return;
                }

                Log.d(TAG, "onUsersLoaded: " + users.size() + " users were loaded from Firestore.");

                if (users.isEmpty()) {
                    Log.d(TAG, "The list is empty. Check Firestore query, data, and security rules.");
                    Toast.makeText(CreateUserProfileActivity.this, "No users found needing a profile.", Toast.LENGTH_LONG).show();
                } else {
                    for (User user : users) {
                        // Log the details of each user found. Relies on the toString() method in User.java
                        Log.d(TAG, "Found user: " + user.toString());
                    }
                }
                // --- END OF DEBUGGING BLOCK ---

                // This is the original code to set up the adapter
                userList = users;
                ArrayAdapter<User> adapter = new ArrayAdapter<>(CreateUserProfileActivity.this, android.R.layout.simple_spinner_item, userList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                AutoCompleteTextView autoCompleteTextView = findViewById(R.id.spinnerUserAccount);
                autoCompleteTextView.setAdapter(adapter);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressDialog.dismiss();
                // Add logging to the failure case
                Log.e(TAG, "onDataLoadFailed: " + errorMessage);
                Toast.makeText(CreateUserProfileActivity.this, "Error loading users: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSaveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String contact = etContactNumber.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (selectedUser == null) {
            Toast.makeText(this, "Please select a user account.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fullName.isEmpty() || contact.isEmpty() || dob.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all profile fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Saving Profile...");
        progressDialog.show();

        profileController.saveUserProfile(selectedUser, fullName, contact, dob, address, new UserProfileController.ProfileCallback() {
            @Override
            public void onProfileSaveSuccess() {
                progressDialog.dismiss();
                Toast.makeText(CreateUserProfileActivity.this, "Profile created successfully!", Toast.LENGTH_LONG).show();
                finish(); // Go back to the dashboard after success
            }

            @Override
            public void onProfileSaveFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(CreateUserProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    etDateOfBirth.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
}
