package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

import java.util.Date;
import java.util.List;

public class SearchHistoryOfCompletedMatchesHelpRequestController {

    public interface SearchCallback {
        void onSearchSuccess(List<HelpRequest> results);

        void onSearchFailure(String errorMessage);
    }

    public static void searchCompletedMatches(String companyId, String category, Date fromDate, Date toDate, SearchCallback callback) {
        // The call was missing the companyId.
        HelpRequest.getCompletedHistory(companyId, category, fromDate, toDate, category, new HelpRequest.ListCallback() {
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