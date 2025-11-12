package com.example.csit314sdm;

import java.util.Map;

public class UpdateUserProfileController {

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
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

    // Note: Kept deleteUserAccount here as it's a profile management function.
    public void deleteUserAccount(String userId, User.UserDeleteCallback callback) {
        User.deleteUserAccount(userId, callback);
    }
}
