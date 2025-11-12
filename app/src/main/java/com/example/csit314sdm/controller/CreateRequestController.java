package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.HelpRequest;

import java.util.Collections;

public class CreateRequestController {

    public interface CreateRequestCallback {
        void onRequestCreated(String documentId);
        void onRequestFailed(String errorMessage);
    }

    public void createHelpRequest(String requestType, String description, String location, String region,
                                    String preferredTime, String notes, String urgencyLevel, String pinId,
                                    final CreateRequestCallback callback) {

        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty()) {
            if (callback != null) callback.onRequestFailed("Validation failed. Required fields are missing.");
            return;
        }

        HelpRequest newRequest = new HelpRequest();
        newRequest.setSubmittedBy(pinId);
        newRequest.setPinId(pinId);
        newRequest.setCategory(requestType);
        newRequest.setDescription(description);
        newRequest.setLocation(location);
        newRequest.setRegion(region);
        newRequest.setStatus("Open");
        newRequest.setPreferredTime(preferredTime);
        newRequest.setUrgencyLevel(urgencyLevel);
        newRequest.setTitle(requestType); 
        newRequest.setOrganization(""); 
        newRequest.setSavedByCsrId(Collections.emptyList()); 
        newRequest.setShortlistedDate(null); 
        newRequest.setViewCount(0); 

        HelpRequest.createRequest(newRequest, new HelpRequest.CreateCallback() {
            @Override
            public void onCreateSuccess(String documentId) {
                if (callback != null) callback.onRequestCreated(documentId);
            }

            @Override
            public void onCreateFailure(String errorMessage) {
                if (callback != null) callback.onRequestFailed(errorMessage);
            }
        });
    }
}
