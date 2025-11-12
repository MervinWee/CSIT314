package com.example.csit314sdm;

public class RetrieveUserProfileController {

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
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
