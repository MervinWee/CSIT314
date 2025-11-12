package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

public class MyCompletedRequestsController {
    public void searchMyCompletedRequests(String csrId, String keyword, HelpRequest.ListCallback callback) {
        // Fix: Pass null for the second String parameter and the two Date parameters
        // to match the required method signature.
        HelpRequest.getCompletedHistory(csrId, null, null, null, keyword, callback);
    }
}
