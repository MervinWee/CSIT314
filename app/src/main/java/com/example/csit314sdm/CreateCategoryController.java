package com.example.csit314sdm;

public class CreateCategoryController {

    private final PlatformDataAccount platformDataAccount;

    // Callback for operations like create
    public interface CategoryOperationCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public CreateCategoryController() {
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
}
