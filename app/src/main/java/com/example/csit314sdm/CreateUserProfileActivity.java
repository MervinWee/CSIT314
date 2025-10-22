package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateUserProfileActivity extends AppCompatActivity {

    private UserProfileController profileController;
    private AutoCompleteTextView spinnerUserAccount;
    private TextInputEditText etFullName, etContactNumber, etDateOfBirth, etAddress;
    private Button btnSaveProfile;
    private ImageButton btnBack;
    private ProgressDialog progressDialog;

    private List<User> userList; // This will hold the filtered list of users without profiles
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

        progressDialog = new ProgressDialog(this);
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
        progressDialog.setMessage("Loading Users...");
        progressDialog.show();

        profileController.getAllUsersWithProfileCheck(new UserProfileController.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> allUsers) {
                progressDialog.dismiss();
                if (allUsers == null || allUsers.isEmpty()) {
                    Toast.makeText(CreateUserProfileActivity.this, "No users found in the system.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Filter the list on the client side for users needing a profile
                List<User> usersWithoutProfile = new ArrayList<>();
                for (User user : allUsers) {
                    if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                        usersWithoutProfile.add(user);
                    }
                }

                Log.d(TAG, "Found " + usersWithoutProfile.size() + " users needing a profile.");

                if (usersWithoutProfile.isEmpty()) {
                    Toast.makeText(CreateUserProfileActivity.this, "All users already have a profile.", Toast.LENGTH_LONG).show();
                    return;
                }

                // The rest of the code works with the correctly filtered list
                userList = usersWithoutProfile; // Save the filtered list

                List<String> userEmails = new ArrayList<>();
                for (User user : userList) {
                    userEmails.add(user.getEmail());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateUserProfileActivity.this, android.R.layout.simple_dropdown_item_1line, userEmails);
                spinnerUserAccount.setAdapter(adapter);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                progressDialog.dismiss();
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
            Toast.makeText(this, "Please select a user account from the dropdown.", Toast.LENGTH_SHORT).show();
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
                finish();
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

        new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    etDateOfBirth.setText(selectedDate);
                }, year, month, day).show();
    }
}
