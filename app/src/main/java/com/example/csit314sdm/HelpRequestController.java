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

    public void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        HelpRequest.getCompletedHistory(companyId, fromDate, toDate, category, new HelpRequest.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        HelpRequest.searchShortlistedRequests(keyword, location, category, new HelpRequest.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequest.getSavedHelpRequests(new HelpRequest.HelpRequestsLoadCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                callback.onDataLoadFailed(errorMessage);
            }
        });
    }

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
        // --- FIX: This query now ONLY fetches "Open" requests. ---
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

    public void getInProgressRequestsForCsr(final HelpRequestsLoadCallback callback) {        String currentCsrId = auth.getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        db.collection("help_requests")
                .whereEqualTo("status", "In-progress")
                .whereEqualTo("acceptedByCsrId", currentCsrId)
                // Note: Firestore can only order by the field used in the last 'whereEqualTo'
                // so we will sort for urgency on the client side for simplicity.
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            requests.add(request);
                        }

                        // --- SORT BY URGENCY on the client ---
                        // Create a mapping for urgency levels to sort them correctly
                        Map<String, Integer> urgencyOrder = new HashMap<>();
                        urgencyOrder.put("Critical", 1);
                        urgencyOrder.put("High", 2);
                        urgencyOrder.put("Medium", 3);
                        urgencyOrder.put("Low", 4);

                        Collections.sort(requests, (r1, r2) -> {
                            int urgency1 = urgencyOrder.getOrDefault(r1.getUrgencyLevel(), 5);
                            int urgency2 = urgencyOrder.getOrDefault(r2.getUrgencyLevel(), 5);
                            return Integer.compare(urgency1, urgency2);
                        });

                        if (callback != null) callback.onRequestsLoaded(requests);
                    } else {
                        if (callback != null) callback.onDataLoadFailed("Failed to load your requests: " + task.getException().getMessage());
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

    // --- START: THIS IS THE FIX ---
    // The method signature is updated to accept the csrId as the third argument.
    public void acceptRequest(String requestId, String companyId, String csrId, final UpdateCallback callback) {
        if (csrId == null || csrId.isEmpty()) {
            if (callback != null) callback.onUpdateFailure("Cannot accept request: User ID is invalid.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "In-progress");
        updates.put("acceptedByCsrId", csrId); // Use the passed-in csrId
        updates.put("companyId", companyId);
        updates.put("savedByCsrId", FieldValue.arrayUnion(csrId)); // Automatically shortlist

        db.collection("help_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onUpdateSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onUpdateFailure(e.getMessage());
                });
    }
    // --- END: THIS IS THE FIX ---


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
    public void releaseRequestByPin(String requestId, final UpdateCallback callback) {
        db.collection("help_requests").document(requestId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                if (callback != null) callback.onUpdateFailure("Request not found.");
                return;
            }

            HelpRequest request = documentSnapshot.toObject(HelpRequest.class);
            if (request == null) {
                if (callback != null) callback.onUpdateFailure("Failed to read request data.");
                return;
            }

            String currentUrgency = request.getUrgencyLevel();
            String newUrgency = currentUrgency;

            // Elevate the urgency level
            switch (currentUrgency.toLowerCase()) {
                case "low":
                    newUrgency = "Medium";
                    break;
                case "medium":
                    newUrgency = "High";
                    break;
                case "high":
                    // If it's already high, it can stay high or become critical
                    newUrgency = "Critical";
                    break;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "Open"); // Set status back to Open
            updates.put("urgencyLevel", newUrgency); // Set the new, higher urgency

            // Remove the assigned CSR and their company
            updates.put("acceptedByCsrId", FieldValue.delete());
            updates.put("companyId", FieldValue.delete());

            // Note: We intentionally do NOT remove the CSR from the `savedByCsrId` list.
            // This is a business decision. You could if you wanted to.

            db.collection("help_requests").document(requestId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onUpdateSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onUpdateFailure(e.getMessage());
                    });
        }).addOnFailureListener(e -> {
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
