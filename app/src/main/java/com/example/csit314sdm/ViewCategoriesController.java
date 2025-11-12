package com.example.csit314sdm;

import java.util.List;

public class ViewCategoriesController {

    private final PlatformDataAccount platformDataAccount;

    // Callback for fetching the list of categories
    public interface CategoryFetchCallback {
        void onCategoriesFetched(List<Category> categories);
        void onFailure(String errorMessage);
    }

    public ViewCategoriesController() {
        this.platformDataAccount = new PlatformDataAccount();
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

    // Cleanup listener
    public void cleanup() {
        platformDataAccount.detachCategoryListener();
    }
}
