package com.example.csit314sdm;

public class MyCompletedRequestsController {

    public void searchMyCompletedRequests(String keyword, String location, String category, final HelpRequestController.HelpRequestsLoadCallback callback) {
        // This controller simply delegates the call to the static method in the HelpRequest entity.
        HelpRequest.searchMyCompletedRequests(keyword, location, category, new HelpRequestController.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(java.util.List<HelpRequest> requests) {
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
}
