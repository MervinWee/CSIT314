package com.example.csit314sdm;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldPath; // <-- THIS IS THE FIX

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

    // Interfaces for callbacks
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
    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public HelpRequestController() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void getHelpRequestById(String requestId, String userRole, final SingleRequestLoadCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            callback.onDataLoadFailed("Invalid Request ID provided.");
            return;
        }

        if ("CSR".equals(userRole)) {
            db.collection("help_requests").document(requestId)
                    .update("viewCount", FieldValue.increment(1));
        }

        Task<DocumentSnapshot> requestTask = db.collection("help_requests").document(requestId).get();

        requestTask.addOnSuccessListener(requestDoc -> {
            if (!requestDoc.exists()) {
                callback.onDataLoadFailed("Request not found.");
                return;
            }

            HelpRequest request = requestDoc.toObject(HelpRequest.class);
            if (request == null) {
                callback.onDataLoadFailed("Failed to parse request data.");
                return;
            }
            request.setId(requestDoc.getId());

            if (request.getSubmittedBy() != null && !request.getSubmittedBy().isEmpty()) {
                Task<DocumentSnapshot> pinUserTask = db.collection("users").document(request.getSubmittedBy()).get();
                Tasks.whenAllSuccess(pinUserTask).addOnSuccessListener(results -> {
                    DocumentSnapshot pinUserDoc = (DocumentSnapshot) results.get(0);
                    if (pinUserDoc.exists()) {
                        request.setPinName(pinUserDoc.getString("fullName"));
                        request.setPinShortId(pinUserDoc.getString("shortId"));
                    } else {
                        request.setPinName("Unknown User");
                        request.setPinShortId("Unknown");
                    }
                    callback.onRequestLoaded(request);
                }).addOnFailureListener(e -> {
                    request.setPinName("Error");
                    request.setPinShortId("Error");
                    callback.onRequestLoaded(request);
                });
            } else {
                request.setPinName("None");
                request.setPinShortId("None");
                callback.onRequestLoaded(request);
            }
        }).addOnFailureListener(e -> callback.onDataLoadFailed(e.getMessage()));
    }

    // ... [The rest of your HelpRequestController code remains exactly the same]
    // [I have omitted it for brevity, but the single import fix is all that's needed]
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
        if (currentUser == null) {
            return null;
        }
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
            Calendar c = Calendar.getInstance();
            c.setTime(toDate);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            query = query.whereLessThanOrEqualTo("creationTimestamp", c.getTime());
        }

        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }

    public void acceptRequest(String requestId, String companyId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "In-progress");
        updates.put("companyId", companyId);

        db.collection("help_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onUpdateSuccess())
                .addOnFailureListener(e -> callback.onUpdateFailure(e.getMessage()));
    }

    public void getActiveHelpRequests(String currentCsrId, final HelpRequestsLoadCallback callback) {
        db.collection("help_requests")
                .whereIn("status", Arrays.asList("Open", "In-progress"))
                .orderBy("creationTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> filteredList = new ArrayList<>();
                        List<Task<DocumentSnapshot>> userProfileTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());

                            if (request.getSavedByCsrId() == null || !request.getSavedByCsrId().contains(currentCsrId)) {
                                filteredList.add(request);
                                if (request.getSubmittedBy() != null && !request.getSubmittedBy().isEmpty()) {
                                    userProfileTasks.add(db.collection("users").document(request.getSubmittedBy()).get());
                                }
                            }
                        }

                        if (userProfileTasks.isEmpty()) {
                            callback.onRequestsLoaded(filteredList);
                            return;
                        }

                        Tasks.whenAllSuccess(userProfileTasks).addOnSuccessListener(results -> {
                            Map<String, String> pinNames = new HashMap<>();
                            for (Object snapshot : results) {
                                DocumentSnapshot userDoc = (DocumentSnapshot) snapshot;
                                if (userDoc.exists()) {
                                    pinNames.put(userDoc.getId(), userDoc.getString("fullName"));
                                }
                            }

                            for (HelpRequest request : filteredList) {
                                if (request.getSubmittedBy() != null) {
                                    request.setPinName(pinNames.getOrDefault(request.getSubmittedBy(), "Unknown User"));
                                }
                            }
                            callback.onRequestsLoaded(filteredList);
                        }).addOnFailureListener(e -> {
                            Log.e("HelpRequestController", "Failed to fetch all PIN profiles for active requests.", e);
                            callback.onRequestsLoaded(filteredList);
                        });

                    } else {
                        callback.onDataLoadFailed("Failed to load active requests: " + task.getException().getMessage());
                    }
                });
    }

    public void getSavedHelpRequests(String csrId, final HelpRequestsLoadCallback callback) {
        if (csrId == null || csrId.isEmpty()) {
            callback.onDataLoadFailed("Cannot load saved requests: User ID is invalid.");
            return;
        }

        db.collection("help_requests")
                .whereArrayContains("savedByCsrId", csrId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requestList = new ArrayList<>();
                        List<Task<DocumentSnapshot>> userProfileTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            if (Arrays.asList("Open", "In-progress").contains(request.getStatus())) {
                                requestList.add(request);
                                if (request.getSubmittedBy() != null && !request.getSubmittedBy().isEmpty()) {
                                    userProfileTasks.add(db.collection("users").document(request.getSubmittedBy()).get());
                                }
                            }
                        }

                        if (userProfileTasks.isEmpty()) {
                            callback.onRequestsLoaded(requestList);
                            return;
                        }

                        Tasks.whenAllSuccess(userProfileTasks).addOnSuccessListener(results -> {
                            Map<String, String> pinNames = new HashMap<>();
                            for (Object snapshot : results) {
                                DocumentSnapshot userDoc = (DocumentSnapshot) snapshot;
                                if (userDoc.exists()) {
                                    pinNames.put(userDoc.getId(), userDoc.getString("fullName"));
                                }
                            }
                            for (HelpRequest request : requestList) {
                                if (request.getSubmittedBy() != null) {
                                    request.setPinName(pinNames.getOrDefault(request.getSubmittedBy(), "Unknown User"));
                                }
                            }
                            if (requestList.size() > 1) {
                                Collections.sort(requestList, (r1, r2) -> {
                                    if (r1.getCreationTimestamp() == null || r2.getCreationTimestamp() == null) return 0;
                                    return r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp());
                                });
                            }
                            callback.onRequestsLoaded(requestList);
                        }).addOnFailureListener(e -> {
                            Log.e("HelpRequestController", "Failed to fetch PIN profiles for saved requests.", e);
                            callback.onRequestsLoaded(requestList);
                        });

                    } else {
                        callback.onDataLoadFailed("Failed to load saved requests. Error: " + task.getException().getMessage());
                    }
                });
    }

    public void saveRequest(String requestId, final SaveCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        db.collection("help_requests").document(requestId)
                .update("savedByCsrId", FieldValue.arrayUnion(currentUser.getUid()))
                .addOnSuccessListener(aVoid -> callback.onSaveSuccess())
                .addOnFailureListener(e -> callback.onSaveFailure(e.getMessage()));
    }

    public void unsaveRequest(String requestId, final SaveCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        db.collection("help_requests").document(requestId)
                .update("savedByCsrId", FieldValue.arrayRemove(currentUser.getUid()))
                .addOnSuccessListener(aVoid -> callback.onSaveSuccess())
                .addOnFailureListener(e -> callback.onSaveFailure(e.getMessage()));
    }

    public void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        if (companyId == null || companyId.isEmpty()) {
            callback.onDataLoadFailed("Company ID is required to fetch history.");
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
                List<Task<DocumentSnapshot>> userProfileTasks = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    HelpRequest request = document.toObject(HelpRequest.class);
                    request.setId(document.getId());
                    requests.add(request);

                    if (request.getSubmittedBy() != null && !request.getSubmittedBy().isEmpty()) {
                        userProfileTasks.add(db.collection("users").document(request.getSubmittedBy()).get());
                    }
                }

                if (userProfileTasks.isEmpty()) {
                    callback.onRequestsLoaded(requests);
                    return;
                }

                Tasks.whenAllSuccess(userProfileTasks).addOnSuccessListener(results -> {
                    Map<String, String> pinNames = new HashMap<>();
                    for (Object snapshot : results) {
                        DocumentSnapshot userDoc = (DocumentSnapshot) snapshot;
                        if (userDoc.exists()) {
                            pinNames.put(userDoc.getId(), userDoc.getString("fullName"));
                        }
                    }
                    for (HelpRequest request : requests) {
                        if (request.getSubmittedBy() != null) {
                            request.setPinName(pinNames.getOrDefault(request.getSubmittedBy(), "Unknown User"));
                        }
                    }
                    callback.onRequestsLoaded(requests);
                }).addOnFailureListener(e -> {
                    Log.e("HelpRequestController", "Failed to fetch PIN profiles for completed history.", e);
                    callback.onRequestsLoaded(requests);
                });

            } else {
                callback.onDataLoadFailed("Query failed. Check logs for index requirements. Error: " + task.getException().getMessage());
            }
        });
    }

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
                        List<HelpRequest> initialRequests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            if (Arrays.asList("Open", "In-progress").contains(request.getStatus())) {
                                initialRequests.add(request);
                            }
                        }

                        List<HelpRequest> filteredRequests = initialRequests.stream()
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

                        if (filteredRequests.size() > 1) {
                            Collections.sort(filteredRequests, (r1, r2) -> {
                                if (r1.getCreationTimestamp() == null || r2.getCreationTimestamp() == null) return 0;
                                return r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp());
                            });
                        }

                        callback.onRequestsLoaded(filteredRequests);
                    } else {
                        callback.onDataLoadFailed("Query failed. Error: " + task.getException().getMessage());
                    }
                });
    }

}
