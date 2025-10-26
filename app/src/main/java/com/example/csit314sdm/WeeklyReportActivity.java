package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyReportActivity extends AppCompatActivity {

    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private Button btnGenerateReport;
    private Button btnBack;
    private TextView tvUniquePinsCount;
    private TextView tvUniqueCsrCompaniesCount;
    private TextView tvTotalCompletedMatchesCount;

    private PlatformDataAccount platformDataAccount;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        platformDataAccount = new PlatformDataAccount();

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReportW);
        btnBack = findViewById(R.id.btnBack);
        tvUniquePinsCount = findViewById(R.id.tvUniquePinsCount);
        tvUniqueCsrCompaniesCount = findViewById(R.id.tvUniqueCsrCompaniesCount);
        tvTotalCompletedMatchesCount = findViewById(R.id.tvTotalCompletedMatchesCount);

        etStartDate.setOnClickListener(v -> showStartDatePickerDialog());
        etEndDate.setOnClickListener(v -> showEndDatePickerDialog());

        btnGenerateReport.setOnClickListener(v -> generateReport());

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
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etStartDate.setText(sdf.format(startDate.getTime()));
    }

    private void updateEndDateInView() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etEndDate.setText(sdf.format(endDate.getTime()));
    }

    private void generateReport() {
        int uniquePinsCount = platformDataAccount.getUniqueActivePINs(startDate.getTime(), endDate.getTime());
        int uniqueCsrCompaniesCount = platformDataAccount.getUniqueActiveCRSs(startDate.getTime(), endDate.getTime());
        int totalCompletedMatches = platformDataAccount.getTotalMatch(startDate.getTime(), endDate.getTime());

        tvUniquePinsCount.setText(String.valueOf(uniquePinsCount));
        tvUniqueCsrCompaniesCount.setText(String.valueOf(uniqueCsrCompaniesCount));
        tvTotalCompletedMatchesCount.setText(String.valueOf(totalCompletedMatches));
    }
}
