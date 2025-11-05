package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateUserProfileActivity extends AppCompatActivity {

    private UserProfileController profileController;
    private AutoCompleteTextView spinnerUserAccount;
    private TextInputEditText etFullName, etContactNumber, etDateOfBirth, etAddress;
    private Button btnSaveProfile;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private List<User> userList = new ArrayList<>();
    private User selectedUser;

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
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());
        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog());


        spinnerUserAccount.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEmail = (String) parent.getItemAtPosition(position);
            for (User user : userList) {
                if (user.getEmail().equals(selectedEmail)) {
                    selectedUser = user;
                    break;
                }
            }
            if (selectedUser != null) {
                Log.d(TAG, "User selected: " + selectedUser.getEmail());
            }
        });
    }

    private void loadUsersWithoutProfiles() {
        progressBar.setVisibility(View.VISIBLE);

        profileController.getAllUsersWithProfileCheck(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> allUsers) {
                progressBar.setVisibility(View.GONE);

                if (allUsers == null || allUsers.isEmpty()) {
                    Toast.makeText(CreateUserProfileActivity.this, "No users found in the system.", Toast.LENGTH_LONG).show();
                    return;
                }


                userList.clear();
                List<String> userEmails = new ArrayList<>();


                for (User user : allUsers) {

                    if (user.getFullName() == null || user.getFullName().trim().isEmpty() ||
                            user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                        userList.add(user);
                        userEmails.add(user.getEmail());
                    }
                }

                Log.d(TAG, "Found " + userList.size() + " users needing a profile.");

                if (userList.isEmpty()) {
                    spinnerUserAccount.setHint("All users already have a profile.");
                    Toast.makeText(CreateUserProfileActivity.this, "All users already have a profile.", Toast.LENGTH_LONG).show();
                    return;
                }

                // The ArrayAdapter, when used with an AutoCompleteTextView, provides the search/filter functionality automatically.
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateUserProfileActivity.this, android.R.layout.simple_dropdown_item_1line, userEmails);
                spinnerUserAccount.setAdapter(adapter);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onDataLoadFailed: " + errorMessage);
                Toast.makeText(CreateUserProfileActivity.this, "Error loading users: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSaveProfile() {
        // Before saving, ensure a user has actually been selected from the list,
        String currentText = spinnerUserAccount.getText().toString();
        boolean userIsValid = false;
        if (selectedUser != null && selectedUser.getEmail().equals(currentText)) {
            userIsValid = true;
        }

        if (!userIsValid) {
            Toast.makeText(this, "Please select a valid user from the dropdown list.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String contact = etContactNumber.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty() || contact.isEmpty() || dob.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all profile fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        profileController.saveUserProfile(selectedUser, fullName, contact, dob, address, new UserProfileController.ProfileCallback() {
            @Override
            public void onProfileSaveSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateUserProfileActivity.this, "Profile created successfully!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onProfileSaveFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateUserProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format the date consistently using Locale for safety
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etDateOfBirth.setText(selectedDate);
                }, year, month, day).show();
    }
}
