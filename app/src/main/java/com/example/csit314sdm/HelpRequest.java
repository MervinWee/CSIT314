package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelpRequest {

    public interface CreateCallback {
        void onCreateSuccess(String documentId);
        void onCreateFailure(String errorMessage);
    }

    public interface MyMatchesCallback {
        void onMatchesLoaded(List<User> matchedUsers);
        void onDataLoadFailed(String errorMessage);
    }

    // --- Fields ---
    private String id;
    private String title, description, region, location, submittedBy, pinId, pinName, pinShortId;
    private String requestType, preferredTime, urgencyLevel, status, category, organization, companyId, acceptedByCsrId;
    private Date shortlistedDate;
    private long viewCount = 0;
    private List<String> savedByCsrId;
    private String pinPhoneNumber;

    @ServerTimestamp
    private Date creationTimestamp;

    // --- Constructors ---
    public HelpRequest() {}

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    public String getPinId() { return pinId; }
    public void setPinId(String pinId) { this.pinId = pinId; }
    public String getPinName() { return pinName; }
    public void setPinName(String pinName) { this.pinName = pinName; }
    public String getPinShortId() { return pinShortId; }
    public void setPinShortId(String pinShortId) { this.pinShortId = pinShortId; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getAcceptedByCsrId() { return acceptedByCsrId; }
    public void setAcceptedByCsrId(String acceptedByCsrId) { this.acceptedByCsrId = acceptedByCsrId; }
    public Date getShortlistedDate() { return shortlistedDate; }
    public void setShortlistedDate(Date shortlistedDate) { this.shortlistedDate = shortlistedDate; }
    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public List<String> getSavedByCsrId() { return savedByCsrId; }
    public void setSavedByCsrId(List<String> savedByCsrId) { this.savedByCsrId = savedByCsrId; }
    public String getPinPhoneNumber() { return pinPhoneNumber; }
    public void setPinPhoneNumber(String pinPhoneNumber) { this.pinPhoneNumber = pinPhoneNumber; }
    public Date getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Date creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    // --- Active Record Methods ---

    public static void create(HelpRequest newRequest, final CreateCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").add(newRequest)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) {
                        callback.onCreateSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onCreateFailure(e.getMessage());
                    }
                });
    }

    public static void findById(String requestId, String userRole, final HelpRequestController.SingleRequestLoadCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

                if (request.getSubmittedBy() != null && !request.getSubmittedBy().isEmpty()) {
                    db.collection("users").document(request.getSubmittedBy()).get()
                        .addOnSuccessListener(pinUserDoc -> {
                            if (pinUserDoc.exists()) {
                                request.setPinName(pinUserDoc.getString("fullName"));
                                Object shortIdObj = pinUserDoc.get("shortId");
                                if (shortIdObj != null) {
                                    request.setPinShortId(String.valueOf(shortIdObj));
                                }
                                request.setPinPhoneNumber(pinUserDoc.getString("phoneNumber"));
                            } else {
                                request.setPinName("Unknown User");
                                request.setPinPhoneNumber("Not Available");
                            }
                            if (callback != null) callback.onRequestLoaded(request);
                        })
                        .addOnFailureListener(e -> {
                            request.setPinName("Error");
                            request.setPinPhoneNumber("Error");
                            if (callback != null) callback.onRequestLoaded(request);
                        });
                } else {
                    request.setPinName("N/A");
                    request.setPinPhoneNumber("N/A");
                    if (callback != null) callback.onRequestLoaded(request);
                }
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onDataLoadFailed(e.getMessage());
            });
    }

    public void update(final HelpRequestController.UpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (this.id == null || this.id.isEmpty()) {
            if (callback != null) callback.onUpdateFailure("Cannot update request without an ID.");
            return;
        }
        db.collection("help_requests").document(this.id).set(this)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onUpdateSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onUpdateFailure(e.getMessage());
            });
    }
    
    public static void getCategories(final HelpRequestController.CategoryListCallback callback) {
        FirebaseFirestore.getInstance().collection("HelpCategories").orderBy("name").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<Category> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Category category = doc.toObject(Category.class);
                            category.setId(doc.getId());
                            categories.add(category);
                        }
                        if (callback != null) callback.onCategoriesLoaded(categories);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onDataLoadFailed(e.getMessage());
                });
    }

    public static void cancelRequest(String requestId, final HelpRequestController.DeleteCallback callback) {
        if (requestId == null || requestId.isEmpty()) {
            if (callback != null) callback.onDeleteFailure("Invalid Request ID.");
            return;
        }
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onDeleteSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onDeleteFailure(e.getMessage()); });
    }

    public static Query getFilteredHelpRequestsQuery(String statusFilter) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();
        Query query = FirebaseFirestore.getInstance().collection("help_requests").whereEqualTo("submittedBy", pinId);

        if ("History".equalsIgnoreCase(statusFilter)) {
            query = query.whereIn("status", Arrays.asList("Completed", "Cancelled"));
        } else if ("Active".equalsIgnoreCase(statusFilter)) {
            query = query.whereIn("status", Arrays.asList("Open", "In-progress"));
        } else if (statusFilter != null && !statusFilter.isEmpty() && !"All".equalsIgnoreCase(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }

    public static Query getMatchHistoryQuery(String category, Date fromDate, Date toDate) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();

        Query query = FirebaseFirestore.getInstance().collection("help_requests")
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

    public static void getActiveHelpRequests(final HelpRequestController.HelpRequestsLoadCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").whereEqualTo("status", "Open")
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

    public static void getInProgressRequestsForCsr(final HelpRequestController.HelpRequestsLoadCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        FirebaseFirestore.getInstance().collection("help_requests")
                .whereEqualTo("status", "In-progress")
                .whereEqualTo("acceptedByCsrId", currentCsrId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HelpRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HelpRequest request = document.toObject(HelpRequest.class);
                            request.setId(document.getId());
                            requests.add(request);
                        }
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

    public static void getSavedHelpRequests(final HelpRequestController.HelpRequestsLoadCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }
        FirebaseFirestore.getInstance().collection("help_requests").whereArrayContains("savedByCsrId", currentCsrId).get()
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

    public static void saveRequest(String requestId, final HelpRequestController.SaveCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update("savedByCsrId", FieldValue.arrayUnion(currentCsrId))
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSaveSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onSaveFailure(e.getMessage()); });
    }
    
    public static void unsaveRequest(String requestId, final HelpRequestController.SaveCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update("savedByCsrId", FieldValue.arrayRemove(currentCsrId))
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSaveSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onSaveFailure(e.getMessage()); });
    }

    public static void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestController.HelpRequestsLoadCallback callback) {
        if (companyId == null || companyId.isEmpty()) {
            if (callback != null) callback.onDataLoadFailed("Company ID is required to fetch history.");
            return;
        }

        Query query = FirebaseFirestore.getInstance().collection("help_requests")
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

        query.orderBy("creationTimestamp", Query.Direction.DESCENDING).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<HelpRequest> requests = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HelpRequest request = document.toObject(HelpRequest.class);
                        request.setId(document.getId());
                        requests.add(request);
                    }
                    if (callback != null) callback.onRequestsLoaded(requests);
                } else {
                    if (callback != null) callback.onDataLoadFailed("Query failed. Error: " + task.getException().getMessage());
                }
            });
    }

    public static void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestController.HelpRequestsLoadCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onDataLoadFailed("No user logged in.");
            return;
        }
        String currentCsrId = currentUser.getUid();

        Query query = FirebaseFirestore.getInstance().collection("help_requests").whereArrayContains("savedByCsrId", currentCsrId);

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("category", category);
        }
        if (location != null && !location.isEmpty() && !location.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("region", location);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HelpRequest> requests = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    HelpRequest request = document.toObject(HelpRequest.class);
                    request.setId(document.getId());
                    requests.add(request);
                }

                if (keyword != null && !keyword.isEmpty()) {
                    List<HelpRequest> filteredByKeyword = requests.stream()
                            .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()) || r.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                            .collect(Collectors.toList());
                    Collections.sort(filteredByKeyword, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));
                    if (callback != null) callback.onRequestsLoaded(filteredByKeyword);
                } else {
                    Collections.sort(requests, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));
                    if (callback != null) callback.onRequestsLoaded(requests);
                }
            } else {
                if (callback != null) callback.onDataLoadFailed("Query failed. Error: " + task.getException().getMessage());
            }
        });
    }
    
    public static void searchMyCompletedRequests(String keyword, String location, String category, final HelpRequestController.HelpRequestsLoadCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onDataLoadFailed("No user logged in.");
            return;
        }
        String pinId = currentUser.getUid();

        Query query = FirebaseFirestore.getInstance().collection("help_requests")
                .whereEqualTo("submittedBy", pinId)
                .whereIn("status", Arrays.asList("Completed", "Cancelled"));

        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = query.whereEqualTo("category", category);
        }
        if (location != null && !location.isEmpty() && !"All".equalsIgnoreCase(location)) {
            query = query.whereEqualTo("region", location);
        }
        
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HelpRequest> requests = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    HelpRequest request = document.toObject(HelpRequest.class);
                    request.setId(document.getId());
                    requests.add(request);
                }

                if (keyword != null && !keyword.isEmpty()) {
                    List<HelpRequest> filteredByKeyword = requests.stream()
                            .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()) || r.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                            .collect(Collectors.toList());
                    Collections.sort(filteredByKeyword, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));
                    if (callback != null) callback.onRequestsLoaded(filteredByKeyword);
                } else {
                    Collections.sort(requests, (r1, r2) -> r2.getCreationTimestamp().compareTo(r1.getCreationTimestamp()));
                    if (callback != null) callback.onRequestsLoaded(requests);
                }
            } else {
                if (callback != null) callback.onDataLoadFailed("Query failed. Error: " + task.getException().getMessage());
            }
        });
    }

    public static void acceptRequest(String requestId, String companyId, String csrId, final HelpRequestController.UpdateCallback callback) {
        if (csrId == null || csrId.isEmpty()) {
            if (callback != null) callback.onUpdateFailure("Cannot accept request: User ID is invalid.");
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "In-progress");
        updates.put("acceptedByCsrId", csrId);
        updates.put("companyId", companyId);
        updates.put("savedByCsrId", FieldValue.arrayUnion(csrId));

        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }

    public static void releaseRequestByCsr(String requestId, final HelpRequestController.UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Open");
        updates.put("acceptedByCsrId", FieldValue.delete());
        updates.put("companyId", FieldValue.delete());

        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }

    public static void releaseRequestByPin(String requestId, final HelpRequestController.UpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
            switch (currentUrgency.toLowerCase()) {
                case "low": newUrgency = "Medium"; break;
                case "medium": newUrgency = "High"; break;
                case "high": newUrgency = "Critical"; break;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "Open");
            updates.put("urgencyLevel", newUrgency);
            updates.put("acceptedByCsrId", FieldValue.delete());
            updates.put("companyId", FieldValue.delete());

            db.collection("help_requests").document(requestId).update(updates)
                    .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
                    .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
        }).addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }

    public static void updateRequestStatus(String requestId, String newStatus, final HelpRequestController.UpdateCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }
    
    public static void getMatchesForCsr(final MyMatchesCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        FirebaseFirestore.getInstance().collection("help_requests")
            .whereEqualTo("acceptedByCsrId", currentCsrId)
            .whereEqualTo("status", "Completed")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    if (callback != null) callback.onMatchesLoaded(new ArrayList<>());
                    return;
                }

                List<String> pinIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    HelpRequest request = doc.toObject(HelpRequest.class);
                    if (request.getSubmittedBy() != null && !pinIds.contains(request.getSubmittedBy())) {
                        pinIds.add(request.getSubmittedBy());
                    }
                }

                if (pinIds.isEmpty()) {
                    if (callback != null) callback.onMatchesLoaded(new ArrayList<>());
                    return;
                }

                FirebaseFirestore.getInstance().collection("users")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), pinIds)
                    .get()
                    .addOnSuccessListener(userSnaps -> {
                        List<User> matchedUsers = new ArrayList<>();
                        for(QueryDocumentSnapshot doc : userSnaps){
                            User user = doc.toObject(User.class);
                            user.setId(doc.getId());
                            matchedUsers.add(user);
                        }
                        if (callback != null) callback.onMatchesLoaded(matchedUsers);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onDataLoadFailed(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onDataLoadFailed(e.getMessage());
            });
    }
}
