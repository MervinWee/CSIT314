package com.example.csit314sdm;

public class EditCategoryController {

    private final PlatformDataAccount platformDataAccount;

    // Callback for operations like update
    public interface CategoryOperationCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public EditCategoryController() {
        this.platformDataAccount = new PlatformDataAccount();
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
}
