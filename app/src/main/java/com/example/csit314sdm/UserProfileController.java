package com.example.csit314sdm;

import java.util.List;

public class UserProfileController {

    public interface UsersLoadCallback {
        void onUsersLoaded(List<User> users);
        void onDataLoadFailed(String errorMessage);
    }

    public interface ProfileCallback {
        void onProfileSaveSuccess();
        void onProfileSaveFailure(String errorMessage);
    }

    public void getAllUsersWithProfileCheck(final UsersLoadCallback callback) {
        User.getAllUsersWithProfileCheck(new User.UsersLoadCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                callback.onUsersLoaded(users);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                callback.onDataLoadFailed(errorMessage);
            }
        });
    }
    public void getUserById(String userId, User.UserCallback<User> callback) {
        // Delegate the call directly to the static method in the User class
        User.fetchUserById(userId, callback);
    }

    public void saveUserProfile(User user, String fullName, String contact, String dob, String address, final ProfileCallback callback) {
        User.saveUserProfile(user, fullName, contact, dob, address, new User.ProfileCallback() {
            @Override
            public void onProfileSaveSuccess() {
                callback.onProfileSaveSuccess();
            }

            @Override
            public void onProfileSaveFailure(String errorMessage) {
                callback.onProfileSaveFailure(errorMessage);
            }
        });
    }
}
