package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

public class UpdateHelpRequestController {

    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public static void updateHelpRequest(String requestId, HelpRequest updatedRequest, UpdateCallback callback) {
        if (requestId == null || requestId.isEmpty() || updatedRequest == null) {
            if (callback != null) callback.onUpdateFailure("Invalid request data.");
            return;
        }

        HelpRequest.updateRequest(requestId, updatedRequest.toMap(), new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onUpdateSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onUpdateFailure(errorMessage);
            }
        });
    }
}
