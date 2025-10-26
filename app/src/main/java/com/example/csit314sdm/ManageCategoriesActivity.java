package com.example.csit314sdm;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ManageCategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Button btnViewCategories = findViewById(R.id.btnViewCategories);
        Button btnSearchCategories = findViewById(R.id.btnSearchCategories);
        Button btnCreateCategory = findViewById(R.id.btnCreateCategory);
        Button btnEditCategory = findViewById(R.id.btnEditCategory);
        Button btnDeleteCategory = findViewById(R.id.btnDeleteCategory);
        Button btnBack = findViewById(R.id.btnBack);

        btnViewCategories.setOnClickListener(v -> {
            // TODO: Implement navigation to ViewCategoriesActivity
            Toast.makeText(this, "View Categories clicked", Toast.LENGTH_SHORT).show();
        });

        btnSearchCategories.setOnClickListener(v -> {
            // TODO: Implement navigation to SearchCategoriesActivity
            Toast.makeText(this, "Search Categories clicked", Toast.LENGTH_SHORT).show();
        });

        btnCreateCategory.setOnClickListener(v -> {
            // TODO: Implement navigation to CreateCategoryActivity
            Toast.makeText(this, "Create Category clicked", Toast.LENGTH_SHORT).show();
        });

        btnEditCategory.setOnClickListener(v -> {
            // TODO: Implement navigation to EditCategoryActivity
            Toast.makeText(this, "Edit Category clicked", Toast.LENGTH_SHORT).show();
        });

        btnDeleteCategory.setOnClickListener(v -> {
            // TODO: Implement navigation to DeleteCategoryActivity
            Toast.makeText(this, "Delete Category clicked", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
