package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

public class ViewNumberOfRequestShortlistsHelpRequestController {

    public interface ShortlistCallback {
        void onShortlistCountLoaded(int count);
        void onLoadFailed(String errorMessage);
    }

    public static void getNumberOfShortlists(String requestId, ShortlistCallback callback) {
        HelpRequest.getShortlistCount(requestId, new HelpRequest.ShortlistCountCallback() {
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
