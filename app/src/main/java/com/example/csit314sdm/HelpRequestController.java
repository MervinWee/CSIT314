package com.example.csit314sdm;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }
    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }
    public interface SaveCallback {
        void onSaveSuccess();
        void onSaveFailure(String errorMessage);
    }


    public HelpRequestController() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // --- START: THIS IS THE CORRECTED AND FINAL VERSION OF THE METHOD ---
    public void getHelpRequestById(String requestId, String userRole, final SingleRequestLoadCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            if (callback != null) callback.onDataLoadFailed("Invalid Request ID provided.");
            return;
        }

        if ("CSR".equals(userRole)) {
            db.collection("help_requests").document(requestId).update("viewCount", FieldValue.increment(1));
        }

        db.collection("help_requests").document(requestId).get()
                .addOnSuccessListener(requestDoc -> {
                    if (!requestDoc.exists()) {
                        if (callback != null) callback.onDataLoadFailed("Request not found.");
                        return;
                    }

                    HelpRequest request = requestDoc.toObject(HelpRequest.class);
                    if (request == null) {
                        if (callback != null) callback.onDataLoadFailed("Failed to parse request data.");
                        return;
                    }
                    request.setId(requestDoc.getId());

                    // Now, fetch the PIN user's profile to enrich the request object
                    if (request.getSubmittedBy() != null && !request.getSubmittedBy().isEmpty()) {
                        db.collection("users").document(request.getSubmittedBy()).get()
                                .addOnSuccessListener(pinUserDoc -> {
                                    if (pinUserDoc.exists()) {
                                        // Set the publicly visible details
                                        request.setPinName(pinUserDoc.getString("fullName"));
                                        Object shortIdObj = pinUserDoc.get("shortId");
                                        if(shortIdObj != null) {
                                            request.setPinShortId(String.valueOf(shortIdObj));
                                        }

                                        // Also fetch and set the phone number. This will be shown/hidden in the UI.
                                        request.setPinPhoneNumber(pinUserDoc.getString("phoneNumber"));

                                    } else {
                                        // If PIN user document is missing
                                        request.setPinName("Unknown User");
                                        request.setPinPhoneNumber("Not Available");
                                    }
                                    // Return the fully enriched request object
                                    if (callback != null) callback.onRequestLoaded(request);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to fetch the user profile
                                    request.setPinName("Error");
                                    request.setPinPhoneNumber("Error");
                                    if (callback != null) callback.onRequestLoaded(request);
                                });
                    } else {
                        // No user is associated with this request
                        request.setPinName("N/A");
                        request.setPinPhoneNumber("N/A");
                        if (callback != null) callback.onRequestLoaded(request);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onDataLoadFailed(e.getMessage());
                });
    }
    // --- END: REPLACEMENT OF getHelpRequestById ---


    public void cancelRequest(String requestId, final DeleteCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            if (callback != null) callback.onDeleteFailure("Invalid Request ID.");
            return;
        }
        db.collection("help_requests").document(requestId).update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onDeleteSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onDeleteFailure(e.getMessage());
                });
    }

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

    public void getActiveHelpRequests(final HelpRequestsLoadCallback callback) {
        db.collection("help_requests").whereEqualTo("status", "Open")
                .orderBy("creationTimestamp", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            requestList.add(request);
                        }
                        if (callback != null) callback.onRequestsLoaded(requestList);
                    } else {
                        if (callback != null) callback.onDataLoadFailed("Failed to load active requests: " + task.getException().getMessage());
                    }
                });
    }

    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        db.collection("help_requests").whereArrayContains("savedByCsrId", currentCsrId).get()
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
                        Collections.sort(requestList, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));
                        if (callback != null) callback.onRequestsLoaded(requestList);
                    } else {
                        if (callback != null) callback.onDataLoadFailed("Failed to load saved requests. Error: " + task.getException().getMessage());
                    }
                });
    }

    public void saveRequest(String requestId, final SaveCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        db.collection("help_requests").document(requestId).update("savedByCsrId", FieldValue.arrayUnion(currentCsrId))
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSaveSuccess();
                }).addOnFailureListener(e -> {
                    if (callback != null) callback.onSaveFailure(e.getMessage());
                });
    }

    public void unsaveRequest(String requestId, final SaveCallback callback) {
        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        db.collection("help_requests").document(requestId).update("savedByCsrId", FieldValue.arrayRemove(currentCsrId))
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSaveSuccess();
                }).addOnFailureListener(e -> {
                    if (callback != null) callback.onSaveFailure(e.getMessage());
                });
    }

    public void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        if (companyId == null || companyId.isEmpty()) {
            if (callback != null) callback.onDataLoadFailed("Company ID is required to fetch history.");
            return;
        }

        Query query = db.collection("help_requests")
                .whereEqualTo("status", "Completed")
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
                if (callback != null) callback.onRequestsLoaded(requests);
            } else {
                if (callback != null) callback.onDataLoadFailed("Query failed. Check logs for index requirements. Error: " + task.getException().getMessage());
            }
        });
    }

    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onDataLoadFailed("No user logged in.");
            return;
        }
        String currentCsrId = currentUser.getUid();

        db.collection("help_requests").whereArrayContains("savedByCsrId", currentCsrId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            if (Arrays.asList("Open", "In-progress").contains(request.getStatus())) {
                                request.setId(document.getId());
                                requests.add(request);
                            }
                        }

                        List<HelpRequest> filteredRequests = requests.stream().filter(r -> {
                            boolean matches = true;
                            if (location != null && !location.isEmpty() && !location.equals("All")) matches = r.getLocation().equals(location);
                            if (matches && category != null && !category.isEmpty() && !category.equals("All")) matches = r.getCategory().equals(category);
                            if (matches && keyword != null && !keyword.isEmpty()) matches = r.getTitle().toLowerCase().contains(keyword.toLowerCase());
                            return matches;
                        }).collect(Collectors.toList());

                        Collections.sort(filteredRequests, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));

                        if (callback != null) callback.onRequestsLoaded(filteredRequests);
                    } else {
                        if (callback != null) callback.onDataLoadFailed("Query failed. Error: " + task.getException().getMessage());
                    }
                });
    }

    public void acceptRequest(String requestId, String companyId, String csrId, final UpdateCallback callback) {
        if (csrId == null || csrId.isEmpty()) {
            if (callback != null) callback.onUpdateFailure("Cannot accept request: User ID is invalid.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "In-progress");
        updates.put("acceptedByCsrId", csrId);
        updates.put("companyId", companyId);
        updates.put("savedByCsrId", FieldValue.arrayUnion(csrId));

        db.collection("help_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onUpdateSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onUpdateFailure(e.getMessage());
                });
    }

    public void releaseRequestByCsr(String requestId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Open");
        updates.put("acceptedByCsrId", FieldValue.delete());
        updates.put("companyId", FieldValue.delete());

        db.collection("help_requests").document(requestId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onUpdateSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onUpdateFailure(e.getMessage());
                });
    }

    public void updateRequestStatus(String requestId, String newStatus, final UpdateCallback callback) {
        db.collection("help_requests").document(requestId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onUpdateSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onUpdateFailure(e.getMessage());
                });
    }
}
