package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PlatformDashboardActivity extends AppCompatActivity {

    private PlatformDataAccount platformDataAccount;
    private LogoutController logoutController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_dashboard);

        platformDataAccount = new PlatformDataAccount();
        logoutController = new LogoutController(platformDataAccount);

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
            logoutController.logout(new LogoutController.LogoutCallback() {
                @Override
                public void onLogoutComplete() {
                    Intent intent = new Intent(PlatformDashboardActivity.this, loginPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        });
    }
}
