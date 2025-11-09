package com.example.csit314sdm;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class HelpRequest {

    private String id;
    private String title;
    private String description;

    private String region;
    private String location;
    private String submittedBy;
    private String pinId;
    private String pinName;
    private String pinShortId;
    private String requestType;
    private String preferredTime;
    private String urgencyLevel;
    private String status;
    private String category;
    private String organization;
    private String companyId;
    private String acceptedByCsrId;
    private Date shortlistedDate;
    private long viewCount = 0;
    private List<String> savedByCsrId;


    private String pinPhoneNumber;

    public interface HelpRequestsLoadCallback {
        void onRequestsLoaded(List<HelpRequest> requests);
        void onDataLoadFailed(String errorMessage);
    }

    public interface MyMatchesCallback {
        void onMatchesLoaded(List<User> matchedUsers);
        void onDataLoadFailed(String errorMessage);
    }

    public interface SaveCallback {
        void onSaveSuccess();
        void onSaveFailure(String errorMessage);
    }

    @ServerTimestamp
    private Date creationTimestamp;

    public HelpRequest() {}

    public static void saveRequest(String requestId, final SaveCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId)
                .update("savedByCsrId", FieldValue.arrayUnion(currentCsrId))
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSaveSuccess();
                }).addOnFailureListener(e -> {
                    if (callback != null) callback.onSaveFailure(e.getMessage());
                });
    }

    public static void unsaveRequest(String requestId, final SaveCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onSaveFailure("No user is currently logged in.");
            return;
        }
        FirebaseFirestore.getInstance().collection("help_requests").document(requestId)
                .update("savedByCsrId", FieldValue.arrayRemove(currentCsrId))
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSaveSuccess();
                }).addOnFailureListener(e -> {
                    if (callback != null) callback.onSaveFailure(e.getMessage());
                });
    }

    public static void searchMyCompletedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onDataLoadFailed("No user logged in.");
            return;
        }
        String currentCsrId = currentUser.getUid();

        Query query = FirebaseFirestore.getInstance().collection("help_requests")
                .whereEqualTo("status", "Completed")
                .whereEqualTo("acceptedByCsrId", currentCsrId);

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

    public static void getMatchesForCsr(final MyMatchesCallback callback) {
        String currentCsrId = FirebaseAuth.getInstance().getUid();
        if (currentCsrId == null) {
            if (callback != null) callback.onDataLoadFailed("No user is currently logged in.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("help_requests")
                .whereEqualTo("status", "Completed")
                .whereEqualTo("acceptedByCsrId", currentCsrId)
                .get()
                .addOnSuccessListener(completedRequestsSnapshot -> {
                    if (completedRequestsSnapshot.isEmpty()) {
                        callback.onMatchesLoaded(new ArrayList<>());
                        return;
                    }

                    HashSet<String> pinUserIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : completedRequestsSnapshot) {
                        String pinId = doc.getString("submittedBy");
                        if (pinId != null && !pinId.isEmpty()) {
                            pinUserIds.add(pinId);
                        }
                    }

                    if (pinUserIds.isEmpty()) {
                        callback.onMatchesLoaded(new ArrayList<>());
                        return;
                    }

                    db.collection("users").whereIn(FieldPath.documentId(), new ArrayList<>(pinUserIds)).get()
                            .addOnSuccessListener(pinUsersSnapshot -> {
                                List<User> matchedUsers = new ArrayList<>();
                                for (DocumentSnapshot userDoc : pinUsersSnapshot.getDocuments()) {
                                    User user = userDoc.toObject(User.class);
                                    if (user != null) {
                                        user.setId(userDoc.getId());
                                        matchedUsers.add(user);
                                    }
                                }
                                callback.onMatchesLoaded(matchedUsers);
                            })
                            .addOnFailureListener(e -> callback.onDataLoadFailed("Failed to fetch user profiles: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onDataLoadFailed("Failed to find completed requests: " + e.getMessage()));
    }
    
    public static void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
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

    public static void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
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

    public static void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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


    public String getPinPhoneNumber() { return pinPhoneNumber; }
    public void setPinPhoneNumber(String pinPhoneNumber) { this.pinPhoneNumber = pinPhoneNumber; }


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

    public Date getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Date creationTimestamp) { this.creationTimestamp = creationTimestamp; }
}
