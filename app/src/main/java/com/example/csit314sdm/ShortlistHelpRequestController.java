package com.example.csit314sdm;

import java.util.List;

public class ShortlistHelpRequestController {

    // --- Callbacks ---
    public interface HelpRequestsLoadCallback {
        void onRequestsLoaded(List<HelpRequestEntity> requests);
        void onDataLoadFailed(String errorMessage);
    }

    public interface ShortlistCallback {
        void onShortlistSuccess();
        void onShortlistFailure(String errorMessage);
    }

    // --- Constructor ---
    public ShortlistHelpRequestController() {
    }

    // --- Shortlist Management Methods ---

    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.getSavedHelpRequests(new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) {
                    callback.onRequestsLoaded(requests);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) {
                    callback.onDataLoadFailed(errorMessage);
                }
            }
        });
    }

    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.searchShortlistedRequests(keyword, location, category, new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) {
                    callback.onRequestsLoaded(requests);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) {
                    callback.onDataLoadFailed(errorMessage);
                }
            }
        });
    }

    public void saveRequest(String requestId, final ShortlistCallback callback) {
        HelpRequestEntity.saveRequest(requestId, new HelpRequestEntity.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) {
                    callback.onShortlistSuccess();
                }
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) {
                    callback.onShortlistFailure(errorMessage);
                }
            }
        });
    }

    public void unsaveRequest(String requestId, final ShortlistCallback callback) {
        HelpRequestEntity.unsaveRequest(requestId, new HelpRequestEntity.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) {
                    callback.onShortlistSuccess();
                }
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) {
                    callback.onShortlistFailure(errorMessage);
                }
            }
        });
    }
}