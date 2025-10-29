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

public class MonthlyReportActivity extends AppCompatActivity {

    private TextInputEditText etMonth;
    private Button btnGenerateReport;
    private Button btnBack;
    private TextView tvTopCompany;
    private TextView tvMostRequestedService;

    private PlatformDataAccount platformDataAccount;
    private Calendar selectedMonth = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        platformDataAccount = new PlatformDataAccount();

        etMonth = findViewById(R.id.etMonth);
        btnGenerateReport = findViewById(R.id.btnGenerateReportM);
        btnBack = findViewById(R.id.btnBack);
        tvTopCompany = findViewById(R.id.tvTopCompany);
        tvMostRequestedService = findViewById(R.id.tvMostRequestedService);

        etMonth.setOnClickListener(v -> showMonthPickerDialog());

        btnGenerateReport.setOnClickListener(v -> generateReport());

        btnBack.setOnClickListener(v -> finish());

        updateMonthInView();
    }

    private void showMonthPickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedMonth.set(Calendar.YEAR, year);
            selectedMonth.set(Calendar.MONTH, month);
            updateMonthInView();
        };

        new DatePickerDialog(this, dateSetListener,
                selectedMonth.get(Calendar.YEAR),
                selectedMonth.get(Calendar.MONTH),
                selectedMonth.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateMonthInView() {
        String myFormat = "MM/yyyy"; // Format for month and year
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etMonth.setText(sdf.format(selectedMonth.getTime()));
    }

    private void generateReport() {
        int year = selectedMonth.get(Calendar.YEAR);
        int month = selectedMonth.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

        String topCompany = platformDataAccount.getTopPerformingCompany(year, month);
        String mostRequestedService = platformDataAccount.getMostRequestedService(year, month);

        tvTopCompany.setText(topCompany != null ? topCompany : "N/A");
        tvMostRequestedService.setText(mostRequestedService != null ? mostRequestedService : "N/A");
    }
}
