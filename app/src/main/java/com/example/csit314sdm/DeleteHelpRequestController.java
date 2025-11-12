package com.example.csit314sdm;

import com.example.csit314sdm.HelpRequestEntity;

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

        HelpRequestEntity.deleteRequest(requestId, new HelpRequestEntity.DeleteCallback() {
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
