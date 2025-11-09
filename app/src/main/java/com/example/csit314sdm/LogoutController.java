package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

public class LogoutController {

    private final PlatformDataAccount platformDataAccount;
    private final ListenerRegistration pinRequestListener;
    private final String fcmTopicToUnsubscribe;

    public interface LogoutCallback {
        void onLogoutComplete();
    }

    public LogoutController() {
        this.platformDataAccount = null;
        this.pinRequestListener = null;
        this.fcmTopicToUnsubscribe = null;
    }

    public LogoutController(PlatformDataAccount platformDataAccount) {
        this.platformDataAccount = platformDataAccount;
        this.pinRequestListener = null;
        this.fcmTopicToUnsubscribe = null;
    }

    public LogoutController(ListenerRegistration pinRequestListener) {
        this.platformDataAccount = null;
        this.pinRequestListener = pinRequestListener;
        this.fcmTopicToUnsubscribe = null;
    }

    public LogoutController(String fcmTopicToUnsubscribe) {
        this.platformDataAccount = null;
        this.pinRequestListener = null;
        this.fcmTopicToUnsubscribe = fcmTopicToUnsubscribe;
    }

    public void logout(LogoutCallback callback) {
        // 1. Clean up any active listeners or subscriptions
        if (platformDataAccount != null) {
            platformDataAccount.cleanupAllListeners();
        }
        if (pinRequestListener != null) {
            pinRequestListener.remove();
        }
        if (fcmTopicToUnsubscribe != null && !fcmTopicToUnsubscribe.isEmpty()) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(fcmTopicToUnsubscribe);
        }

        // 2. Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // 3. Notify the view that logout is complete
        if (callback != null) {
            callback.onLogoutComplete();
        }
    }
}
