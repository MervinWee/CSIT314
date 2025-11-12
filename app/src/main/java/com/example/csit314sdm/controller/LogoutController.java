package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.User;

public class LogoutController {

    public LogoutController() {}

    public void logoutUser() {
        User.logoutUser();
    }

    public void logoutUser(String fcmTopicToUnsubscribe) {
        User.logoutUser(fcmTopicToUnsubscribe);
    }
}
