package com.example.csit314sdm;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformDataAccount {

    private final DatabaseReference usersRef, requestsRef, matchesRef, categoriesRef;

    // Callbacks
    public interface DailyReportCallback { void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount); void onError(String message); }
    public interface WeeklyReportCallback { void onReportDataLoaded(int uniquePins, int uniqueCsrs, int totalMatches); void onError(String message); }
    public interface MonthlyReportCallback { void onReportDataLoaded(String topCompany, String mostRequestedService); void onError(String message); }
    public interface FirebaseCallback { void onSuccess(); void onError(String message); }
    public interface CategoryListCallback { void onDataLoaded(List<Category> categories); void onError(String message); }

    public PlatformDataAccount() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        requestsRef = database.getReference("HelpRequests");
        matchesRef = database.getReference("Matches");
        categoriesRef = database.getReference("HelpCategories");
    }

    // --- Category Management ---

    public void listenForCategoryChanges(final CategoryListCallback callback) {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    // Automatically map the data to the Category object
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                callback.onDataLoaded(categories);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void createCategory(String name, String description, final FirebaseCallback callback) {
        String key = categoriesRef.push().getKey();
        Category newCategory = new Category(name, description);
        categoriesRef.child(key).setValue(newCategory)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateCategory(Category category, String newName, String newDescription, final FirebaseCallback callback) {
        // Find the category by its current name to get its key
        categoriesRef.orderByChild("name").equalTo(category.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String key = snapshot.getChildren().iterator().next().getKey();
                    Category updatedCategory = new Category(newName, newDescription);
                    categoriesRef.child(key).setValue(updatedCategory)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    callback.onError("Category not found.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
        });
    }

    public void deleteCategory(Category category, final FirebaseCallback callback) {
        categoriesRef.orderByChild("name").equalTo(category.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String key = snapshot.getChildren().iterator().next().getKey();
                    categoriesRef.child(key).removeValue()
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    callback.onError("Category not found.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
        });
    }

    // --- Report Generation Methods (existing code) ---
    public void generateDailyReport(Date date, final DailyReportCallback callback) { /* ... */ }
    public void generateWeeklyReport(Date startDate, Date endDate, final WeeklyReportCallback callback) { /* ... */ }
    public void generateMonthlyReport(int year, int month, final MonthlyReportCallback callback) { /* ... */ }
}
