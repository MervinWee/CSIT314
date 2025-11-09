package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PlatformDashboardActivity extends AppCompatActivity {

    private PlatformDataAccount platformDataAccount;
    private LogoutController logoutController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_dashboard);

        platformDataAccount = new PlatformDataAccount();
        // ** THE FIX IS HERE **
        // The constructor for LogoutController now takes no arguments.
        logoutController = new LogoutController();

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

        // ** AND THE FIX IS HERE **
        btnPlatformLogout.setOnClickListener(v -> {
            // 1. Call the simple logout method.
            logoutController.logoutUser();

            // 2. Handle UI changes directly.
            Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PlatformDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
