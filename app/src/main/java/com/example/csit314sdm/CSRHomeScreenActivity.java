package com.example.csit314sdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class CSRHomeScreenActivity extends AppCompatActivity {


    Button btnLogout; // Changed variable name to match ID for consistency
    FirebaseAuth mAuth; // Declare a FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_csrhome_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.btnLogout); // Corrected variable name


        // Set the OnClickListener with an action
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Sign the user out
                mAuth.signOut();

                // 2. Create an Intent to go back to the login page
                Intent intent = new Intent(CSRHomeScreenActivity.this, loginPage.class);

                // 3. Add flags to clear the back stack
                // This prevents the user from pressing the back button to return to the home screen after logging out
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // 4. Start the new activity
                startActivity(intent);

                // 5. Finish the current activity
                finish();
            }
        });
    }
}