package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingPageForPin extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_page_for_pin);

        // --- Toolbar Buttons --- //
        ImageButton backButton = findViewById(R.id.back_button);

        // --- Account Section --- //
        TextView personalInfo = findViewById(R.id.personal_information);
        TextView changePassword = findViewById(R.id.change_password);
        TextView notificationPrefs = findViewById(R.id.notification_preferences);
        TextView privacySettings = findViewById(R.id.privacy_settings);
        RelativeLayout language = findViewById(R.id.language_layout);

        // --- Help Request Preferences Section --- //
        TextView defaultServiceTypes = findViewById(R.id.default_service_types);
        TextView preferredContactMethods = findViewById(R.id.preferred_contact_methods);
        TextView availabilitySettings = findViewById(R.id.availability_settings);
        TextView emergencyContacts = findViewById(R.id.emergency_contacts);

        // --- Sign Out Button --- //
        Button signOutButton = findViewById(R.id.sign_out_button);

        // --- Set Click Listeners --- //
        backButton.setOnClickListener(this);
        personalInfo.setOnClickListener(this);
        changePassword.setOnClickListener(this);
        notificationPrefs.setOnClickListener(this);
        privacySettings.setOnClickListener(this);
        language.setOnClickListener(this);
        defaultServiceTypes.setOnClickListener(this);
        preferredContactMethods.setOnClickListener(this);
        availabilitySettings.setOnClickListener(this);
        emergencyContacts.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.back_button) {
            finish(); // Go back to the previous activity
        } else if (id == R.id.personal_information) {
            showToast("Personal Information clicked");
        } else if (id == R.id.change_password) {
            showToast("Change Password clicked");
        } else if (id == R.id.notification_preferences) {
            showToast("Notification Preferences clicked");
        } else if (id == R.id.privacy_settings) {
            showToast("Privacy Settings clicked");
        } else if (id == R.id.language_layout) {
            showToast("Language clicked");
        } else if (id == R.id.default_service_types) {
            showToast("Default Service Types clicked");
        } else if (id == R.id.preferred_contact_methods) {
            showToast("Preferred Contact Methods clicked");
        } else if (id == R.id.availability_settings) {
            showToast("Availability Settings clicked");
        } else if (id == R.id.emergency_contacts) {
            showToast("Emergency Contacts clicked");
        } else if (id == R.id.sign_out_button) {
            showToast("Sign Out clicked");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
