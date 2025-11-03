package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserProfileController {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "users"; // Use a constant for the collection name

    // --- EXISTING INTERFACES ---
    public interface UsersLoadCallback {
        void onUsersLoaded(List<User> users);
        void onDataLoadFailed(String errorMessage);
    }

    public interface UserLoadCallback {
        void onUserLoaded(User user);
        void onDataLoadFailed(String errorMessage);
    }

    public interface ProfileUpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public interface UserDeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }

    // --- START: ADD THIS NEW INTERFACE ---
    /**
     * Callback interface specifically for the profile creation/saving process.
     */
    public interface ProfileCallback {
        void onProfileSaveSuccess();
        void onProfileSaveFailure(String errorMessage);
    }
    // --- END: ADD THIS NEW INTERFACE ---


    public UserProfileController() {
        db = FirebaseFirestore.getInstance();
    }

    // --- EXISTING METHOD ---
    public void getUserById(String uid, final UserLoadCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onDataLoadFailed("User ID is invalid.");
            return;
        }

        db.collection(COLLECTION_NAME).document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            // FIX: The user object from toObject does not contain the document ID. Set it manually.
                            user.setId(task.getResult().getId());
                            callback.onUserLoaded(user);
                        } else {
                            callback.onDataLoadFailed("User data is corrupt.");
                        }
                    } else {
                        callback.onDataLoadFailed("User not found or failed to load.");
                    }
                });
    }

    // --- START: ADD THIS NEW METHOD ---
    /**
     * Fetches all users from the 'users' collection.
     * This is required by CreateUserProfileActivity to list all users who need a profile.
     * @param callback The callback to invoke on success or failure.
     */
    public void getAllUsersWithProfileCheck(UsersLoadCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            user.setId(document.getId()); // Set the document ID on the user object
                            userList.add(user);
                        }
                        callback.onUsersLoaded(userList);
                    } else {
                        callback.onDataLoadFailed("Failed to fetch users: " + task.getException().getMessage());
                    }
                });
    }
    // --- END: ADD THIS NEW METHOD ---


    // --- START: ADD THIS NEW METHOD ---
    /**
     * Saves the full profile details for a newly selected user.
     * Required by CreateUserProfileActivity.
     * @param user The user object containing the ID to update.
     * @param fullName The full name to save.
     * @param contact The contact number to save.
     * @param dob The date of birth to save.
     * @param address The address to save.
     * @param callback The callback to invoke on success or failure.
     */
    public void saveUserProfile(User user, String fullName, String contact, String dob, String address, final ProfileCallback callback) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            callback.onProfileSaveFailure("Invalid user or user ID provided.");
            return;
        }

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("phoneNumber", contact);
        profileData.put("dateOfBirth", dob);
        profileData.put("address", address);

        db.collection(COLLECTION_NAME).document(user.getId())
                .update(profileData)
                .addOnSuccessListener(aVoid -> callback.onProfileSaveSuccess())
                .addOnFailureListener(e -> callback.onProfileSaveFailure("Failed to save profile: " + e.getMessage()));
    }
    // --- END: ADD THIS NEW METHOD ---


    // --- EXISTING METHOD ---
    public void updateUserProfile(String uid, String newFullName, String newPhoneNumber, String newDob, String newAddress, final ProfileUpdateCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onUpdateFailure("User ID is invalid.");
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("fullName", newFullName);
        updatedData.put("phoneNumber", newPhoneNumber);
        updatedData.put("dob", newDob);
        updatedData.put("address", newAddress);

        db.collection(COLLECTION_NAME).document(uid)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> callback.onUpdateSuccess())
                .addOnFailureListener(e -> callback.onUpdateFailure("Failed to update profile: " + e.getMessage()));
    }

    // --- EXISTING METHOD ---
    public void deleteUserAccount(String uid, final UserDeleteCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onDeleteFailure("User ID is invalid.");
            return;
        }

        db.collection(COLLECTION_NAME).document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onDeleteSuccess())
                .addOnFailureListener(e -> callback.onDeleteFailure("Failed to delete account: " + e.getMessage()));
    }
}
