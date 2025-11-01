package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Arrays;
import java.util.Date; // <-- Import Date

/**
 * CONTROL (Retrieve): Manages the business logic for fetching a PIN's help requests from Firestore.
 */
public class ViewRequestsController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Constructs a Firestore query to get all help requests submitted by the current user.
     * @return A Query object that the Boundary can listen to for real-time updates.
     */
    public Query getMyHelpRequestsQuery() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();

        // This default query should still sort by date to be useful
        return db.collection("help_requests")
                .whereEqualTo("submittedBy", pinId)
                .orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }

    public Query getFilteredHelpRequestsQuery(String statusFilter) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return null;

        String pinId = currentUser.getUid();
        Query query = db.collection("help_requests").whereEqualTo("submittedBy", pinId);

        if ("History".equalsIgnoreCase(statusFilter)) {
            query = query.whereIn("status", Arrays.asList("Completed", "Cancelled"));
        } else if ("Active".equalsIgnoreCase(statusFilter)) {
            query = query.whereIn("status", Arrays.asList("Open", "In-progress"));
        } else if (statusFilter != null && !statusFilter.isEmpty() && !"All".equalsIgnoreCase(statusFilter)) {
            query = query.whereEqualTo("status", statusFilter);
        }

        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }

    /**
     * --- NEW METHOD ---
     * Builds a complex query for the Match History screen.
     * @param category The service type to filter by. Can be "All".
     * @param fromDate The start of the date range. Can be null.
     * @param toDate The end of the date range. Can be null.
     * @return A Firestore Query object ready to be executed.
     */
    public Query getMatchHistoryQuery(String category, Date fromDate, Date toDate) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();

        // Base query: Always get historical items ("Completed" or "Cancelled") for the current user.
        Query query = db.collection("help_requests")
                .whereEqualTo("submittedBy", pinId)
                .whereIn("status", Arrays.asList("Completed", "Cancelled"));

        // Apply optional category filter
        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = query.whereEqualTo("category", category);
        }

        // Apply optional date filters if they are provided
        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("creationTimestamp", fromDate);
        }
        if (toDate != null) {
            query = query.whereLessThanOrEqualTo("creationTimestamp", toDate);
        }

        // Always sort the results by date
        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }
}
