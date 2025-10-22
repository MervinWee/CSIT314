package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// CONTROL: Manages reading and writing user profile data
public class UserProfileController {

    private final FirebaseFirestore db;

    // --- INTERFACES for Callbacks ---
    public interface UsersLoadCallback {
        void onUsersLoaded(List<User> users);
        void onDataLoadFailed(String errorMessage);
    }

    public interface UserLoadCallback {
        void onUserLoaded(User user);
        void onDataLoadFailed(String errorMessage);
    }

    public interface ProfileCallback {
        void onProfileSaveSuccess();
        void onProfileSaveFailure(String errorMessage);
    }

    public interface ProfileUpdateCallback {
        void onProfileUpdateSuccess();
        void onProfileUpdateFailure(String errorMessage);
    }

    public UserProfileController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches ALL users from Firestore. The client-side will then filter this list.
     * This is the reliable way to find users who need a profile, as it doesn't
     * depend on a specific field existing or being null in the database.
     */
    public void getAllUsersWithProfileCheck(final UsersLoadCallback callback) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            user.setUid(document.getId()); // Manually set UID from document ID
                            userList.add(user);
                        }
                        callback.onUsersLoaded(userList); // Send the full list back
                    } else {
                        callback.onDataLoadFailed("Failed to load users: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Saves the profile fields for a selected user.
     * It uses .update() to add new fields to an existing document.
     */
    public void saveUserProfile(User selectedUser, String fullName, String contact, String dob, String address, final ProfileCallback callback) {
        if (selectedUser == null || selectedUser.getUid() == null) {
            callback.onProfileSaveFailure("Invalid user selected.");
            return;
        }

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("contactNumber", contact);
        profileData.put("dateOfBirth", dob);
        profileData.put("address", address);

        db.collection("users").document(selectedUser.getUid())
                .update(profileData) // .update() is safer than .set() for adding fields
                .addOnSuccessListener(aVoid -> callback.onProfileSaveSuccess())
                .addOnFailureListener(e -> callback.onProfileSaveFailure("Failed to save profile: " + e.getMessage()));
    }

    /**
     * Fetches a single user document by its ID.
     */
    public void getUserById(String uid, final UserLoadCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onDataLoadFailed("User ID is invalid.");
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            user.setUid(task.getResult().getId()); // Ensure UID is set
                            callback.onUserLoaded(user); // Success
                        } else {
                            callback.onDataLoadFailed("User data is corrupt.");
                        }
                    } else {
                        callback.onDataLoadFailed("User not found or failed to load: " + (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    /**
     * Updates specific fields of a user document (e.g., name, contact).
     */
    public void updateUserProfile(String uid, Map<String, Object> updatedData, final ProfileUpdateCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onProfileUpdateFailure("User ID is invalid.");
            return;
        }

        db.collection("users").document(uid)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> callback.onProfileUpdateSuccess())
                .addOnFailureListener(e -> callback.onProfileUpdateFailure("Failed to update profile: " + e.getMessage()));
    }

    /**
     * Updates only the account status field of a user document.
     */
    public void updateUserAccountStatus(String uid, String newStatus, final ProfileUpdateCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onProfileUpdateFailure("User ID is invalid.");
            return;
        }
        if (!newStatus.equals("Active") && !newStatus.equals("Suspended")) {
            callback.onProfileUpdateFailure("Invalid status provided.");
            return;
        }

        db.collection("users").document(uid)
                .update("accountStatus", newStatus)
                .addOnSuccessListener(aVoid -> callback.onProfileUpdateSuccess())
                .addOnFailureListener(e -> callback.onProfileUpdateFailure("Failed to update status: " + e.getMessage()));
    }
}
