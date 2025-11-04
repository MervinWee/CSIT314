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

    /**
     * Creates and saves a new help request document in Firestore.
     * The 'phoneNumber' parameter has been removed as it is no longer needed.
     */
    public Task<DocumentReference> createNewRequest(String requestType, String description, String location, String region,
                                                    String preferredTime, String notes, String urgencyLevel, String pinId) {

        System.out.println("CONTROL: Processing new help request...");

        // --- 2. Validate Required Inputs ---
        // Note: The main validation is now handled in CreateRequestActivity.
        if (requestType.isEmpty() || description.isEmpty() || location.isEmpty()) {
            System.err.println("CONTROL: Validation failed. Required fields are missing.");
            return null;
        }

        // --- 3. Create a Map of data to save to Firestore ---
        Map<String, Object> newRequestData = new HashMap<>();

        // This links the request to the currently logged-in PIN.
        newRequestData.put("submittedBy", pinId);
        newRequestData.put("pinId", pinId); // Kept for potential legacy compatibility

        // Add all the other data from the form
        newRequestData.put("category", requestType);
        newRequestData.put("description", description);
        newRequestData.put("location", location);
        newRequestData.put("region", region);
        newRequestData.put("status", "Open"); // Set initial status to Open
        newRequestData.put("preferredTime", preferredTime);
        newRequestData.put("urgencyLevel", urgencyLevel);
        newRequestData.put("notes", notes); // Add optional notes
        newRequestData.put("creationTimestamp", FieldValue.serverTimestamp()); // Use server time

        // Initialize empty/default fields to match the HelpRequest entity structure
        newRequestData.put("title", requestType); // Use category as a default title for display
        newRequestData.put("organization", "");
        newRequestData.put("savedByCsrId", Collections.emptyList()); // Corrected field name
        newRequestData.put("shortlistedDate", null);
        newRequestData.put("viewCount", 0);


        // --- 4. Save to Firestore ---
        System.out.println("CONTROL: Saving new request to 'help_requests' collection for user: " + pinId);
        return db.collection("help_requests").add(newRequestData);
    }
}
