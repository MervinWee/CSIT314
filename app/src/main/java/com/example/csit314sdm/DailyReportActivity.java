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
        btnGenerateReport = findViewById(R.id.btnGenerateReportD);
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
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void generateReport() {
        int newUserCount = platformDataAccount.getNewUserCount(selectedDate.getTime());
        int newRequestCount = platformDataAccount.getNewRequestCount(selectedDate.getTime());
        int completedMatchesCount = platformDataAccount.getCompletedMatchesCount(selectedDate.getTime());

        tvNewUsersCount.setText(String.valueOf(newUserCount));
        tvNewRequestsCount.setText(String.valueOf(newRequestCount));
        tvCompletedMatchesCount.setText(String.valueOf(completedMatchesCount));
    }
}
