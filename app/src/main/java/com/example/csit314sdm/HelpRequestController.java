package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// CONTROL: Manages all business logic for fetching and managing help requests for ALL user types.
public class HelpRequestController {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Interfaces for callbacks to the UI (Boundary)
    public interface HelpRequestsLoadCallback {
        void onRequestsLoaded(List<HelpRequest> requests);
        void onDataLoadFailed(String errorMessage);
    }
    public interface SingleRequestLoadCallback {
        void onRequestLoaded(HelpRequest request);
        void onDataLoadFailed(String errorMessage);
    }
    public interface SaveCallback {
        void onSaveSuccess();
        void onSaveFailure(String errorMessage);
    }
    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }
    // ADDED: Callback for general updates
    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public HelpRequestController() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // --- Methods for Both PIN and CSR ---

    /**
     * Fetches a single help request by its ID AND atomically increments its view count.
     */
    public void getHelpRequestById(String requestId, String userRole, final SingleRequestLoadCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            callback.onDataLoadFailed("Invalid Request ID provided.");
            return;
        }

        if ("CSR".equals(userRole)) {
            db.collection("help_requests").document(requestId)
                    .update("viewCount", FieldValue.increment(1));
        }

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

    // ADDED: Method to update a request's status
    public void updateRequestStatus(String requestId, String newStatus, final UpdateCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            callback.onUpdateFailure("Invalid Request ID.");
            return;
        }
        db.collection("help_requests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onUpdateSuccess())
                .addOnFailureListener(e -> callback.onUpdateFailure(e.getMessage()));
    }

    // --- Methods primarily for PIN user ---

    /**
     * Updates a request's status to "Cancelled". Used by the PIN user.
     */
    public void cancelRequest(String requestId, final DeleteCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            callback.onDeleteFailure("Invalid Request ID.");
            return;
        }
        db.collection("help_requests").document(requestId)
                .update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> callback.onDeleteSuccess())
                .addOnFailureListener(e -> callback.onDeleteFailure(e.getMessage()));
    }

    /**
     * Builds a query for the PIN's "My Requests" screen with optional status filters.
     */
    public Query getFilteredHelpRequestsQuery(String statusFilter) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();
        Query query = db.collection("help_requests").whereEqualTo("submittedBy", pinId);

        if ("History".equalsIgnoreCase(statusFilter)) {
            query = query.whereIn("status", Arrays.asList("Completed", "Cancelled"));
        } else if ("Active".equalsIgnoreCase(statusFilter)) {
            query = query.whereIn("status", Arrays.asList("Open", "In-progress"));
        } else if (statusFilter != null && !statusFilter.isEmpty() && !"All".equalsIgnoreCase(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }

    /**
     * Builds a complex query for the PIN's "Match History" screen.
     */
    public Query getMatchHistoryQuery(String category, Date fromDate, Date toDate) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();

        Query query = db.collection("help_requests")
                .whereEqualTo("submittedBy", pinId)
                .whereIn("status", Arrays.asList("Completed", "Cancelled"));

        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = query.whereEqualTo("category", category);
        }
        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("creationTimestamp", fromDate);
        }
        if (toDate != null) {
            query = query.whereLessThanOrEqualTo("creationTimestamp", toDate);
        }

        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }


    // --- Methods primarily for CSR user ---

    /**
     * Fetches all requests that are not yet completed for the CSR dashboard.
     */
    public void getActiveHelpRequests(final HelpRequestsLoadCallback callback) {
        db.collection("help_requests")
                .whereIn("status", Arrays.asList("Open", "In-progress"))
                .orderBy("creationTimestamp", Query.Direction.DESCENDING)
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
                        callback.onDataLoadFailed("Failed to load active requests: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Fetches all requests saved (shortlisted) by the currently logged-in CSR.
     */
    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        db.collection("help_requests")
                .whereArrayContains("savedByCsrId", currentCsrId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            if (Arrays.asList("Open", "In-progress").contains(request.getStatus())) {
                                requestList.add(request);
                            }
                        }
                        // Sort by creation date in descending order
                        Collections.sort(requestList, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));
                        callback.onRequestsLoaded(requestList);
                    } else {
                        callback.onDataLoadFailed("Failed to load saved requests. Error: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Adds the CSR's ID to a request's 'savedByCsrId' list.
     */
    public void saveRequest(String requestId, final SaveCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        db.collection("help_requests").document(requestId)
                .update("savedByCsrId", FieldValue.arrayUnion(currentCsrId))
                .addOnSuccessListener(aVoid -> callback.onSaveSuccess())
                .addOnFailureListener(e -> callback.onSaveFailure(e.getMessage()));
    }

    /**
     * Removes the CSR's ID from a request's 'savedByCsrId' list.
     */
    public void unsaveRequest(String requestId, final SaveCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        db.collection("help_requests").document(requestId)
                .update("savedByCsrId", FieldValue.arrayRemove(currentCsrId))
                .addOnSuccessListener(aVoid -> callback.onSaveSuccess())
                .addOnFailureListener(e -> callback.onSaveFailure(e.getMessage()));
    }

    /**
     * Fetches completed requests for a specific company (CSR) within a given date range and category.
     */
    public void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        if (companyId == null || companyId.isEmpty()) {
            callback.onDataLoadFailed("Company ID is required to fetch history.");
            return;
        }

        Query query = db.collection("help_requests")
                .whereEqualTo("status", "completed")
                .whereEqualTo("companyId", companyId);

        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("creationTimestamp", fromDate);
        }
        if (toDate != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(toDate);
            c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59);
            query = query.whereLessThanOrEqualTo("creationTimestamp", c.getTime());
        }
        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("category", category);
        }

        query = query.orderBy("creationTimestamp", Query.Direction.DESCENDING);

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

    /**
     * Searches for requests that have been saved by the current CSR, with additional filters.
     */
    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onDataLoadFailed("No user logged in.");
            return;
        }
        String currentCsrId = currentUser.getUid();

        db.collection("help_requests").whereArrayContains("savedByCsrId", currentCsrId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            if (Arrays.asList("Open", "In-progress").contains(request.getStatus())) {
                                requests.add(request);
                            }
                            request.setId(document.getId());
                            requests.add(request);
                        }

                        // Manual Filtering
                        List<HelpRequest> filteredRequests = requests.stream()
                                .filter(r -> {
                                    boolean matches = true;
                                    if (location != null && !location.isEmpty() && !location.equals("All")) {
                                        matches = r.getLocation().equals(location);
                                    }
                                    if (matches && category != null && !category.isEmpty() && !category.equals("All")) {
                                        matches = r.getCategory().equals(category);
                                    }
                                    if (matches && keyword != null && !keyword.isEmpty()) {
                                        matches = r.getTitle().toLowerCase().contains(keyword.toLowerCase());
                                    }
                                    return matches;
                                })
                                .collect(Collectors.toList());

                        // Sort by creation date in descending order
                        Collections.sort(filteredRequests, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));

                        callback.onRequestsLoaded(filteredRequests);
                    } else {
                        callback.onDataLoadFailed("Query failed. Error: " + task.getException().getMessage());
                    }
                });
    }
}
