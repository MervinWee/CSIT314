package com.example.csit314sdm;

import java.util.List;
import java.util.Map;

public class UserManagementController {

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void searchUsers(String searchText, String role, UserCallback<List<User>> callback) {
        User.searchUsers(searchText, role, new User.UserCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void fetchUserById(String userId, UserCallback<User> callback) {
        User.fetchUserById(userId, new User.UserCallback<User>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void updateUserProfile(String userId, Map<String, Object> updates, UserCallback<Void> callback) {
        User.updateUserProfile(userId, updates, new User.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void suspendUserProfile(String userId, UserCallback<Void> callback) {
        User.suspendUserProfile(userId, new User.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void reinstateUserProfile(String userId, UserCallback<Void> callback) {
        User.reinstateUserProfile(userId, new User.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void deleteUserAccount(String userId, User.UserDeleteCallback callback) {
        User.deleteUserAccount(userId, callback);
    }
}
