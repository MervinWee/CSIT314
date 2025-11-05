package com.example.csit314sdm;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class CreateRequestController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance(); // Firebase Authentication instance


    public Task<DocumentReference> createNewRequest(String requestType, String description, String location, String region,
                                                    String preferredTime, String notes, String urgencyLevel, String pinId) {

        System.out.println("CONTROL: Processing new help request...");

        // --- 2. Validate Required Inputs ---
        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty()) {
            System.err.println("CONTROL: Validation failed. Required fields are missing.");
            return null;
        }

        // --- 3. Create a Map of data to save to Firestore ---
        Map<String, Object> newRequestData = new HashMap<>();

        // This links the request to the currently logged-in PIN.
        newRequestData.put("submittedBy", pinId);
        newRequestData.put("pinId", pinId);

        // Add all the other data from the form
        newRequestData.put("category", requestType);
        newRequestData.put("description", description);
        newRequestData.put("location", location);
        newRequestData.put("region", region);
        newRequestData.put("status", "Open");
        newRequestData.put("preferredTime", preferredTime);
        newRequestData.put("urgencyLevel", urgencyLevel);
        newRequestData.put("notes", notes);
        newRequestData.put("creationTimestamp", FieldValue.serverTimestamp());

        // Initialize empty/default fields to match the HelpRequest entity structure
        newRequestData.put("title", requestType);
        newRequestData.put("organization", "");
        newRequestData.put("savedByCsrId", Collections.emptyList());
        newRequestData.put("shortlistedDate", null);
        newRequestData.put("viewCount", 0);


        // --- 4. Save to Firestore ---
        System.out.println("CONTROL: Saving new request to 'help_requests' collection for user: " + pinId);
        return db.collection("help_requests").add(newRequestData);
    }
}
