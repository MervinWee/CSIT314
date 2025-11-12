package com.example.csit314sdm.boundary;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csit314sdm.R;

public class CreateUserRoleActivity extends AppCompatActivity {

    private EditText etRoleName, etRoleDescription;
    private Spinner spinnerStatus;
    private Button btnCreateRole;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_role);

        etRoleName = findViewById(R.id.etRoleName);
        etRoleDescription = findViewById(R.id.etRoleDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnCreateRole = findViewById(R.id.btnCreateRole);
        btnBack = findViewById(R.id.btnBack);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.role_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnCreateRole.setOnClickListener(v -> {
            String roleName = etRoleName.getText().toString().trim();
            String roleDescription = etRoleDescription.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            if (roleName.isEmpty() || roleDescription.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Implement the logic to create the user role.
            Toast.makeText(this, "Role '" + roleName + "' created with status '" + status + "'", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
