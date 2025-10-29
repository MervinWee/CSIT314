// File: C:/Users/suhai/StudioProjects/CSIT314/app/src/main/java/com/example/csit314sdm/HelpRequestController.java
// FINAL CORRECTED VERSION with all syntax errors fixed.

package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// CONTROL: Manages the business logic for fetching and managing help requests.
public class HelpRequestController {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface HelpRequestsLoadCallback {
        void onRequestsLoaded(List<HelpRequest> requests);
        void onDataLoadFailed(String errorMessage);
    }

    public interface SingleRequestLoadCallback {
        void onRequestLoaded(HelpRequest request);
        void onDataLoadFailed(String errorMessage);
    }

    public HelpRequestController() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void getHelpRequestById(String requestId, final SingleRequestLoadCallback callback) {
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

    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        db.collection("help_requests")
                .whereEqualTo("savedByCsrId", currentCsrId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            requestList.add(request);
                        }
                        callback.onRequestsLoaded(requestList);
                    } else {
                        callback.onDataLoadFailed("Failed to load requests: " + task.getException().getMessage());
                    }
                });
    }

    public void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        if (companyId == null || companyId.isEmpty()) {
            callback.onDataLoadFailed("Company ID is required to fetch history.");
            return;
        }

        Query query = db.collection("help_requests")
                .whereEqualTo("status", "completed")
                .whereEqualTo("companyId", companyId);

        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("completedAt", fromDate);
        }
        if (toDate != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(toDate);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            query = query.whereLessThanOrEqualTo("completedAt", c.getTime());
        }

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("category", category);
        }

        query = query.orderBy("completedAt", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HelpRequest> requests = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    HelpRequest request = document.toObject(HelpRequest.class);
                    request.setId(document.getId());
                    requests.add(request);
                }
                callback.onRequestsLoaded(requests);
            } else {
                callback.onDataLoadFailed("Query failed. Check logs for index requirement. Error: " + task.getException().getMessage());
            }
        });
    }

    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onDataLoadFailed("No user logged in.");
            return;
        }
        String currentCsrId = currentUser.getUid();

        Query query = db.collection("help_requests").whereEqualTo("savedByCsrId", currentCsrId);

        boolean isSearchingByKeyword = keyword != null && !keyword.isEmpty();

        if (location != null && !location.isEmpty() && !location.equals("All")) {
            query = query.whereEqualTo("location", location);
        }
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            query = query.whereEqualTo("category", category);
        }

        if (isSearchingByKeyword) {
            String lowercaseKeyword = keyword.toLowerCase();
            query = query.whereGreaterThanOrEqualTo("title_lowercase", lowercaseKeyword)
                    .whereLessThanOrEqualTo("title_lowercase", lowercaseKeyword + '\uf8ff');
            query = query.orderBy("title_lowercase");
        } else {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HelpRequest> requests = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    HelpRequest request = document.toObject(HelpRequest.class);
                    request.setId(document.getId());
                    requests.add(request);
                }
                callback.onRequestsLoaded(requests);
            } else {
                callback.onDataLoadFailed("Query failed. Check logs for index requirements. Error: " + task.getException().getMessage());
            }
        });
    }
}





















