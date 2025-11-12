package com.example.csit314sdm.boundary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.csit314sdm.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CategoryManagementActivity extends AppCompatActivity {

    private static final String TAG = "CategoryManagement";

    // UI Elements
    private ListView categoriesListView;
    private Button btnAddCategory;
    private ProgressBar progressBar;

    // Firebase Firestore
    private FirebaseFirestore db;
    private CollectionReference categoriesCollection;

    private ArrayList<String> categoryList;
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure this layout file exists and has the correct UI elements
        setContentView(R.layout.activity_category_management);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        categoriesCollection = db.collection("categories");

        initializeUI();
        loadCategories();
    }

    private void initializeUI() {
        categoriesListView = findViewById(R.id.categoriesListView);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        categoriesListView.setAdapter(categoryAdapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        categoriesCollection.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                categoryList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String categoryName = document.getString("name");
                    if (categoryName != null) {
                        categoryList.add(categoryName);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error loading categories: ", task.getException());
                Toast.makeText(this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        final EditText input = new EditText(this);
        input.setHint("Category Name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                addNewCategory(categoryName);
            } else {
                Toast.makeText(this, "Category name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addNewCategory(String categoryName) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> category = new HashMap<>();
        category.put("name", categoryName);

        categoriesCollection.add(category)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Category added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Category added successfully!", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error adding category", e);
                    Toast.makeText(this, "Failed to add category.", Toast.LENGTH_SHORT).show();
                });
    }
}
