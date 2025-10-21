package com.example.csit314sdm;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// CONTROL: Handles logic for creating and managing user profiles.
public class UserProfileController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ProfileCallback {
        void onProfileSaveSuccess();
        void onProfileSaveFailure(String errorMessage);
    }

    public interface UsersLoadCallback {
        void onUsersLoaded(List<User> users);
        void onDataLoadFailed(String errorMessage);
    }

    // Fetches all users who do not have a 'fullName' field (i.e., no profile yet)
    public void getUsersWithoutProfiles(UsersLoadCallback callback) {
        db.collection("users")
                .whereEqualTo("userType", "PIN")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userList.add(user);
                        }
                        callback.onUsersLoaded(userList);
                    } else {
                        callback.onDataLoadFailed("Error loading users: " + task.getException().getMessage());
                    }
                });
    }

    // Saves the new profile data to an existing user document
    public void saveUserProfile(User selectedUser, String fullName, String contact, String dob, String address, ProfileCallback callback) {
        if (selectedUser == null || selectedUser.getUid() == null) {
            callback.onProfileSaveFailure("No user selected.");
            return;
        }

        Map<String, Object> profileData = new java.util.HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("contactNumber", contact);
        profileData.put("dateOfBirth", dob);
        profileData.put("address", address);

        db.collection("users").document(selectedUser.getUid())
                .update(profileData)
                .addOnSuccessListener(aVoid -> callback.onProfileSaveSuccess())
                .addOnFailureListener(e -> callback.onProfileSaveFailure("Failed to save profile: " + e.getMessage()));
    }
}
