package com.example.csit314sdm;

public class LogoutController {

    public LogoutController() {}

    public void logoutUser() {
        User.logoutUser();
    }

    public void logoutUser(String fcmTopicToUnsubscribe) {
        User.logoutUser(fcmTopicToUnsubscribe);
    }
}
