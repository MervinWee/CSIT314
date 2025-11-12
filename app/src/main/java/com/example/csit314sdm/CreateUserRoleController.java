package com.example.csit314sdm;

public class CreateUserRoleController {

    public interface CreateUserRoleCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void createUserRole(String role, String roleDescription, String status, final CreateUserRoleCallback callback) {
        User.createUserRole(role, roleDescription, status, new User.UserCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e.getMessage());
            }
        });
    }
}