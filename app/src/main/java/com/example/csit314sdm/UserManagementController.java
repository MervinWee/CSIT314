package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Map;

public class UserManagementController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String USERS_COLLECTION = "users";

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // --- NEW: Search and Filter Method ---
    public void searchUsers(String searchText, String role, UserCallback<List<User>> callback) {
        Query query = db.collection(USERS_COLLECTION);

        // Apply role filter if a specific role is selected
        if (role != null && !role.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("role", role);
        }

        // Always sort by email for consistent ordering
        query = query.orderBy("email");

        // Apply search text filter for email
        if (searchText != null && !searchText.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("email", searchText)
                     .whereLessThanOrEqualTo("email", searchText + "\uf8ff");
        }

        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<User> users = queryDocumentSnapshots.toObjects(User.class);
                // Manually set the UID for each user, as toObjects doesn't do it automatically
                for (int i = 0; i < users.size(); i++) {
                    users.get(i).setUid(queryDocumentSnapshots.getDocuments().get(i).getId());
                }
                callback.onSuccess(users);
            })
            .addOnFailureListener(callback::onFailure);
    }


    // --- Existing Methods (Unchanged) ---

    public void fetchAllUsers(UserCallback<List<User>> callback) {
        db.collection(USERS_COLLECTION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = queryDocumentSnapshots.toObjects(User.class);
                    for (int i = 0; i < users.size(); i++) {
                        users.get(i).setUid(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void fetchUserById(String userId, UserCallback<User> callback) {
        db.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        user.setUid(documentSnapshot.getId());
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserProfile(String userId, Map<String, Object> updates, UserCallback<Void> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be empty."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId).update(updates)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void suspendUserProfile(String userId, UserCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(userId).update("accountStatus", "Suspended")
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void reinstateUserProfile(String userId, UserCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(userId).update("accountStatus", "Active")
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }
}
