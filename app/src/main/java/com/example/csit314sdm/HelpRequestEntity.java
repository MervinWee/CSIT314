package com.example.csit314sdm;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.WriteBatch;

import java.util.*;
import java.util.stream.Collectors;

public class HelpRequestEntity {

    // --- Interfaces for Callbacks ---
    public interface CreateCallback { void onCreateSuccess(String documentId); void onCreateFailure(String errorMessage); }
    public interface LoadCallback { void onRequestLoaded(HelpRequestEntity request); void onDataLoadFailed(String errorMessage); }
    public interface DeleteCallback { void onDeleteSuccess(); void onDeleteFailure(String errorMessage); }
    public interface UpdateCallback { void onUpdateSuccess(); void onUpdateFailure(String errorMessage); }
    public interface ListCallback { void onRequestsLoaded(List<HelpRequestEntity> requests); void onDataLoadFailed(String errorMessage); }
    public interface ViewCountCallback { void onSuccess(long count); void onFailure(String errorMessage); }
    public interface ShortlistCountCallback { void onSuccess(int count); void onFailure(String errorMessage); }
    public interface CategoryListCallback { void onCategoriesLoaded(List<Category> categories); void onDataLoadFailed(String errorMessage); }
    public interface MyMatchesCallback { void onMatchesLoaded(List<User> matchedUsers); void onDataLoadFailed(String errorMessage); }

    // --- Fields ---
    private String id, title, description, region, location, submittedBy, pinId, pinName, pinShortId;
    private String requestType, preferredTime, urgencyLevel, status, category, organization, companyId, acceptedByCsrId;
    private Date shortlistedDate;
    private long viewCount = 0;
    private List<String> savedByCsrId;
    private String pinPhoneNumber;

    @ServerTimestamp
    private Date creationTimestamp;

    // --- Constructors ---
    public HelpRequestEntity() {}

