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

    // Use LiveData to automatically update the UI when data changes.
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void searchUsers(String searchText, String role) {
        Query query = db.collection("users");

        // 1. Filter by role if a specific role is selected
        if (role != null && !role.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("userType", role);
        }

        // 2. Filter by search text (email)
        // Firestore is limited with text search. For a basic search,
        // we can check for values greater than or equal to the search text
        // and less than the search text + a Unicode character.
        if (searchText != null && !searchText.isEmpty()) {
            query = query.orderBy("email")
                    .whereGreaterThanOrEqualTo("email", searchText)
                    .whereLessThanOrEqualTo("email", searchText + "\uf8ff");
        }

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
                errorLiveData.setValue("Error fetching users: " + task.getException().getMessage());
            }
        });
    }
}
