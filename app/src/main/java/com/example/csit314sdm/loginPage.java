package com.example.csit314sdm;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class loginPage extends AppCompatActivity {


    private static final String TAG = "LoginPage"; // Use TAG for logging

    Button btnLogin;
    EditText etUsername, etPassword; // Renamed txTitle to etUsername for clarity
    TextView tvTitle, tvCreateAccount, tvForgetAccount; // These are already correct

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loginpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find UI components by their IDs
        etUsername = findViewById(R.id.etEmail); // Corresponding to UserName EditText
        etPassword = findViewById(R.id.etPassword); // Corresponding to Password EditText
        btnLogin = findViewById(R.id.btnLogin);
        tvTitle = findViewById(R.id.tvTitle); // Assuming you want to interact with this, though not strictly needed for login logic
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvForgetAccount = findViewById(R.id.tvForgetAccount);

        // --- Check if user is already logged in ---
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, fetch user type and navigate immediately
            Log.d(TAG, "User already logged in: " + currentUser.getEmail());
            fetchUserTypeAndNavigate(currentUser.getUid());
        }

        // --- Set up OnClickListener for Login Button ---
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // --- Set up clickable text for "No Account? Click here" ---
        String createAccountFullText = "No Account? Click here";
        SpannableString createAccountSpannable = new SpannableString(createAccountFullText);
        ClickableSpan createAccountClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(loginPage.this, RegisterActivity.class));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        };
        int createAccountStartIndex = createAccountFullText.indexOf("here");
        int createAccountEndIndex = createAccountStartIndex + "here".length();
        if (createAccountStartIndex != -1) { // Check if "here" exists
            createAccountSpannable.setSpan(createAccountClickableSpan, createAccountStartIndex, createAccountEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            createAccountSpannable.setSpan(new StyleSpan(Typeface.BOLD), createAccountStartIndex, createAccountEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvCreateAccount.setText(createAccountSpannable);
        tvCreateAccount.setMovementMethod(LinkMovementMethod.getInstance()); // Essential for clickable spans

        // --- Set up clickable text for "Forget Password? Click here" ---
        String forgetPasswordFullText = "Forget Password? Click here";
        SpannableString forgetPasswordSpannable = new SpannableString(forgetPasswordFullText);
        ClickableSpan forgetPasswordClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // TODO: Implement Forgot Password Activity
                Toast.makeText(loginPage.this, "Forgot password functionality coming soon!", Toast.LENGTH_SHORT).show();
                // Example: startActivity(new Intent(loginPage.this, ForgotPasswordActivity.class));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        };
        int forgetPasswordStartIndex = forgetPasswordFullText.indexOf("here");
        int forgetPasswordEndIndex = forgetPasswordStartIndex + "here".length();
        if (forgetPasswordStartIndex != -1) { // Check if "here" exists
            forgetPasswordSpannable.setSpan(forgetPasswordClickableSpan, forgetPasswordStartIndex, forgetPasswordEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            forgetPasswordSpannable.setSpan(new StyleSpan(Typeface.BOLD), forgetPasswordStartIndex, forgetPasswordEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvForgetAccount.setText(forgetPasswordSpannable);
        tvForgetAccount.setMovementMethod(LinkMovementMethod.getInstance()); // Essential for clickable spans
    }

    private void loginUser() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etUsername.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required.");
            return;
        }

        // You might want to show a ProgressBar here
        // showProgress(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // hideProgress(false); // Hide progress bar

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                fetchUserTypeAndNavigate(user.getUid());
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(loginPage.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserTypeAndNavigate(String uid) {
        DocumentReference userDocRef = db.collection("users").document(uid);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userType = document.getString("userType");
                        Log.d(TAG, "User type fetched: " + userType);
                        navigateToDashboard(userType);
                    } else {
                        Log.d(TAG, "No user document in Firestore for UID: " + uid);
                        Toast.makeText(loginPage.this, "User data not found. Please re-register or contact support.", Toast.LENGTH_LONG).show();
                        mAuth.signOut(); // Sign out user if data is missing for consistency
                    }
                } else {
                    Log.w(TAG, "Failed to fetch user data: ", task.getException());
                    Toast.makeText(loginPage.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        if ("PIN".equals(userType)) {
            intent = new Intent(loginPage.this, PINHomeScreenActivity.class);
        } else if ("CSR_Representative".equals(userType)) {
            intent = new Intent(loginPage.this, CSRHomeScreenActivity.class);
        } else {
            // Fallback for unknown user types or if userType is null
            Log.w(TAG, "Unknown user type or userType is null: " + userType);
            Toast.makeText(loginPage.this, "Unknown user type. Please contact administrator.", Toast.LENGTH_LONG).show();
            mAuth.signOut(); // Log out unknown user types for security
            intent = new Intent(loginPage.this, loginPage.class); // Go back to login
        }
        // Clear all activities on the stack and start a new task with the dashboard
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish loginPage so user can't go back to it with back button
    }
}