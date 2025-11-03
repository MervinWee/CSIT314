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

/**
 * CONTROL (Create): Manages the business logic for creating and saving a new help request.
 */
public class CreateRequestController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance(); // Firebase Authentication instance

    public Task<DocumentReference> createNewRequest(String requestType, String description, String location,
                                                    String preferredTime, String phoneNumber, String notes, String urgencyLevel, String pinId) {

        System.out.println("CONTROL: Processing new help request...");

        // --- 2. Validate Required Inputs ---
        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty()) {
            System.err.println("CONTROL: Validation failed. Required fields are missing.");
            return null;
        }

        // --- 3. Create a Map of data to save to Firestore ---
        Map<String, Object> newRequestData = new HashMap<>();

        // This links the request to the PIN.
        newRequestData.put("submittedBy", pinId);
        newRequestData.put("pinId", pinId);

        // Add all the other data from the form
        newRequestData.put("category", requestType);
        newRequestData.put("description", description);
        newRequestData.put("location", location);
        newRequestData.put("status", "Open");
        newRequestData.put("preferredTime", preferredTime);

        // --- THIS IS THE FIX ---
        // The key used here MUST exactly match the variable name in the HelpRequest.java class.
        newRequestData.put("urgencyLevel", urgencyLevel);

        // The date field is already correct.
        newRequestData.put("creationTimestamp", FieldValue.serverTimestamp());

        // Add optional fields
        newRequestData.put("phoneNumber", phoneNumber);
        newRequestData.put("notes", notes);

        // Initialize empty/default fields to match the HelpRequest entity structure
        newRequestData.put("title", requestType); // Use category as a default title
        newRequestData.put("organization", "");
        newRequestData.put("savedBy", Collections.emptyList());
        newRequestData.put("shortlistedDate", null);


        // --- 4. Save to Firestore ---
        System.out.println("CONTROL: Saving new request to 'help_requests' collection for user: " + pinId);
        return db.collection("help_requests").add(newRequestData);
    }
}
