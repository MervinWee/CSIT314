package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

import java.util.Date;
import java.util.List;

public class ViewCompletedMatchHelpRequestController {

    public interface ViewCallback {
        void onViewSuccess(List<HelpRequest> completedRequests);
        void onViewFailure(String errorMessage);
    }

    // You need to add the missing userId parameter here
    public static void viewCompletedMatches(String companyId, String userId, Date fromDate, Date toDate, ViewCallback callback) {
        // The 4th argument for 'category' was missing. We pass null to search all categories.
        // We now pass the new 'userId' as the second argument.
        HelpRequest.getCompletedHistory(companyId, userId, fromDate, toDate, null, new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) {
                    callback.onViewSuccess(requests);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) {
                    callback.onViewFailure(errorMessage);
                }
            }
        });
    }
}
