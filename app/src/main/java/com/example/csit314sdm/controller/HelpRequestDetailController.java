package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class HelpRequestDetailController {

    // Define a callback interface to handle asynchronous data loading
    public interface RequestDetailCallback {
        void onRequestLoaded(HelpRequest request);
        void onDataLoadFailed(String errorMessage);
    }


    public void getRequestById(String requestId, final RequestDetailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("help_requests").document(requestId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            HelpRequest request = document.toObject(HelpRequest.class);
                            if (request != null) {
                                callback.onRequestLoaded(request);
                            } else {
                                callback.onDataLoadFailed("Failed to parse request data.");
                            }
                        } else {
                            callback.onDataLoadFailed("Request not found.");
                        }
                    } else {
                        callback.onDataLoadFailed(task.getException().getMessage());
                    }
                });
    }
}
