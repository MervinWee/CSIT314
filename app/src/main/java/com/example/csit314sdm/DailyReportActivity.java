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

public class DailyReportActivity extends AppCompatActivity implements DailyReportController.View {

    private TextInputEditText etDate;
    private Button btnGenerateReport;
    private Button btnBack;
    private TextView tvNewUsersCount;
    private TextView tvNewRequestsCount;
    private TextView tvCompletedMatchesCount;

    private DailyReportController controller;
    private PlatformDataAccount platformDataAccount;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        platformDataAccount = new PlatformDataAccount();
        controller = new DailyReportController(this, platformDataAccount);

        etDate = findViewById(R.id.etDate);
        etDate.setKeyListener(null);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnBack = findViewById(R.id.btnBack);
        tvNewUsersCount = findViewById(R.id.tvNewUsersCount);
        tvNewRequestsCount = findViewById(R.id.tvNewRequestsCount);
        tvCompletedMatchesCount = findViewById(R.id.tvCompletedMatchesCount);

        etDate.setOnClickListener(v -> showDatePickerDialog());

        btnGenerateReport.setOnClickListener(v -> {
            showToast("Generating report...");
            controller.generateReport(selectedDate.getTime());
        });

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

    @Override
    public void showReportData(int newUserCount, int newRequestCount, int completedMatchesCount) {
        String successMsg = String.format("Report complete: %d users, %d requests, %d matches", newUserCount, newRequestCount, completedMatchesCount);
        showToast(successMsg);

        tvNewUsersCount.setText(String.valueOf(newUserCount));
        tvNewRequestsCount.setText(String.valueOf(newRequestCount));
        tvCompletedMatchesCount.setText(String.valueOf(completedMatchesCount));
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setGenerateButtonEnabled(boolean enabled) {
        btnGenerateReport.setEnabled(enabled);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
