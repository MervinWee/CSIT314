package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Arrays;
import java.util.Date; // <-- Import Date


public class ViewRequestsController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();


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


    public Query getMatchHistoryQuery(String category, Date fromDate, Date toDate) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return null;
        String pinId = currentUser.getUid();


        Query query = db.collection("help_requests")
                .whereEqualTo("submittedBy", pinId)
                .whereIn("status", Arrays.asList("Completed", "Cancelled"));


        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            query = query.whereEqualTo("category", category);
        }


        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("creationTimestamp", fromDate);
        }
        if (toDate != null) {
            query = query.whereLessThanOrEqualTo("creationTimestamp", toDate);
        }


        return query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
    }
}
