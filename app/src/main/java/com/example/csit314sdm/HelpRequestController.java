package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// CONTROL: Manages the business logic for fetching and managing help requests.
public class HelpRequestController {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Callback for loading a list of help requests
    public interface HelpRequestsLoadCallback {
        void onRequestsLoaded(List<HelpRequest> requests);
        void onDataLoadFailed(String errorMessage);
    }

    public HelpRequestController() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // In HelpRequestController.java// New callback interface for loading a single request
    public interface HelpRequestLoadCallback {
        void onRequestLoaded(HelpRequest request);
        void onDataLoadFailed(String errorMessage);
    }

    // New method to fetch a single document by its ID
    public void getHelpRequestById(String requestId, final HelpRequestLoadCallback callback) {
        db.collection("help_requests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HelpRequest request = documentSnapshot.toObject(HelpRequest.class);
                        if (request != null) {
                            request.setId(documentSnapshot.getId()); // Set the ID
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

    /**
     * Fetches all help requests that have been "saved" by the currently logged-in CSR.
     */
    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        db.collection("help_requests")
                // Query for documents where 'savedByCsrId' matches the current CSR's ID
                .whereEqualTo("savedByCsrId", currentCsrId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // Show newest first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId()); // Manually set the document ID
                            requestList.add(request);
                        }
                        callback.onRequestsLoaded(requestList);
                    } else {
                        callback.onDataLoadFailed("Failed to load requests: " + task.getException().getMessage());
                    }
                });
    }
}
