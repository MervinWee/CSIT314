package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditCsrProfileActivity extends AppCompatActivity {

    private UserManagementController userManagementController; // Corrected controller
    private TextInputEditText etFullName, etContactNumber, etDateOfBirth, etAddress;
    private Button btnSaveChanges;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_csr_profile);

        userManagementController = new UserManagementController(); // Corrected instantiation

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditCsrProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFullName = findViewById(R.id.etEditCsrFullName);
        etContactNumber = findViewById(R.id.etEditCsrContactNumber);
        etDateOfBirth = findViewById(R.id.etEditCsrDateOfBirth);
        etAddress = findViewById(R.id.etEditCsrAddress);
        btnSaveChanges = findViewById(R.id.btnSaveCsrProfile);
        progressBar = findViewById(R.id.progressBarEditCsr);

        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog());
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        loadCurrentUserProfile();
    }

    private void loadCurrentUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            // Corrected to use UserManagementController and its UserCallback
            userManagementController.fetchUserById(currentUser.getUid(), new UserManagementController.UserCallback<User>() {
                @Override
                public void onSuccess(User user) { // Corrected method name
                    etFullName.setText(user.getFullName());
                    etContactNumber.setText(user.getPhoneNumber());
                    etDateOfBirth.setText(user.getDob());
                    etAddress.setText(user.getAddress());
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Exception e) { // Corrected method name and parameter
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCsrProfileActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveChanges() {
        String fullName = etFullName.getText().toString().trim();
        String contact = etContactNumber.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty() || contact.isEmpty() || dob.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Create a map for the updated data, as required by the controller
            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", fullName);
            updates.put("phoneNumber", contact);
            updates.put("dob", dob);
            updates.put("address", address);

            // Corrected to use UserManagementController and its UserCallback
            userManagementController.updateUserProfile(currentUser.getUid(), updates, new UserManagementController.UserCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) { // Corrected method name
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCsrProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) { // Corrected method name and parameter
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCsrProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etDateOfBirth.setText(selectedDate);
                }, year, month, day).show();
    }
}
