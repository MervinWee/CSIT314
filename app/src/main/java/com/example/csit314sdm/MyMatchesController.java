package com.example.csit314sdm;

import java.util.List;

public class MyMatchesController {

    public interface MatchedPINsCallback {
        void onMatchedPINsReceived(List<User> pins);
        void onError(String message);
    }

    public void getMatchedPINs(String csrId, MatchedPINsCallback callback) {
        HelpRequest.getMatchesForCsr(new HelpRequest.MyMatchesCallback() {
            @Override
            public void onMatchesLoaded(List<User> matchedUsers) {
                callback.onMatchedPINsReceived(matchedUsers);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
