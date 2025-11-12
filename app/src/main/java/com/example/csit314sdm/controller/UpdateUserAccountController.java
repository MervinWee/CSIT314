package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.User;

import java.util.Map;

public class UpdateUserAccountController {

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void updateUserAccount(String userId, Map<String, Object> updates, UserCallback<Void> callback) {
        // Corrected to call the corresponding method in the User Entity
        User.updateUserAccount(userId, updates, new User.UserCallback<Void>() {
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
}
