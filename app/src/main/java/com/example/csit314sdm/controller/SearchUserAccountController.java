package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.User;

import java.util.List;

public class SearchUserAccountController {

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
}
