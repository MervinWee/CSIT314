package com.example.csit314sdm;

import java.util.List;

public class MyMatchesController {

    public interface MatchedPINsCallback {
        void onMatchedPINsReceived(List<User> pins);
        void onError(String message);
    }

    // Responsible ONLY for retrieving CSR's matched PINs
    public void getMatchedPINs(String csrId, MatchedPINsCallback callback) {
        HelpRequest.getMatchesForCsr(new HelpRequest.MyMatchesCallback() {
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
