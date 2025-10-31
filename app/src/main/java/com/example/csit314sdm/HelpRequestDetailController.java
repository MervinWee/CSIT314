package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class HelpRequestDetailController {

    // Define a callback interface to handle asynchronous data loading
    public interface RequestDetailCallback {
        void onRequestLoaded(HelpRequest request);
        void onDataLoadFailed(String errorMessage);
    }

    // Method to fetch a single help request by its ID
    public void getRequestById(String requestId, final RequestDetailCallback callback) {
        // This example assumes you are using Firestore.
        // Replace with your actual data source if different.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("help_requests").document(requestId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Convert the document snapshot to a HelpRequest object
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
