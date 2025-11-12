package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.User;

public class SuspendUserAccountController {

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void suspendUserAccount(String userId, UserCallback<Void> callback) {
        // CORRECTED: Now calls the corresponding method in the User Entity
        User.suspendUserAccount(userId, new User.UserCallback<Void>() {
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

    public void reinstateUserAccount(String userId, UserCallback<Void> callback) {
        // CORRECTED: Now calls the corresponding method in the User Entity
        User.reinstateUserAccount(userId, new User.UserCallback<Void>() {
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
