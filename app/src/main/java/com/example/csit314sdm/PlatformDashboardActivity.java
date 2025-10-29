package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PlatformDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_dashboard);

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
            // For now, this is a placeholder. In the next step, we will create CategoryManagementActivity.
            Toast.makeText(this, "Category Management screen coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnPlatformLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(PlatformDashboardActivity.this, LoginController.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
