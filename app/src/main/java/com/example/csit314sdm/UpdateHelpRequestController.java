package com.example.csit314sdm;
public class UpdateHelpRequestController {

    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public static void updateHelpRequest(String requestId, HelpRequestEntity updatedRequest, UpdateCallback callback) {
        if (requestId == null || requestId.isEmpty() || updatedRequest == null) {
            if (callback != null) callback.onUpdateFailure("Invalid request data.");
            return;
        }

        HelpRequestEntity.updateRequest(requestId, updatedRequest.toMap(), new HelpRequestEntity.UpdateCallback() {
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
