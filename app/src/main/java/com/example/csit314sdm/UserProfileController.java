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

    // Callback for loading a list of users
    public interface UsersLoadCallback {
        void onUsersLoaded(List<User> users);
        void onDataLoadFailed(String errorMessage);
    }

    // Callback for saving a single profile
    public interface ProfileCallback {
        void onProfileSaveSuccess();
        void onProfileSaveFailure(String errorMessage);
    }

    public UserProfileController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches all users with the userType "PIN".
     * This is a single-fetch operation using .get() and is the correct way
     * to populate the "Create User Profile" dropdown.
     */
    public void getUsersWithoutProfiles(final UsersLoadCallback callback) {
        db.collection("users")
                .whereEqualTo("userType", "PIN")
                .get() // Use .get() for a one-time read
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> userList = new ArrayList<>();
                        // The result is a list of documents
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert each document into a User object
                            User user = document.toObject(User.class);
                            userList.add(user);
                        }
                        // Return the fully populated list
                        callback.onUsersLoaded(userList);
                    } else {
                        // If the .get() task fails, return the error
                        callback.onDataLoadFailed("Error loading users: " + task.getException().getMessage());
                    }
                });
    }

    // Add this method to UserProfileController.java

    public void getAllUsers(final UsersLoadCallback callback) {
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userList.add(user);
                        }
                        callback.onUsersLoaded(userList);
                    } else {
                        callback.onDataLoadFailed("Failed to load all users: " + task.getException().getMessage());
                    }
                });
    }

    // Add this interface and method to UserProfileController.java

    // Callback for loading a SINGLE user
    public interface UserLoadCallback {
        void onUserLoaded(User user);
        void onDataLoadFailed(String errorMessage);
    }// Method to fetch a single user document by its ID

    // Callback for updating a profile
    public interface ProfileUpdateCallback {
        void onProfileUpdateSuccess();
        void onProfileUpdateFailure(String errorMessage);
    }

    // Method to update specific fields of a user document
    public void updateUserProfile(String uid, Map<String, Object> updatedData, final ProfileUpdateCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onProfileUpdateFailure("User ID is invalid.");
            return;
        }

        db.collection("users").document(uid)
                .update(updatedData) // .update() merges the new data without overwriting the whole document
                .addOnSuccessListener(aVoid -> callback.onProfileUpdateSuccess())
                .addOnFailureListener(e -> callback.onProfileUpdateFailure("Failed to update profile: " + e.getMessage()));
    }
    public void getUserById(String uid, final UserLoadCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onDataLoadFailed("User ID is invalid.");
            return;
        }

        db.collection("users").document(uid)
                .get() // Get a single document
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            callback.onUserLoaded(user); // Success
                        } else {
                            callback.onDataLoadFailed("User not found.");
                        }
                    } else {
                        callback.onDataLoadFailed("Failed to load user: " + task.getException().getMessage());
                    }
                });
    }

// In UserProfileController.java

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
                .update("accountStatus", newStatus) // Efficiently update only one field
                .addOnSuccessListener(aVoid -> callback.onProfileUpdateSuccess())
                .addOnFailureListener(e -> callback.onProfileUpdateFailure("Failed to update status: " + e.getMessage()));
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

        // Use a Map to update only the profile fields
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
}
