package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;
import com.example.csit314sdm.entity.User;

import java.util.List;

public class MyMatchesController {

    public interface MatchedPINsCallback {
        void onMatchedPINsReceived(List<User> pins);
        void onError(String message);
    }

    public void getMatchedPINs(String csrId, MatchedPINsCallback callback) {
        HelpRequest.getMatchesForCsr(csrId, new HelpRequest.MyMatchesCallback() {
            @Override
            public void onMatchesLoaded(List<User> matchedUsers) {
                if (callback != null) {
                    callback.onMatchedPINsReceived(matchedUsers);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }
}