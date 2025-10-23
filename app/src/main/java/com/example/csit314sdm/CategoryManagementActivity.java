package com.example.csit314sdm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
// --- ADD THIS IMPORT ---
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;


public class CategoryManagementActivity extends AppCompatActivity implements CategoryAdapter.OnEditClickListener {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter adapter;
    private CategoryController controller;
    private ProgressBar progressBar;
    private TextView tvNoCategories;
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarCategoryManagement);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBarCategories);
        tvNoCategories = findViewById(R.id.tvNoCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);


        controller = new CategoryController();
        setupRecyclerView();



        fabAddCategory.setOnClickListener(v -> showCreateOrUpdateCategoryDialog(null));


        fetchCategories();
    }

    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryAdapter(this);
        recyclerViewCategories.setAdapter(adapter);
    }


    @Override
    public void onEditClick(Category category) {
        showCreateOrUpdateCategoryDialog(category);
    }


    private void fetchCategories() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewCategories.setVisibility(View.GONE);
        tvNoCategories.setVisibility(View.GONE);

        controller.getAllCategories(new CategoryController.CategoryFetchCallback() {
            @Override
            public void onCategoriesFetched(List<Category> categories) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (categories.isEmpty()) {
                        tvNoCategories.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewCategories.setVisibility(View.VISIBLE);
                        adapter.setCategories(categories);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CategoryManagementActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // The method is now correctly named
    private void showCreateOrUpdateCategoryDialog(final Category categoryToUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_category, null);
        builder.setView(dialogView);

        final EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        final EditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);

        // --- THIS BLOCK HANDLES BOTH CREATE AND EDIT ---
        if (categoryToUpdate != null) {
            // This is an EDIT operation, so pre-populate the form
            builder.setTitle("Edit Category");
            etCategoryName.setText(categoryToUpdate.getName());
            etCategoryDescription.setText(categoryToUpdate.getDescription());
            builder.setPositiveButton("Update", null);
        } else {
            // This is a CREATE operation
            builder.setTitle("Create New Category");
            builder.setPositiveButton("Create", null);
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        // --------------------------------------------------

        AlertDialog dialog = builder.create();

        // Use onShowListener to override the default closing behavior
        dialog.setOnShowListener(d -> {
            // =========== FIX 3: Use the correct android.widget.Button class ===========
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etCategoryName.getText().toString().trim();
                String description = etCategoryDescription.getText().toString().trim();

                if (name.isEmpty()) {
                    etCategoryName.setError("Category name cannot be empty.");
                    // Don't close the dialog if validation fails
                    return;
                }

                if (categoryToUpdate != null) {
                    // --- UPDATE LOGIC ---
                    categoryToUpdate.setName(name);
                    categoryToUpdate.setDescription(description);
                    controller.updateCategory(categoryToUpdate, createOperationCallback());
                } else {
                    // --- CREATE LOGIC ---
                    // You need to have a constructor in your Category class
                    Category newCategory = new Category(name, description);
                    controller.createCategory(newCategory, createOperationCallback());
                }
                dialog.dismiss(); // Close the dialog ONLY after successful validation
            });
        });

        dialog.show();
    }

    // --- CREATE A REUSABLE CALLBACK ---
    private CategoryController.CategoryOperationCallback createOperationCallback() {
        return new CategoryController.CategoryOperationCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(CategoryManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                fetchCategories(); // Refresh the list on success
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CategoryManagementActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        };
    }
}
