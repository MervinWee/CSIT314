package com.example.csit314sdm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private Button btnCreateCategory, btnEditCategory, btnDeleteCategory, btnBack;
    private CategoryAdapter categoryAdapter;

    private PlatformDataAccount platformDataAccount; // The CONTROL layer
    private Category selectedCategory = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        platformDataAccount = new PlatformDataAccount();

        // Initialize Views
        rvCategories = findViewById(R.id.rvCategories);
        btnCreateCategory = findViewById(R.id.btnCreateCategory);
        btnEditCategory = findViewById(R.id.btnEditCategory);
        btnDeleteCategory = findViewById(R.id.btnDeleteCategory);
        btnBack = findViewById(R.id.btnBack);

        setupRecyclerView();

        // Use the controller to listen for data changes
        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                categoryAdapter.updateCategories(categories);
                resetSelection();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(ManageCategoriesActivity.this, "Error loading categories: " + message, Toast.LENGTH_LONG).show();
            }
        });

        // Initial button states
        btnEditCategory.setEnabled(false);
        btnDeleteCategory.setEnabled(false);

        // Button Click Listeners
        btnCreateCategory.setOnClickListener(v -> showCreateOrEditDialog(null));
        btnEditCategory.setOnClickListener(v -> {
            if (selectedCategory != null) showCreateOrEditDialog(selectedCategory);
        });
        btnDeleteCategory.setOnClickListener(v -> {
            if (selectedCategory != null) showDeleteConfirmationDialog(selectedCategory);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (platformDataAccount != null) {
            platformDataAccount.detachCategoryListener();
        }
    }

    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this::onCategorySelected);
        rvCategories.setAdapter(categoryAdapter);
    }

    private void onCategorySelected(Category category) {
        selectedCategory = category;
        btnEditCategory.setEnabled(true);
        btnDeleteCategory.setEnabled(true);
    }

    private void resetSelection() {
        selectedCategory = null;
        btnEditCategory.setEnabled(false);
        btnDeleteCategory.setEnabled(false);
    }

    private void showCreateOrEditDialog(@Nullable final Category existingCategory) {
        boolean isEditing = existingCategory != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEditing ? "Edit Category" : "Create New Category");

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Category Name");
        if (isEditing) nameInput.setText(existingCategory.getName());

        final EditText descInput = new EditText(this);
        descInput.setHint("Description");
        if (isEditing) descInput.setText(existingCategory.getDescription());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(nameInput);
        layout.addView(descInput);
        builder.setView(layout);

        builder.setPositiveButton(isEditing ? "Save" : "Create", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            PlatformDataAccount.FirebaseCallback callback = new PlatformDataAccount.FirebaseCallback() {
                @Override public void onSuccess(String message) { Toast.makeText(ManageCategoriesActivity.this, message, Toast.LENGTH_SHORT).show(); } // Corrected
                @Override public void onError(String message) { Toast.makeText(ManageCategoriesActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show(); }
            };

            if (isEditing) {
                platformDataAccount.updateCategory(existingCategory, name, description, callback);
            } else {
                platformDataAccount.createCategory(name, description, callback);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteConfirmationDialog(final Category categoryToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + categoryToDelete.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    platformDataAccount.deleteCategory(categoryToDelete, new PlatformDataAccount.FirebaseCallback() {
                        @Override public void onSuccess(String message) { Toast.makeText(ManageCategoriesActivity.this, message, Toast.LENGTH_SHORT).show(); } // Corrected
                        @Override public void onError(String message) { Toast.makeText(ManageCategoriesActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show(); }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
