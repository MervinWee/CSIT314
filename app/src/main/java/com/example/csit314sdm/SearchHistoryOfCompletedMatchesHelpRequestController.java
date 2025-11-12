package com.example.csit314sdm;

import java.util.Date;
import java.util.List;

public class SearchHistoryOfCompletedMatchesHelpRequestController {

    public interface SearchCallback {
        void onSearchSuccess(List<HelpRequestEntity> results);

        void onSearchFailure(String errorMessage);
    }

    public static void searchCompletedMatches(String companyId, String category, Date fromDate, Date toDate, SearchCallback callback) {
        // The call was missing the companyId.
        HelpRequestEntity.getCompletedHistory(companyId, category, fromDate, toDate, category, new HelpRequestEntity.ListCallback() {
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