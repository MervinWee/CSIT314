package com.example.csit314sdm;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
    private static final String TAG = "PlatformDataAccount";
    private ListenerRegistration categoryListenerRegistration;

    // Callbacks
    public interface DailyReportCallback { void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount); void onError(String message); }
    public interface WeeklyReportCallback { void onReportDataLoaded(int uniquePins, int uniqueCsrs, int totalMatches); void onError(String message); }
    public interface MonthlyReportCallback { void onReportDataLoaded(String topCompany, String mostRequestedService); void onError(String message); }
    public interface FirebaseCallback { void onSuccess(String message); void onError(String message); }
    public interface CategoryListCallback { void onDataLoaded(List<Category> categories); void onError(String message); }
    public interface MigrationCallback { void onSuccess(String message); void onError(String message); }

    public PlatformDataAccount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        requestsRef = db.collection("help_requests");
        // ** THE FIX IS HERE **
        // Changed "HelpCategories" to "categories" to match the security rules.
        categoriesRef = db.collection("categories");
    }

    // --- Report Generation Methods ---

    public void generateDailyReport(Date date, final DailyReportCallback callback) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Date startDate = new Date(getStartOfDay(cal));
        Date endDate = new Date(getEndOfDay(cal));

        Task<QuerySnapshot> usersTask = usersRef.whereGreaterThanOrEqualTo("creationDate", startDate).whereLessThanOrEqualTo("creationDate", endDate).get();
        Task<QuerySnapshot> requestsTask = requestsRef.whereGreaterThanOrEqualTo("creationTimestamp", startDate).whereLessThanOrEqualTo("creationTimestamp", endDate).get();
        Task<QuerySnapshot> matchesTask = requestsRef.whereIn("status", Arrays.asList("Completed", "completed")).whereGreaterThanOrEqualTo("creationTimestamp", startDate).whereLessThanOrEqualTo("creationTimestamp", endDate).get();

        Tasks.whenAllSuccess(usersTask, requestsTask, matchesTask).addOnSuccessListener(results -> {
            int newUserCount = ((QuerySnapshot) results.get(0)).size();
            int newRequestCount = ((QuerySnapshot) results.get(1)).size();
            int completedMatchesCount = ((QuerySnapshot) results.get(2)).size();
            if (callback != null) callback.onReportDataLoaded(newUserCount, newRequestCount, completedMatchesCount);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Daily report generation failed", e);
            if (callback != null) callback.onError("Query failed: " + e.getMessage());
        });
    }

    public void generateWeeklyReport(Date startDate, Date endDate, final WeeklyReportCallback callback) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Date adjustedStartDate = new Date(getStartOfDay(startCal));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        Date adjustedEndDate = new Date(getEndOfDay(endCal));

        requestsRef.whereGreaterThanOrEqualTo("creationTimestamp", adjustedStartDate).whereLessThanOrEqualTo("creationTimestamp", adjustedEndDate).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        if (callback != null) callback.onReportDataLoaded(0, 0, 0);
                        return;
                    }
                    HashSet<String> uniquePinIds = new HashSet<>();
                    HashSet<String> uniqueCsrUserIds = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String pinId = doc.getString("submittedBy");
                        if (pinId != null && !pinId.isEmpty()) uniquePinIds.add(pinId);
                        Object savedByData = doc.get("savedByCsrId");
                        if (savedByData instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> csrIdList = (List<String>) savedByData;
                            uniqueCsrUserIds.addAll(csrIdList);
                        }
                    }
                    if (callback != null) callback.onReportDataLoaded(uniquePinIds.size(), uniqueCsrUserIds.size(), querySnapshot.size());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Weekly report generation failed", e);
                    if (callback != null) callback.onError("Query failed: " + e.getMessage());
                });
    }

    public void generateMonthlyReport(int year, int month, final MonthlyReportCallback callback) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = new Date(getStartOfDay(cal));
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(getEndOfDay(cal));

        Task<QuerySnapshot> allRequestsTask = requestsRef.whereGreaterThanOrEqualTo("creationTimestamp", startDate).whereLessThanOrEqualTo("creationTimestamp", endDate).get();
        Task<QuerySnapshot> completedRequestsTask = requestsRef.whereIn("status", Arrays.asList("Completed", "completed")).whereGreaterThanOrEqualTo("creationTimestamp", startDate).whereLessThanOrEqualTo("creationTimestamp", endDate).get();

        Tasks.whenAllSuccess(allRequestsTask, completedRequestsTask).addOnSuccessListener(results -> {
            QuerySnapshot allRequestsSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot completedRequestsSnapshot = (QuerySnapshot) results.get(1);

            Map<String, Integer> categoryCounts = new HashMap<>();
            for (DocumentSnapshot doc : allRequestsSnapshot.getDocuments()) {
                String category = doc.getString("category");
                if (category != null && !category.isEmpty()) categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            }
            String mostRequestedService = "N/A";
            if (!categoryCounts.isEmpty()) mostRequestedService = Collections.max(categoryCounts.entrySet(), Map.Entry.comparingByValue()).getKey();

            Map<String, Integer> companyCounts = new HashMap<>();
            for (DocumentSnapshot doc : completedRequestsSnapshot.getDocuments()) {
                String companyId = doc.getString("companyId");
                if (companyId != null && !companyId.isEmpty()) companyCounts.put(companyId, companyCounts.getOrDefault(companyId, 0) + 1);
            }
            String topCompany = "N/A";
            if (!companyCounts.isEmpty()) topCompany = Collections.max(companyCounts.entrySet(), Map.Entry.comparingByValue()).getKey();

            if (callback != null) callback.onReportDataLoaded(topCompany, mostRequestedService);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Monthly report generation failed", e);
            if (callback != null) callback.onError("Query failed: " + e.getMessage());
        });
    }

    public void migrateUserCreationDate(final MigrationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                if (callback != null) callback.onError("Failed to fetch users: " + task.getException().getMessage());
                return;
            }
            List<User> usersToUpdate = new ArrayList<>();

            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    user.setId(document.getId());
                    if (user.getCreationDate() == null) {
                        usersToUpdate.add(user);
                    }
                }
            }

            if (usersToUpdate.isEmpty()) {
                if (callback != null) callback.onSuccess("Migration check complete. No users needed updating.");
                return;
            }

            WriteBatch batch = db.batch();
            Date migrationTime = new Date();
            for (User user : usersToUpdate) {

                if (user.getId() != null && !user.getId().isEmpty()) {
                    batch.update(usersRef.document(user.getId()), "creationDate", migrationTime);
                }
            }

            batch.commit().addOnCompleteListener(batchTask -> {
                if (batchTask.isSuccessful()) {
                    if (callback != null) callback.onSuccess("Migration complete. " + usersToUpdate.size() + " users were updated.");
                } else {
                    if (callback != null) callback.onError("Migration failed during batch update: " + batchTask.getException().getMessage());
                }
            });
        });
    }


    public void listenForCategoryChanges(final CategoryListCallback callback) {
        if (categoryListenerRegistration != null) categoryListenerRegistration.remove();
        categoryListenerRegistration = categoriesRef.orderBy("name").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Category listen failed.", e);
                if (callback != null) callback.onError(e.getMessage());
                return;
            }
            if (snapshots != null) {
                List<Category> categories = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Category category = doc.toObject(Category.class);
                    if (category != null) {
                        category.setId(doc.getId());
                        categories.add(category);
                    }
                }
                if (callback != null) callback.onDataLoaded(categories);
            }
        });
    }

    public void detachCategoryListener() {
        if (categoryListenerRegistration != null) {
            categoryListenerRegistration.remove();
            categoryListenerRegistration = null;
            Log.d(TAG, "Category listener detached.");
        }
    }

    public void cleanupAllListeners() {
        detachCategoryListener();
        Log.d(TAG, "All platform listeners have been cleaned up.");
    }

    public void createCategory(String name, String description, final FirebaseCallback callback) {
        categoriesRef.whereEqualTo("name", name).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                if (callback != null) callback.onError("A category with this name already exists.");
            } else {
                Category newCategory = new Category(name, description);
                categoriesRef.add(newCategory).addOnSuccessListener(documentReference -> {
                    if (callback != null) callback.onSuccess("Category created successfully.");
                }).addOnFailureListener(e -> {
                    if (callback != null) callback.onError("Failed to create category: " + e.getMessage());
                });
            }
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError("Failed to check for existing category: " + e.getMessage());
        });
    }

    public void updateCategory(Category category, String newName, String newDescription, final FirebaseCallback callback) {
        if (category.getId() == null || category.getId().isEmpty()) {
            if (callback != null) callback.onError("Category ID is missing. Cannot update.");
            return;
        }
        Category updatedCategory = new Category(newName, newDescription);
        updatedCategory.setId(category.getId());
        categoriesRef.document(category.getId()).set(updatedCategory).addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess("Category updated successfully.");
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError("Failed to update category: " + e.getMessage());
        });
    }

    public void deleteCategory(Category category, final FirebaseCallback callback) {
        if (category.getId() == null || category.getId().isEmpty()) {
            if (callback != null) callback.onError("Category ID is missing. Cannot delete.");
            return;
        }
        categoriesRef.document(category.getId()).delete().addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess("Category deleted successfully.");
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError("Failed to delete category: " + e.getMessage());
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
}