    // --- Getters & Setters ---
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (title != null) map.put("title", title);
        if (description != null) map.put("description", description);
        if (region != null) map.put("region", region);
        if (location != null) map.put("location", location);
        if (requestType != null) map.put("requestType", requestType);
        if (preferredTime != null) map.put("preferredTime", preferredTime);
        if (urgencyLevel != null) map.put("urgencyLevel", urgencyLevel);
        if (status != null) map.put("status", status);
        if (category != null) map.put("category", category);
        if (organization != null) map.put("organization", organization);
        if (companyId != null) map.put("companyId", companyId);
        if (acceptedByCsrId != null) map.put("acceptedByCsrId", acceptedByCsrId);
        if (shortlistedDate != null) map.put("shortlistedDate", shortlistedDate);
        map.put("viewCount", viewCount);
        if (savedByCsrId != null) map.put("savedByCsrId", savedByCsrId);
        if (pinPhoneNumber != null) map.put("pinPhoneNumber", pinPhoneNumber);
        return map;
    }

    public void update(final UpdateCallback callback) {
        if (id == null) {
            if (callback != null) callback.onUpdateFailure("Request ID is missing.");
            return;
        }
        updateRequest(id, toMap(), callback);
    }

    // --- Firebase methods ---
    public static void createRequest(HelpRequestEntity request, CreateCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").add(request)
                .addOnSuccessListener(docRef -> { if (callback != null) callback.onCreateSuccess(docRef.getId()); })
                .addOnFailureListener(e -> { if (callback != null) callback.onCreateFailure(e.getMessage()); });
    }

    public static void updateRequest(String requestId, Map<String, Object> updates, UpdateCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }

    public static void deleteRequest(String requestId, DeleteCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).delete()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onDeleteSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onDeleteFailure(e.getMessage()); });
    }

    public static void findById(String requestId, String userRole, final LoadCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    if (callback != null) callback.onDataLoadFailed("Request not found");
                    return;
                }
                HelpRequestEntity request = doc.toObject(HelpRequestEntity.class);
                if (request != null) {
                    request.setId(doc.getId());
                    if ("CSR".equals(userRole)) {
                        FirebaseFirestore.getInstance().collection("help_requests").document(requestId)
                            .update("viewCount", FieldValue.increment(1));
                    }
                }
                if (callback != null) callback.onRequestLoaded(request);
            })
            .addOnFailureListener(e -> { if (callback != null) callback.onDataLoadFailed(e.getMessage()); });
    }

    public static void getAllRequests(Query query, ListCallback callback) {
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HelpRequestEntity> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    HelpRequestEntity req = doc.toObject(HelpRequestEntity.class);
                    req.setId(doc.getId());
                    list.add(req);
                }
                if (callback != null) callback.onRequestsLoaded(list);
            } else {
                if (callback != null) callback.onDataLoadFailed(task.getException().getMessage());
            }
        });
    }

    public static void getCategories(final CategoryListCallback callback) {
        FirebaseFirestore.getInstance().collection("categories").orderBy("name").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Category> categories = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Category category = document.toObject(Category.class);
                    category.setId(document.getId());
                    categories.add(category);
                }
                if (callback != null) callback.onCategoriesLoaded(categories);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onDataLoadFailed(e.getMessage());
            });
    }

    public static void cancelRequest(String requestId, final UpdateCallback callback) {
        updateRequestStatus(requestId, "Cancelled", callback);
    }

    public static Query getFilteredHelpRequestsQuery(String statusFilter, String userId, String userRole) {
        return getQueryFilteredRequests(statusFilter, userId, userRole);
    }

    public static Query getMatchHistoryQuery(String category, Date fromDate, Date toDate, String userId) {
        Query query = FirebaseFirestore.getInstance().collection("help_requests")
            .whereEqualTo("status", "Completed")
            .whereEqualTo("submittedBy", userId)
            .orderBy("creationTimestamp", Query.Direction.DESCENDING);
        if (category != null && !category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }
        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("creationTimestamp", fromDate);
        }
        if (toDate != null) {
            query = query.whereLessThanOrEqualTo("creationTimestamp", toDate);
        }
        return query;
    }

    public static void getActiveHelpRequests(final ListCallback callback) {
        Query query = FirebaseFirestore.getInstance().collection("help_requests")
            .whereIn("status", Arrays.asList("Open", "In-progress"))
            .orderBy("creationTimestamp", Query.Direction.DESCENDING);
        getAllRequests(query, callback);
    }

    public static void getInProgressRequestsForCsr(final ListCallback callback) {
        String csrId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = FirebaseFirestore.getInstance().collection("help_requests")
            .whereEqualTo("acceptedByCsrId", csrId)
            .whereEqualTo("status", "In-progress")
            .orderBy("creationTimestamp", Query.Direction.DESCENDING);
        getAllRequests(query, callback);
    }

    public static void getSavedHelpRequests(final ListCallback callback) {
        String csrId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = FirebaseFirestore.getInstance().collection("help_requests")
            .whereArrayContains("savedByCsrId", csrId)
            .whereIn("status", Arrays.asList("Open", "In-progress"))
            .orderBy("creationTimestamp", Query.Direction.DESCENDING);
        getAllRequests(query, callback);
    }

    public static void saveRequest(String requestId, final UpdateCallback callback) {
        String csrId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId)
            .update("savedByCsrId", FieldValue.arrayUnion(csrId))
            .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
            .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }

    public static void unsaveRequest(String requestId, final UpdateCallback callback) {
        String csrId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId)
            .update("savedByCsrId", FieldValue.arrayRemove(csrId))
            .addOnSuccessListener(aVoid -> { if (callback != null) callback.onUpdateSuccess(); })
            .addOnFailureListener(e -> { if (callback != null) callback.onUpdateFailure(e.getMessage()); });
    }

    public static void getCompletedHistory(String companyId, String csrId, Date fromDate, Date toDate, String category, final ListCallback callback) {
        Query query = FirebaseFirestore.getInstance().collection("help_requests")
            .whereEqualTo("status", "Completed")
            .whereEqualTo("acceptedByCsrId", csrId);

        if (companyId != null && !companyId.isEmpty()) {
            query = query.whereEqualTo("companyId", companyId);
        }
        if (category != null && !category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }
        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("creationTimestamp", fromDate);
        }
        if (toDate != null) {
            query = query.whereLessThanOrEqualTo("creationTimestamp", toDate);
        }
        getAllRequests(query.orderBy("creationTimestamp", Query.Direction.DESCENDING), callback);
    }

    public static void getFilteredHelpRequests(String status, String userId, String userRole, ListCallback callback) {
        Query query = getQueryFilteredRequests(status, userId, userRole);
        getAllRequests(query, callback);
    }

    public static void searchShortlistedRequests(String keyword, String location, String category, final ListCallback callback) {
        String csrId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = FirebaseFirestore.getInstance().collection("help_requests").whereArrayContains("savedByCsrId", csrId);

        if (keyword != null && !keyword.isEmpty()) {
            query = query.whereEqualTo("title", keyword); // Firestore doesn't support full-text search. This is a simple keyword match.
        }
        if (location != null && !location.isEmpty() && !location.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("location", location);
        }
        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("category", category);
        }
        getAllRequests(query.orderBy("creationTimestamp", Query.Direction.DESCENDING), callback);
    }

    public static void acceptRequest(String requestId, String companyId, String csrId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "In-progress");
        updates.put("acceptedByCsrId", csrId);
        updates.put("companyId", companyId);
        updateRequest(requestId, updates, callback);
    }

    public static void releaseRequestByCsr(String requestId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Open");
        updates.put("acceptedByCsrId", FieldValue.delete());
        updates.put("companyId", FieldValue.delete());
        updateRequest(requestId, updates, callback);
    }

    public static void releaseRequestByPin(String requestId, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Open");
        updates.put("pinId", FieldValue.delete());
        updates.put("pinName", FieldValue.delete());
        updates.put("pinShortId", FieldValue.delete());
        updates.put("pinPhoneNumber", FieldValue.delete());
        updateRequest(requestId, updates, callback);
    }

    public static void updateRequestStatus(String requestId, String newStatus, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updateRequest(requestId, updates, callback);
    }

    public static void getViewCount(String requestId, ViewCountCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long count = documentSnapshot.getLong("viewCount");
                    callback.onSuccess(count != null ? count : 0);
                } else {
                    callback.onFailure("Request not found");
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void getShortlistCount(String requestId, ShortlistCountCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> savedBy = (List<String>) documentSnapshot.get("savedByCsrId");
                    callback.onSuccess(savedBy != null ? savedBy.size() : 0);
                } else {
                    callback.onFailure("Request not found");
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static Query getQueryFilteredRequests(String status, String userId, String userRole) {
        Query query = FirebaseFirestore.getInstance().collection("help_requests");
        if (userId != null && "CSR".equals(userRole)) {
            query = query.whereEqualTo("acceptedByCsrId", userId);
        } else if (userId != null) {
            query = query.whereEqualTo("submittedBy", userId);
        }

        // FIX: Added '&& !status.equalsIgnoreCase("All")' to correctly handle the "All" filter case.
        if (status != null && !status.equalsIgnoreCase("All")) {
            if (status.equalsIgnoreCase("History")) {
                query = query.whereIn("status", Arrays.asList("Completed", "Cancelled"));
            } else if (status.equalsIgnoreCase("Active")) {
                query = query.whereIn("status", Arrays.asList("Open", "In-progress"));
            } else {
                query = query.whereEqualTo("status", status);
            }
        }

        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }

    public static void getMatchesForCsr(String csrId, final MyMatchesCallback callback) {
        FirebaseFirestore.getInstance().collection("help_requests")
                .whereEqualTo("acceptedByCsrId", csrId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onMatchesLoaded(new ArrayList<>());
                        return;
                    }

                    Set<String> pinIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String pinId = doc.getString("submittedBy");
                        if (pinId != null && !pinId.isEmpty()) {
                            pinIds.add(pinId);
                        }
                    }

                    if (pinIds.isEmpty()) {
                        callback.onMatchesLoaded(new ArrayList<>());
                        return;
                    }

                    FirebaseFirestore.getInstance().collection("users").whereIn("id", new ArrayList<>(pinIds)).get()
                            .addOnSuccessListener(userSnaps -> {
                                List<User> matchedUsers = new ArrayList<>();
                                for (DocumentSnapshot userDoc : userSnaps.getDocuments()) {
                                    User user = userDoc.toObject(User.class);
                                    if (user != null) {
                                        user.setId(userDoc.getId());
                                        matchedUsers.add(user);
                                    }
                                }
                                callback.onMatchesLoaded(matchedUsers);
                            })
                            .addOnFailureListener(e -> callback.onDataLoadFailed(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onDataLoadFailed(e.getMessage()));
    }

    public static void searchMyCompletedRequests(String csrId, String keyword, final ListCallback callback) {
        Query query = FirebaseFirestore.getInstance().collection("help_requests")
                .whereEqualTo("acceptedByCsrId", csrId)
                .whereEqualTo("status", "Completed");

        if (keyword != null && !keyword.isEmpty()) {
            query = query.whereEqualTo("title", keyword);
        }

        getAllRequests(query.orderBy("creationTimestamp", Query.Direction.DESCENDING), callback);
    }
}
