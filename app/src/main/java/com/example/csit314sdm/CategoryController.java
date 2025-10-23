package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CategoryController {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface CategoryOperationCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public interface CategoryFetchCallback {
        void onCategoriesFetched(List<Category> categories);
        void onFailure(String errorMessage);
    }

    // Method to create a new category
    public void createCategory(Category category, final CategoryOperationCallback callback) {
        // First, check if a category with the same name already exists
        db.collection("categories").whereEqualTo("name", category.getName()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // A category with this name already exists
                        callback.onFailure("A category with this name already exists.");
                    } else {
                        // Name is unique, proceed with creation
                        db.collection("categories").add(category)
                                .addOnSuccessListener(documentReference -> callback.onSuccess("Category created successfully."))
                                .addOnFailureListener(e -> callback.onFailure("Failed to create category: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to check for existing category: " + e.getMessage()));
    }

    // Method to fetch all categories
    public void getAllCategories(final CategoryFetchCallback callback) {
        db.collection("categories").orderBy("name", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categoryList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Category category = doc.toObject(Category.class);
                        category.setId(doc.getId());
                        categoryList.add(category);
                    }
                    callback.onCategoriesFetched(categoryList);
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to fetch categories: " + e.getMessage()));
    }
}
