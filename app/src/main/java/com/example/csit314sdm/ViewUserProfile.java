package com.example.csit314sdm;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewUserProfile extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private EditText userRoleEditText;
    private EditText tagsSearch;
    private EditText aboutMeSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_user_profile);

        btnBack = findViewById(R.id.btn_back);
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        userRoleEditText = findViewById(R.id.user_role_edittext);
        tagsSearch = findViewById(R.id.tags_search);
        aboutMeSearch = findViewById(R.id.about_me_search);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Goes back to the previous activity
            }
        });
    }
}
