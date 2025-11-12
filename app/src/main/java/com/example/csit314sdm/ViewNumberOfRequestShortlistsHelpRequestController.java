package com.example.csit314sdm;

import com.example.csit314sdm.HelpRequestEntity;

public class ViewNumberOfRequestShortlistsHelpRequestController {

    public interface ShortlistCallback {
        void onShortlistCountLoaded(int count);
        void onLoadFailed(String errorMessage);
    }

    public static void getNumberOfShortlists(String requestId, ShortlistCallback callback) {
        HelpRequestEntity.getShortlistCount(requestId, new HelpRequestEntity.ShortlistCountCallback() {
            @Override
            public void onSuccess(int count) {
                if (callback != null) callback.onShortlistCountLoaded(count);
            }

            @Override
            public void onFailure(String errorMessage) {
                if (callback != null) callback.onLoadFailed(errorMessage);
            }
        });
    }
}
