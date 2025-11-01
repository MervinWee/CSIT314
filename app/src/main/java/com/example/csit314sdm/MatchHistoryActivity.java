package com.example.csit314sdm;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchHistoryActivity extends AppCompatActivity {

    private static final String TAG = "MatchHistoryActivity";

    private RecyclerView recyclerView;
    private PinMyRequestsAdapter adapter;
    private List<HelpRequest> historyList = new ArrayList<>();
    private TextView tvNoHistory;

    private Spinner categorySpinner;
    private EditText etFromDate, etToDate;
    private Button btnApplyFilter;

    private HelpRequestController controller;
    private Date fromDate, toDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy G", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        controller = new HelpRequestController();

        initializeUI();
        setupFilters();

        loadMatchHistory("All", null, null);
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_history);
        topAppBar.setNavigationOnClickListener(v -> finish());

        tvNoHistory = findViewById(R.id.tv_no_history);
        recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PinMyRequestsAdapter(historyList, this, request -> {
            Intent intent = new Intent(this, HelpRequestDetailActivity.class);
            intent.putExtra(HelpRequestDetailActivity.EXTRA_REQUEST_ID, request.getId());
            intent.putExtra("user_role", "PIN");
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        categorySpinner = findViewById(R.id.spinner_history_category);
        etFromDate = findViewById(R.id.et_history_from_date);
        etToDate = findViewById(R.id.et_history_to_date);
        btnApplyFilter = findViewById(R.id.btn_apply_history_filter);
    }

    private void setupFilters() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_filter_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        etToDate.setOnClickListener(v -> showDatePickerDialog(false));

        btnApplyFilter.setOnClickListener(v -> {
            String category = categorySpinner.getSelectedItem().toString();
            loadMatchHistory(category, fromDate, toDate);
        });
    }

    private void showDatePickerDialog(boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if (isFromDate) {
                fromDate = calendar.getTime();
                etFromDate.setText(dateFormat.format(fromDate));
            } else {
                toDate = calendar.getTime();
                etToDate.setText(dateFormat.format(toDate));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadMatchHistory(String category, Date fromDate, Date toDate) {
        tvNoHistory.setVisibility(View.GONE);

        Query query = controller.getMatchHistoryQuery(category, fromDate, toDate);

        if (query == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                historyList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    HelpRequest request = document.toObject(HelpRequest.class);
                    request.setId(document.getId()); // FIX: Manually set the document ID
                    historyList.add(request);
                }
                adapter.notifyDataSetChanged();

                if (historyList.isEmpty()) {
                    tvNoHistory.setText("No history found for the selected filters.");
                    tvNoHistory.setVisibility(View.VISIBLE);
                }

            } else {
                Toast.makeText(this, "Failed to load history. Check logs for index errors.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error loading history: ", task.getException());
            }
        });
    }
}
