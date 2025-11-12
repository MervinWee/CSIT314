package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

public class DeleteHelpRequestController {

    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }

    public static void deleteHelpRequest(String requestId, DeleteCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            if (callback != null) callback.onDeleteFailure("Invalid request ID.");
            return;
        }

        HelpRequest.deleteRequest(requestId, new HelpRequest.DeleteCallback() {
            @Override
            public void onDeleteSuccess() {
                if (callback != null) callback.onDeleteSuccess();
            }

            @Override
            public void onDeleteFailure(String errorMessage) {
                if (callback != null) callback.onDeleteFailure(errorMessage);
            }
        });
    }
}
