package com.example.csit314sdm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

// CONTROL: Handles the business logic for searching users in Firestore.
public class SearchUserController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void searchUsers(String searchText, String role) {
        // Start with the base query on the "users" collection.
        Query query = db.collection("users");

        // --- FIX #2: Apply Role Filter --- 
        // If a specific role is selected (i.e., not "All"), add a filter.
        if (role != null && !role.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("userType", role);
        }

        // --- FIX #1: Always Sort by Email ---
        // This ensures the list is always in alphabetical order.
        query = query.orderBy("email");

        // --- Apply Search Text Filter ---
        // If the user has typed something, add a range filter to act as a "starts with" search.
        if (searchText != null && !searchText.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("email", searchText)
                         .whereLessThanOrEqualTo("email", searchText + "\uf8ff");
        }

        // Execute the final query.
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot documents = task.getResult();
                if (documents != null && !documents.isEmpty()) {
                    List<User> userList = documents.toObjects(User.class);
                    usersLiveData.setValue(userList);
                } else {
                    usersLiveData.setValue(new ArrayList<>()); // Post empty list for "No results"
                }
            } else {
                // If the query fails, post the error. This is often due to a missing Firestore index.
                errorLiveData.setValue("Error fetching users: " + task.getException().getMessage());
            }
        });
    }
}
