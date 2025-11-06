package com.example.csit314sdm;

import java.util.List;

public class CategoryController {

    private final PlatformDataAccount platformDataAccount;

    // Callback for operations like create, update, delete
    public interface CategoryOperationCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // Callback for fetching the list of categories
    public interface CategoryFetchCallback {
        void onCategoriesFetched(List<Category> categories);
        void onFailure(String errorMessage);
    }

    public CategoryController() {
        this.platformDataAccount = new PlatformDataAccount();
    }

    // CREATE
    public void createCategory(String name, String description, final CategoryOperationCallback callback) {
        platformDataAccount.createCategory(name, description, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                if (callback != null) callback.onSuccess(message);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onFailure(message);
            }
        });
    }

    // READ (Listen for real-time updates)
    public void getAllCategories(final CategoryFetchCallback callback) {
        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                if (callback != null) callback.onCategoriesFetched(categories);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onFailure(message);
            }
        });
    }

    // UPDATE
    public void updateCategory(Category categoryToUpdate, String newName, String newDescription, final CategoryOperationCallback callback) {
        platformDataAccount.updateCategory(categoryToUpdate, newName, newDescription, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                if (callback != null) callback.onSuccess(message);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onFailure(message);
            }
        });
    }

    // DELETE
    public void deleteCategory(Category categoryToDelete, final CategoryOperationCallback callback) {
        platformDataAccount.deleteCategory(categoryToDelete, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                if (callback != null) callback.onSuccess(message);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onFailure(message);
            }
        });
    }

    // Cleanup listener
    public void cleanup() {
        platformDataAccount.detachCategoryListener();
    }
}
