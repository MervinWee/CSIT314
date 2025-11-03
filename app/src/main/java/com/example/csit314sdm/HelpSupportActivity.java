package com.example.csit314sdm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class HelpSupportActivity extends AppCompatActivity {

    private EditText etHelpSubject, etHelpMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        initializeUI();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_help_support);
        topAppBar.setNavigationOnClickListener(v -> finish());

        etHelpSubject = findViewById(R.id.etHelpSubject);
        etHelpMessage = findViewById(R.id.etHelpMessage);

        Button btnSubmitHelpRequest = findViewById(R.id.btnSubmitHelpRequest);
        btnSubmitHelpRequest.setOnClickListener(v -> submitHelpRequest());
    }

    private void submitHelpRequest() {
        String subject = etHelpSubject.getText().toString().trim();
        String message = etHelpMessage.getText().toString().trim();

        if (subject.isEmpty()) {
            etHelpSubject.setError("Subject cannot be empty.");
            etHelpSubject.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            etHelpMessage.setError("Message cannot be empty.");
            etHelpMessage.requestFocus();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@example.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
