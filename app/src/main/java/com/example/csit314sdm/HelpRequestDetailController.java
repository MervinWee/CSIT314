package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;

public class HelpRequestDetailController {

    private final FirebaseFirestore db;

    public interface RequestDetailCallback {
        void onRequestLoaded(HelpRequest request);
        void onDataLoadFailed(String errorMessage);
    }

    public HelpRequestDetailController() {
        db = FirebaseFirestore.getInstance();
    }

    public void getRequestById(String requestId, final RequestDetailCallback callback) {
        db.collection("help_requests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HelpRequest request = documentSnapshot.toObject(HelpRequest.class);
                        if (request != null) {
                            request.setId(documentSnapshot.getId());
                            callback.onRequestLoaded(request);
                        } else {
                            callback.onDataLoadFailed("Failed to parse request data.");
                        }
                    } else {
                        callback.onDataLoadFailed("Request not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onDataLoadFailed(e.getMessage()));
    }
}
