package com.example.csit314sdm;

import java.util.List;

public class ShortlistHelpRequestController {

    // --- Callbacks ---
    public interface HelpRequestsLoadCallback {
        void onRequestsLoaded(List<HelpRequest> requests);

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
        HelpRequest.getSavedHelpRequests(new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
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
        HelpRequest.searchShortlistedRequests(keyword, location, category, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
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
        HelpRequest.saveRequest(requestId, new HelpRequestController.SaveCallback() {
            @Override
            public void onSaveSuccess() {
                if (callback != null) {
                    callback.onShortlistSuccess();
                }
            }

            @Override
            public void onSaveFailure(String errorMessage) {
                if (callback != null) {
                    callback.onShortlistFailure(errorMessage);
                }
            }
        });
    }

    public void unsaveRequest(String requestId, final ShortlistCallback callback) {
        HelpRequest.unsaveRequest(requestId, new HelpRequestController.SaveCallback() {
            @Override
            public void onSaveSuccess() {
                if (callback != null) {
                    callback.onShortlistSuccess();
                }
            }

            @Override
            public void onSaveFailure(String errorMessage) {
                if (callback != null) {
                    callback.onShortlistFailure(errorMessage);
                }
            }
        });
    }
}