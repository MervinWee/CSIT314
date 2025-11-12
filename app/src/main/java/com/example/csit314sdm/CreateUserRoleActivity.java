package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateUserRoleActivity extends AppCompatActivity {

    private EditText etRoleName, etRoleDescription;
    private Spinner spinnerStatus;
    private Button btnCreateRole;
    private ImageButton btnBack;
    private CreateUserRoleController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_role);

        etRoleName = findViewById(R.id.etRoleName);
        etRoleDescription = findViewById(R.id.etRoleDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnCreateRole = findViewById(R.id.btnCreateRole);
        btnBack = findViewById(R.id.btnBack);
        controller = new CreateUserRoleController();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.role_status_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerStatus.setAdapter(adapter);

        btnCreateRole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String role = etRoleName.getText().toString();
                String roleDescription = etRoleDescription.getText().toString();
                String status = spinnerStatus.getSelectedItem().toString();

                controller.createUserRole(role, roleDescription, status, new CreateUserRoleController.CreateUserRoleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(CreateUserRoleActivity.this, "Role Created: " + role, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(CreateUserRoleActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
