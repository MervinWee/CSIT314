package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PlatformDashboardActivity extends AppCompatActivity {

    // FIX: Add a reference to the controller
    private PlatformDataAccount platformDataAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_dashboard);

        // FIX: Initialize the controller
        platformDataAccount = new PlatformDataAccount();

        Button btnManageCategories = findViewById(R.id.btnManageCategories);
        Button btnPlatformLogout = findViewById(R.id.btnPlatformLogout);
        Button btnDailyReport = findViewById(R.id.btnDailyReportGenerate);
        Button btnWeeklyReport = findViewById(R.id.btnWeeklyReportGenerate);
        Button btnMonthlyReport = findViewById(R.id.btnMonthlyReportGenerate);

        btnDailyReport.setOnClickListener(v -> {
            Intent intent = new Intent(PlatformDashboardActivity.this, DailyReportActivity.class);
            startActivity(intent);
        });

        btnWeeklyReport.setOnClickListener(v -> {
            Intent intent = new Intent(PlatformDashboardActivity.this, WeeklyReportActivity.class);
            startActivity(intent);
        });

        btnMonthlyReport.setOnClickListener(v -> {
            Intent intent = new Intent(PlatformDashboardActivity.this, MonthlyReportActivity.class);
            startActivity(intent);
        });

        btnManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(PlatformDashboardActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        });

        btnPlatformLogout.setOnClickListener(v -> {
            // FIX: Call the cleanup method BEFORE signing out
            if (platformDataAccount != null) {
                platformDataAccount.cleanupAllListeners();
            }
            
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(PlatformDashboardActivity.this, loginPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
