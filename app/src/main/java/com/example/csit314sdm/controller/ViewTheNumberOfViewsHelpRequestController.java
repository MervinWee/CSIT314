package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

public class ViewTheNumberOfViewsHelpRequestController {

    public interface ViewsCallback {
        void onViewCountLoaded(long viewCount);
        void onLoadFailed(String errorMessage);
    }

    public static void getNumberOfViews(String requestId, ViewsCallback callback) {
        HelpRequest.getViewCount(requestId, new HelpRequest.ViewCountCallback() {
            @Override
            public void onSuccess(long count) {
                if (callback != null) callback.onViewCountLoaded(count);
            }

            @Override
            public void onFailure(String errorMessage) {
                if (callback != null) callback.onLoadFailed(errorMessage);
            }
        });
    }
}
