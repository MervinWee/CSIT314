package com.example.csit314sdm;

public class SuspendUserProfileController {

    // The controller's own callback interface
    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // The method now correctly accepts its own callback type
    public void suspendUserProfile(String userId, UserCallback<Void> callback) {
        // Internally, it uses the User entity's callback to communicate with the database
        User.suspendUserProfile(userId, new User.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // On success, it triggers the callback that was passed in from the Boundary
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // The method now correctly accepts its own callback type
    public void reinstateUserProfile(String userId, UserCallback<Void> callback) {
        // Internally, it uses the User entity's callback to communicate with the database
        User.reinstateUserProfile(userId, new User.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // On success, it triggers the callback that was passed in from the Boundary
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
