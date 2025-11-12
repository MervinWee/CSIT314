package com.example.csit314sdm;

public class SuspendUserAccountController {

    // The controller's own callback interface
    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // *** CORRECTLY RENAMED METHOD ***
    public void suspendUserAccount(String userId, UserCallback<Void> callback) {
        // Internally, it still calls the entity's method, which can keep its original name
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

    // *** CORRECTLY RENAMED METHOD ***
    public void reinstateUserAccount(String userId, UserCallback<Void> callback) {
        // Internally, it still calls the entity's method, which can keep its original name
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
}
