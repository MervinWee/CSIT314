package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyReportActivity extends AppCompatActivity {

    private TextInputEditText etDate;
    private Button btnGenerateReport;
    private Button btnBack;
    private TextView tvNewUsersCount;
    private TextView tvNewRequestsCount;
    private TextView tvCompletedMatchesCount;

    private PlatformDataAccount platformDataAccount;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        platformDataAccount = new PlatformDataAccount();

        etDate = findViewById(R.id.etDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnBack = findViewById(R.id.btnBack);
        tvNewUsersCount = findViewById(R.id.tvNewUsersCount);
        tvNewRequestsCount = findViewById(R.id.tvNewRequestsCount);
        tvCompletedMatchesCount = findViewById(R.id.tvCompletedMatchesCount);

        etDate.setOnClickListener(v -> showDatePickerDialog());

        btnGenerateReport.setOnClickListener(v -> generateReport());

        btnBack.setOnClickListener(v -> finish());

        updateDateInView();
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        new DatePickerDialog(this, dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateInView() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void generateReport() {
        btnGenerateReport.setEnabled(false);
        Toast.makeText(this, "Generating report...", Toast.LENGTH_SHORT).show();

        platformDataAccount.generateDailyReport(selectedDate.getTime(), new PlatformDataAccount.DailyReportCallback() {
            @Override
            public void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount) {

                String successMsg = String.format("Report complete: %d users, %d requests, %d matches", newUserCount, newRequestCount, completedMatchesCount);
                Toast.makeText(DailyReportActivity.this, successMsg, Toast.LENGTH_LONG).show();

                tvNewUsersCount.setText(String.valueOf(newUserCount));
                tvNewRequestsCount.setText(String.valueOf(newRequestCount));
                tvCompletedMatchesCount.setText(String.valueOf(completedMatchesCount));
                btnGenerateReport.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DailyReportActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                btnGenerateReport.setEnabled(true);
            }
        });
    }
}
