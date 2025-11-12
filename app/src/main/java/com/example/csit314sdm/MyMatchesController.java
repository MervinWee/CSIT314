package com.example.csit314sdm;

import java.util.List;

public class MyMatchesController {

    public interface MatchedPINsCallback {
        void onMatchedPINsReceived(List<User> pins);
        void onError(String message);
    }

    public void getMatchedPINs(String csrId, MatchedPINsCallback callback) {
        HelpRequestEntity.getMatchesForCsr(csrId, new HelpRequestEntity.MyMatchesCallback() {
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