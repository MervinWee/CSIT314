package com.example.csit314sdm;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class PlatformDataAccount {

    private final CollectionReference usersRef, requestsRef, categoriesRef;
    private static final String TAG = "PlatformDataAccount"; // Added for logging

    // Callbacks
    public interface DailyReportCallback { void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount); void onError(String message); }
    public interface WeeklyReportCallback { void onReportDataLoaded(int uniquePins, int uniqueCsrs, int totalMatches); void onError(String message); }
    public interface MonthlyReportCallback { void onReportDataLoaded(String topCompany, String mostRequestedService); void onError(String message); }
    public interface FirebaseCallback { void onSuccess(); void onError(String message); }
    public interface CategoryListCallback { void onDataLoaded(List<Category> categories); void onError(String message); }
    public interface MigrationCallback { void onSuccess(String message); void onError(String message); }

    public PlatformDataAccount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        requestsRef = db.collection("help_requests"); // Assumes 'help_requests' collection
        categoriesRef = db.collection("HelpCategories");
    }

    public void generateDailyReport(Date date, final DailyReportCallback callback) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Date startDate = new Date(getStartOfDay(cal));
        Date endDate = new Date(getEndOfDay(cal));

        Task<QuerySnapshot> usersTask = usersRef
                .whereGreaterThanOrEqualTo("creationDate", startDate)
                .whereLessThanOrEqualTo("creationDate", endDate)
                .get();

        Task<QuerySnapshot> requestsTask = requestsRef
                .whereGreaterThanOrEqualTo("creationTimestamp", startDate)
                .whereLessThanOrEqualTo("creationTimestamp", endDate)
                .get();

        Task<QuerySnapshot> matchesTask = requestsRef
                .whereIn("status", Arrays.asList("Completed", "completed"))
                .whereGreaterThanOrEqualTo("creationTimestamp", startDate)
                .whereLessThanOrEqualTo("creationTimestamp", endDate)
                .get();

        // Combine all tasks
        Tasks.whenAllSuccess(usersTask, requestsTask, matchesTask).addOnSuccessListener(results -> {
            QuerySnapshot usersSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot requestsSnapshot = (QuerySnapshot) results.get(1);
            QuerySnapshot matchesSnapshot = (QuerySnapshot) results.get(2);

            int newUserCount = usersSnapshot.size();
            int newRequestCount = requestsSnapshot.size();
            int completedMatchesCount = matchesSnapshot.size();
            callback.onReportDataLoaded(newUserCount, newRequestCount, completedMatchesCount);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Daily report generation failed", e);
            String errorMsg = e.getMessage();
            if (errorMsg.contains("PERMISSION_DENIED")) {
                callback.onError("Permission Denied. Check user role and Firestore rules.");
            } else if (errorMsg.contains("requires an index")) {
                callback.onError("Query failed. A Firestore index is required. Check Logcat for the link.");
            } else {
                callback.onError("Query failed: " + errorMsg);
            }
        });
    }

    public void generateWeeklyReport(Date startDate, Date endDate, final WeeklyReportCallback callback) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Date adjustedStartDate = new Date(getStartOfDay(startCal));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        Date adjustedEndDate = new Date(getEndOfDay(endCal));

        // A single, efficient query for all requests created in the specified time frame.
        requestsRef
                .whereGreaterThanOrEqualTo("creationTimestamp", adjustedStartDate)
                .whereLessThanOrEqualTo("creationTimestamp", adjustedEndDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        // If there are no requests at all in the date range, we can stop here.
                        callback.onReportDataLoaded(0, 0, 0);
                        return;
                    }

                    // 1. Total Active Requests: All requests created in the period.
                    int totalActiveRequests = querySnapshot.size();

                    HashSet<String> uniquePinIds = new HashSet<>();
                    HashSet<String> uniqueCsrUserIds = new HashSet<>(); // The key change

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // 2. Unique Active PINs: Users who created a request.
                        String pinId = doc.getString("submittedBy");
                        if (pinId != null && !pinId.isEmpty()) {
                            uniquePinIds.add(pinId);
                        }

                        // 3. Unique Active CSRs: Users who shortlisted any request in the period.
                        Object savedByData = doc.get("savedByCsrId");
                        if (savedByData instanceof List) {
                            @SuppressWarnings("unchecked") // Suppress warning, as we've checked the type
                            List<String> csrIdList = (List<String>) savedByData;
                            uniqueCsrUserIds.addAll(csrIdList);
                        }
                    }

                    int uniquePinCount = uniquePinIds.size();
                    int uniqueCsrCount = uniqueCsrUserIds.size();

                    // The callback's parameters are (uniquePins, uniqueCsrs, totalMatches)
                    callback.onReportDataLoaded(uniquePinCount, uniqueCsrCount, totalActiveRequests);

                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Weekly report generation failed", e);
                    String errorMsg = e.getMessage();
                    if (errorMsg.contains("PERMISSION_DENIED")) {
                        callback.onError("Permission Denied. Check user role and Firestore rules.");
                    } else if (errorMsg.contains("requires an index")) {
                        callback.onError("Query failed. A Firestore index is required. Check Logcat for the link.");
                    } else {
                        callback.onError("Query failed: " + errorMsg);
                    }
                });
    }

    public void generateMonthlyReport(int year, int month, final MonthlyReportCallback callback) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar month is 0-indexed

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = new Date(getStartOfDay(cal));

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(getEndOfDay(cal));

        // Task for all requests in the month (to find most requested service)
        Task<QuerySnapshot> allRequestsTask = requestsRef
                .whereGreaterThanOrEqualTo("creationTimestamp", startDate)
                .whereLessThanOrEqualTo("creationTimestamp", endDate)
                .get();

        // Task for completed requests in the month (to find top company)
        Task<QuerySnapshot> completedRequestsTask = requestsRef
                .whereIn("status", Arrays.asList("Completed", "completed"))
                .whereGreaterThanOrEqualTo("creationTimestamp", startDate)
                .whereLessThanOrEqualTo("creationTimestamp", endDate)
                .get();

        Tasks.whenAllSuccess(allRequestsTask, completedRequestsTask).addOnSuccessListener(results -> {
            QuerySnapshot allRequestsSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot completedRequestsSnapshot = (QuerySnapshot) results.get(1);

            // 1. Find Most Requested Service from all requests
            Map<String, Integer> categoryCounts = new HashMap<>();
            for (DocumentSnapshot doc : allRequestsSnapshot.getDocuments()) {
                String category = doc.getString("category");
                if (category != null && !category.isEmpty()) {
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }
            }
            String mostRequestedService = "N/A";
            if (!categoryCounts.isEmpty()) {
                 mostRequestedService = Collections.max(categoryCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

            // 2. Find Top Company from completed requests
            Map<String, Integer> companyCounts = new HashMap<>();
            for (DocumentSnapshot doc : completedRequestsSnapshot.getDocuments()) {
                String companyId = doc.getString("companyId");
                if (companyId != null && !companyId.isEmpty()) {
                    companyCounts.put(companyId, companyCounts.getOrDefault(companyId, 0) + 1);
                }
            }
            String topCompany = "N/A";
            if (!companyCounts.isEmpty()) {
                topCompany = Collections.max(companyCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

            callback.onReportDataLoaded(topCompany, mostRequestedService);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Monthly report generation failed", e);
            callback.onError("Query failed: " + e.getMessage());
        });
    }

    public void migrateUserCreationDate(final MigrationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("Failed to fetch users: " + task.getException().getMessage());
                return;
            }

            List<User> usersToUpdate = new ArrayList<>();
            for (User user : task.getResult().toObjects(User.class)) {
                if (user.getCreationDate() == 0L) { // Check if creationDate is not set
                    usersToUpdate.add(user);
                }
            }

            if (usersToUpdate.isEmpty()) {
                callback.onSuccess("Migration check complete. No users needed updating.");
                return;
            }

            WriteBatch batch = db.batch();
            long migrationTime = System.currentTimeMillis();

            for (User user : usersToUpdate) {
                batch.update(usersRef.document(user.getUid()), "creationDate", migrationTime);
            }

            batch.commit().addOnCompleteListener(batchTask -> {
                if (batchTask.isSuccessful()) {
                    callback.onSuccess("Migration complete. " + usersToUpdate.size() + " users were updated.");
                } else {
                    callback.onError("Migration failed during batch update: " + batchTask.getException().getMessage());
                }
            });
        });
    }

    private long getStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public void listenForCategoryChanges(final CategoryListCallback callback) { /* ... */ }
    public void createCategory(String name, String description, final FirebaseCallback callback) { /* ... */ }
    public void updateCategory(Category category, String newName, String newDescription, final FirebaseCallback callback) { /* ... */ }
    public void deleteCategory(Category category, final FirebaseCallback callback) { /* ... */ }
}
