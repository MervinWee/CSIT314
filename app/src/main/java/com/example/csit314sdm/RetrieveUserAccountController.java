package com.example.csit314sdm;

import java.util.List;

public class RetrieveUserAccountController {

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // Method added back to resolve the error
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
}
