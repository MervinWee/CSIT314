package com.example.csit314sdm;

public class LoginController {

    public interface LoginCallback {
        void onLoginSuccess(String userRole);
        void onLoginFailure(String errorMessage);
    }

    public void checkForExistingSession(final LoginCallback callback) {
        User.checkForExistingSession(new User.LoginCallback() {
            @Override
            public void onLoginSuccess(String userRole) {
                callback.onLoginSuccess(userRole);
            }

            @Override
            public void onLoginFailure(String errorMessage) {
                callback.onLoginFailure(errorMessage);
            }
        });
    }

    public void loginUser(String email, String password, final LoginCallback callback) {
        User.loginUser(email, password, new User.LoginCallback() {
            @Override
            public void onLoginSuccess(String userRole) {
                callback.onLoginSuccess(userRole);
            }

            @Override
            public void onLoginFailure(String errorMessage) {
                callback.onLoginFailure(errorMessage);
            }
        });
    }

    // ** THE FIX IS HERE **
    // Added the missing logoutUser() method.
    public void logoutUser() {
        // It correctly delegates the call to the User entity.
        User.logoutUser();
    }
}
