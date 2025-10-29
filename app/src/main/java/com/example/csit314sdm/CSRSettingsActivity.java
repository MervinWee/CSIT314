package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class CSRSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csr_settings);

        ImageButton btnBack = findViewById(R.id.btn_back);
        SwitchMaterial switchNotifications = findViewById(R.id.switch_notifications);
        SwitchMaterial switchDarkMode = findViewById(R.id.switch_dark_mode);
        AutoCompleteTextView spinnerLanguage = findViewById(R.id.spinner_language);
        TextView tvEditProfile = findViewById(R.id.tv_edit_profile);
        TextView tvChangePassword = findViewById(R.id.tv_change_password);
        TextView tvPrivacySettings = findViewById(R.id.tv_privacy_settings);
        TextView tvCompanyInformation = findViewById(R.id.tv_company_information);
        AutoCompleteTextView spinnerDefaultLocation = findViewById(R.id.spinner_default_location);
        AutoCompleteTextView spinnerDefaultCategory = findViewById(R.id.spinner_default_category);
        TextView tvContactMethods = findViewById(R.id.tv_contact_methods);
        TextView tvAvailabilitySettings = findViewById(R.id.tv_availability_settings);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Handle back button click
        btnBack.setOnClickListener(v -> onBackPressed());

        // Populate Language spinner
        String[] languages = {"English", "Chinese", "Malay", "Tamil"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, languages);
        spinnerLanguage.setAdapter(languageAdapter);

        // Populate Location spinner
        String[] locations = {"Anywhere", "North", "South", "East", "West", "Central"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        spinnerDefaultLocation.setAdapter(locationAdapter);

        // TODO: Populate Category spinner from a dynamic source (e.g., Firestore)
        String[] categories = {"All", "Donation", "Volunteer"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerDefaultCategory.setAdapter(categoryAdapter);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notifications preference change
            Toast.makeText(this, "Notifications toggled", Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        tvEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to an Edit Profile screen
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        tvChangePassword.setOnClickListener(v -> {
            // TODO: Navigate to a Change Password screen
            Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show();
        });

        tvPrivacySettings.setOnClickListener(v -> {
            // TODO: Navigate to a Privacy Settings screen
            Toast.makeText(this, "Privacy Settings clicked", Toast.LENGTH_SHORT).show();
        });

        tvCompanyInformation.setOnClickListener(v -> {
            // TODO: Navigate to a Company Information screen
            Toast.makeText(this, "Company Information clicked", Toast.LENGTH_SHORT).show();
        });

        tvContactMethods.setOnClickListener(v -> {
            // TODO: Navigate to a Contact Methods screen
            Toast.makeText(this, "Preferred Contact Methods clicked", Toast.LENGTH_SHORT).show();
        });

        tvAvailabilitySettings.setOnClickListener(v -> {
            // TODO: Navigate to an Availability Settings screen
            Toast.makeText(this, "Availability Settings clicked", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            // Handle logout
            Intent intent = new Intent(CSRSettingsActivity.this, LoginController.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
