package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

import java.util.List;

public class SearchFilterHelpRequestController {

    public interface SearchCallback {
        void onSearchSuccess(List<HelpRequest> results);
        void onSearchFailure(String errorMessage);
    }

    public static void searchHelpRequests(String statusFilter, String userRole, SearchCallback callback) {
        // The entity method requires a userId, but for a general search, we pass null.
        HelpRequest.getFilteredHelpRequests(statusFilter, null, userRole, new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
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
