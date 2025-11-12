package com.example.csit314sdm.boundary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.csit314sdm.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class PINSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_settings);

        ImageButton btnBack = findViewById(R.id.btn_back);
        SwitchMaterial switchNotifications = findViewById(R.id.switch_notifications);
        SwitchMaterial switchDarkMode = findViewById(R.id.switch_dark_mode);
        Button btnLogout = findViewById(R.id.btn_logout);


        btnBack.setOnClickListener(v -> onBackPressed());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {

            Toast.makeText(this, "Notifications toggled", Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        btnLogout.setOnClickListener(v -> {
            // Handle logout
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(PINSettingsActivity.this, loginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
