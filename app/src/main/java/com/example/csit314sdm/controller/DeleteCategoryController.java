package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.Category;
import com.example.csit314sdm.PlatformDataAccount;

public class DeleteCategoryController {

    private final PlatformDataAccount platformDataAccount;

    // Callback for operations like delete
    public interface CategoryOperationCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public DeleteCategoryController() {
        this.platformDataAccount = new PlatformDataAccount();
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
}
