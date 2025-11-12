package com.example.csit314sdm.boundary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.entity.Category;
import com.example.csit314sdm.CategoryAdapter;
import com.example.csit314sdm.controller.CreateCategoryController;
import com.example.csit314sdm.controller.DeleteCategoryController;
import com.example.csit314sdm.controller.EditCategoryController;
import com.example.csit314sdm.R;
import com.example.csit314sdm.controller.ViewCategoriesController;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private Button btnCreateCategory, btnEditCategory, btnDeleteCategory, btnBack;
    private EditText etSearchCategory; // Added for search
    private CategoryAdapter categoryAdapter;

    private CreateCategoryController createCategoryController;
    private ViewCategoriesController viewCategoriesController;
    private EditCategoryController editCategoryController;
    private DeleteCategoryController deleteCategoryController;

    private Category selectedCategory = null;
    private List<Category> allCategories = new ArrayList<>(); // Added to hold the master list

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        createCategoryController = new CreateCategoryController();
        viewCategoriesController = new ViewCategoriesController();
        editCategoryController = new EditCategoryController();
        deleteCategoryController = new DeleteCategoryController();

        // Bind views
        rvCategories = findViewById(R.id.rvCategories);
        btnCreateCategory = findViewById(R.id.btnCreateCategory);
        btnEditCategory = findViewById(R.id.btnEditCategory);
        btnDeleteCategory = findViewById(R.id.btnDeleteCategory);
        btnBack = findViewById(R.id.btnBack);
        etSearchCategory = findViewById(R.id.etSearchCategory); // Assuming this ID exists in your XML

        setupRecyclerView();

        viewCategoriesController.getAllCategories(new ViewCategoriesController.CategoryFetchCallback() {
            @Override
            public void onCategoriesFetched(List<Category> categories) {
                allCategories.clear();
                allCategories.addAll(categories);
                categoryAdapter.updateCategories(new ArrayList<>(allCategories)); // Display all initially
                resetSelection();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ManageCategoriesActivity.this, "Error loading categories: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // Add TextWatcher for search functionality
        etSearchCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
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

    private void filter(String text) {
        List<Category> filteredList = new ArrayList<>();
        for (Category item : allCategories) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        categoryAdapter.updateCategories(filteredList);
        resetSelection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewCategoriesController != null) {
            viewCategoriesController.cleanup();
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

            if (isEditing) {
                editCategoryController.updateCategory(existingCategory, name, description, new EditCategoryController.CategoryOperationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(ManageCategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ManageCategoriesActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                createCategoryController.createCategory(name, description, new CreateCategoryController.CategoryOperationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(ManageCategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ManageCategoriesActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
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
                    deleteCategoryController.deleteCategory(categoryToDelete, new DeleteCategoryController.CategoryOperationCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(ManageCategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ManageCategoriesActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
