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
import java.util.Locale;

public class EditCsrProfileActivity extends AppCompatActivity {

    private UserProfileController userProfileController;
    private TextInputEditText etFullName, etContactNumber, etDateOfBirth, etAddress;
    private Button btnSaveChanges;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_csr_profile);

        userProfileController = new UserProfileController();

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
            userProfileController.getUserById(currentUser.getUid(), new UserProfileController.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    etFullName.setText(user.getFullName());
                    etContactNumber.setText(user.getPhoneNumber());
                    etDateOfBirth.setText(user.getDob());
                    etAddress.setText(user.getAddress());
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onDataLoadFailed(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCsrProfileActivity.this, "Failed to load profile: " + errorMessage, Toast.LENGTH_SHORT).show();
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
            userProfileController.updateUserProfile(currentUser.getUid(), fullName, contact, dob, address, new UserProfileController.ProfileUpdateCallback() {
                @Override
                public void onUpdateSuccess() {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCsrProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onUpdateFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCsrProfileActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_SHORT).show();
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
