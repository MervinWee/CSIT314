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

public class WeeklyReportActivity extends AppCompatActivity implements WeeklyReportController.View {

    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private Button btnGenerateReport;
    private Button btnBack;
    private TextView tvUniquePinsCount;
    private TextView tvUniqueCsrCompaniesCount;
    private TextView tvTotalCompletedMatchesCount;

    private WeeklyReportController controller;
    private PlatformDataAccount platformDataAccount;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        platformDataAccount = new PlatformDataAccount();
        controller = new WeeklyReportController(this, platformDataAccount);

        etStartDate = findViewById(R.id.etStartDate);
        etStartDate.setKeyListener(null);
        etEndDate = findViewById(R.id.etEndDate);
        etEndDate.setKeyListener(null);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnBack = findViewById(R.id.btnBack);
        tvUniquePinsCount = findViewById(R.id.tvUniquePinsCount);
        tvUniqueCsrCompaniesCount = findViewById(R.id.tvUniqueCsrCompaniesCount);
        tvTotalCompletedMatchesCount = findViewById(R.id.tvTotalCompletedMatchesCount);

        etStartDate.setOnClickListener(v -> showStartDatePickerDialog());
        etEndDate.setOnClickListener(v -> showEndDatePickerDialog());

        btnGenerateReport.setOnClickListener(v -> {
            showToast("Generating weekly report...");
            controller.generateReport(startDate.getTime(), endDate.getTime());
        });

        btnBack.setOnClickListener(v -> finish());

        updateStartDateInView();
        updateEndDateInView();
    }

    private void showStartDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            startDate.set(Calendar.YEAR, year);
            startDate.set(Calendar.MONTH, month);
            startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDateInView();
        };

        new DatePickerDialog(this, dateSetListener,
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showEndDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            endDate.set(Calendar.YEAR, year);
            endDate.set(Calendar.MONTH, month);
            endDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEndDateInView();
        };

        new DatePickerDialog(this, dateSetListener,
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateStartDateInView() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etStartDate.setText(sdf.format(startDate.getTime()));
    }

    private void updateEndDateInView() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etEndDate.setText(sdf.format(endDate.getTime()));
    }

    @Override
    public void showReportData(int uniquePins, int uniqueCsrs, int totalMatches) {
        tvUniquePinsCount.setText(String.valueOf(uniquePins));
        tvUniqueCsrCompaniesCount.setText(String.valueOf(uniqueCsrs));
        tvTotalCompletedMatchesCount.setText(String.valueOf(totalMatches));
        showToast("Weekly report generated successfully.");
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
