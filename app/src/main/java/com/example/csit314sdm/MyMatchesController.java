package com.example.csit314sdm;

public class MyMatchesController {

    public void getMatchesForCurrentUser(final HelpRequest.MyMatchesCallback callback) {
        // This controller simply delegates the call to the static method in the HelpRequest entity.
        // The entity handles all the complex database logic.
        HelpRequest.getMatchesForCsr(new HelpRequest.MyMatchesCallback() {
            @Override
            public void onMatchesLoaded(java.util.List<User> matchedUsers) {
                if (callback != null) {
                    callback.onMatchesLoaded(matchedUsers);
                }
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) {
                    callback.onDataLoadFailed(errorMessage);
                }
            }
        });
    }
}
