package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText registerEmail, registerPassword;
    private Spinner registerUserTypeSpinner;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerUserTypeSpinner = findViewById(R.id.registerUserTypeSpinner);
        btnRegister = findViewById(R.id.btnRegister);

        // Setup Spinner for user types
        String[] userTypes = {"PIN", "CSR_Representative"}; // Define your user types
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registerUserTypeSpinner.setAdapter(adapter);

        registerUserTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserType = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Selected user type: " + selectedUserType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserType = userTypes[0]; // Default to PIN
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            registerEmail.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            registerPassword.setError("Password is required.");
            return;
        }
        if (password.length() < 6) {
            registerPassword.setError("Password must be at least 6 characters.");
            return;
        }
        if (selectedUserType == null) {
            Toast.makeText(RegisterActivity.this, "Please select a user type.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a progress indicator here if desired

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress indicator

                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Save additional user data (like user type) to Firestore
                                saveUserToFirestore(user.getUid(), email, selectedUserType);
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String userType) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("userType", userType);
        user.put("createdAt", System.currentTimeMillis()); // Optional: add creation timestamp

        db.collection("users").document(uid)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data saved to Firestore successfully!");
                            Toast.makeText(RegisterActivity.this, "Account created and data saved.", Toast.LENGTH_SHORT).show();
                            // Redirect to appropriate dashboard based on user type
                            navigateToDashboard(userType);
                        } else {
                            Log.w(TAG, "Error adding user data to Firestore", task.getException());
                            Toast.makeText(RegisterActivity.this, "Account created, but data saving failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            // Even if Firestore fails, user is created in Auth, so still navigate or handle gracefully
                            navigateToDashboard(userType);
                        }
                    }
                });
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        if ("PIN".equals(userType)) {
            intent = new Intent(RegisterActivity.this, PINHomeScreenActivity.class); // Use your new class name
        } else if ("CSR_Representative".equals(userType)) {
            intent = new Intent(RegisterActivity.this, CSRHomeScreenActivity.class); // Use your new class name
        } else {
            // Default or error case: go back to the login page
            intent = new Intent(RegisterActivity.this, loginPage.class); // Use your new class name
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Prevent going back to registration
    }
}


