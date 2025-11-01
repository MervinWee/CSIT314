package com.example.csit314sdm;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditPinProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etDob, etAddress, etPhoneNumber;
    private Button btnSaveChanges;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private User currentUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pin_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
        loadCurrentUser();
    }

    private void initializeUI() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar_edit_profile);
        topAppBar.setNavigationOnClickListener(v -> finish());

        etFullName = findViewById(R.id.etEditFullName);
        etDob = findViewById(R.id.etEditDob);
        etAddress = findViewById(R.id.etEditAddress);
        etPhoneNumber = findViewById(R.id.etEditPhoneNumber);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Error: No user logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userDocRef = db.collection("users").document(firebaseUser.getUid());
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUserData = documentSnapshot.toObject(User.class);
                if (currentUserData != null) {
                    populateFields();
                }
            }
        });
    }

    private void populateFields() {
        etFullName.setText(currentUserData.getFullName());
        etDob.setText(currentUserData.getDob());
        etAddress.setText(currentUserData.getAddress());
        etPhoneNumber.setText(currentUserData.getPhoneNumber());
    }

    private void saveChanges() {
        String fullName = etFullName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full name cannot be empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("dob", dob);
        updates.put("address", address);
        updates.put("phoneNumber", phoneNumber);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show());
    }
}
