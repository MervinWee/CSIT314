package com.example.csit314sdm;

import com.example.csit314sdm.HelpRequestEntity;

public class ViewTheNumberOfViewsHelpRequestController {

    public interface ViewsCallback {
        void onViewCountLoaded(long viewCount);
        void onLoadFailed(String errorMessage);
    }

    public static void getNumberOfViews(String requestId, ViewsCallback callback) {
        HelpRequestEntity.getViewCount(requestId, new HelpRequestEntity.ViewCountCallback() {
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
