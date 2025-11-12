package com.example.csit314sdm;

import java.util.List;

public class SearchFilterHelpRequestController {

    public interface SearchCallback {
        void onSearchSuccess(List<HelpRequestEntity> results);
        void onSearchFailure(String errorMessage);
    }

    public static void searchHelpRequests(String statusFilter, String userRole, SearchCallback callback) {
        // The entity method requires a userId, but for a general search, we pass null.
        HelpRequestEntity.getFilteredHelpRequests(statusFilter, null, userRole, new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) {
                    callback.onSearchSuccess(requests);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) {
                    callback.onSearchFailure(errorMessage);
                }
            }
        });
    }
}
