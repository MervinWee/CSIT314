package com.example.csit314sdm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;


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

        Query query = db.collection("users");


        if (role != null && !role.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("userType", role);
        }


        query = query.orderBy("email");


        if (searchText != null && !searchText.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("email", searchText)
                         .whereLessThanOrEqualTo("email", searchText + "\uf8ff");
        }


        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot documents = task.getResult();
                if (documents != null && !documents.isEmpty()) {
                    List<User> userList = documents.toObjects(User.class);
                    usersLiveData.setValue(userList);
                } else {
                    usersLiveData.setValue(new ArrayList<>());
                }
            } else {

                errorLiveData.setValue("Error fetching users: " + task.getException().getMessage());
            }
        });
    }
}
