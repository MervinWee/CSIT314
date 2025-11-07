package com.example.csit314sdm;

import android.text.TextUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.PropertyName;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class User {

    // Fields
    private String id;
    private String email;
    private String uid;
    private String fullName;
    private String phoneNumber;
    private Object dob; // Changed to Object
    private String role;
    private String accountStatus;
    private String address;
    private Object shortId; // Changed to Object
    private Date creationDate;
    private String companyId;

    // Callbacks
    public interface RegistrationCallback {
        void onRegistrationSuccess(String role);
        void onRegistrationFailure(String errorMessage);
    }

    public interface LoginCallback {
        void onLoginSuccess(String userRole);
        void onLoginFailure(String errorMessage);
    }
    
    public interface UserDeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }

    public interface UsersLoadCallback {
        void onUsersLoaded(List<User> users);
        void onDataLoadFailed(String errorMessage);
    }

    public interface ProfileCallback {
        void onProfileSaveSuccess();
        void onProfileSaveFailure(String errorMessage);
    }
    
    public interface UserCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    
    private interface UniqueIdCallback {
        void onUniqueIdFound(String shortId);
        void onIdGenerationFailed(String errorMessage);
    }

    // Constructor
    public User() {}

    // Getters and Setters
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @PropertyName("fullName")
    public String getFullName() { return fullName; }
    @PropertyName("fullName")
    public void setFullName(String fullName) { this.fullName = fullName; }

    @PropertyName("phoneNumber")
    public String getPhoneNumber() { return phoneNumber; }
    @PropertyName("phoneNumber")
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    // Smart getter for dob
    public String getDob() {
        if (dob instanceof String) {
            return (String) dob;
        } else if (dob instanceof Timestamp) {
            Date date = ((Timestamp) dob).toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(date);
        }
        return null;
    }
    
    @PropertyName("dob")
    public void setDob(Object dob) { this.dob = dob; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getShortId() {
        if (shortId instanceof String) {
            return (String) shortId;
        } else if (shortId instanceof Number) {
            return String.valueOf(((Number) shortId).longValue());
        }
        return null;
    }

    public void setShortId(Object shortId) {
        this.shortId = shortId;
    }

    @PropertyName("creationDate")
    public Date getCreationDate() { return creationDate; }
    @PropertyName("creationDate")
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    @PropertyName("companyId")
    public String getCompanyId() { return companyId; }
    @PropertyName("companyId")
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    
    @PropertyName("createdAt") // For Firestore's serverTimestamp
    public void setCreatedAt(Date date) {
        if (this.creationDate == null) {
            this.creationDate = date;
        }
    }
    
    // ... (rest of the file is unchanged, all other methods are preserved)
     public static void createUser(String email, String password, String role, String companyId, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }
        createUserWithSecondaryAuth(email, password, role, companyId, callback);
    }
    
    public static void createUser(String email, String password, String role, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }
        createUserWithSecondaryAuth(email, password, role, null, callback);
    }

    private static boolean isInputValid(String email, String password, RegistrationCallback callback) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onRegistrationFailure("Invalid email address.");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            callback.onRegistrationFailure("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    private static void createUserWithSecondaryAuth(String email, String password, String role, String companyId, final RegistrationCallback callback) {
        try {
            FirebaseApp mainApp = FirebaseApp.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String secondaryAppName = "user-creation-" + System.currentTimeMillis();
            FirebaseApp secondaryApp = FirebaseApp.initializeApp(mainApp.getApplicationContext(), mainApp.getOptions(), secondaryAppName);
            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
            secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                saveUserDataToFirestore(db, firebaseUser, role, companyId, callback);
                            } else {
                                callback.onRegistrationFailure("Secondary auth: Failed to get user after creation.");
                            }
                        } else {
                            callback.onRegistrationFailure("Secondary auth: " + task.getException().getMessage());
                        }
                        secondaryApp.delete();
                    });
        } catch (Exception e) {
            callback.onRegistrationFailure("Failed to initialize secondary Firebase app: " + e.getMessage());
        }
    }

    private static void saveUserDataToFirestore(FirebaseFirestore db, FirebaseUser firebaseUser, String role, String companyId, final RegistrationCallback callback) {
        generateUniqueShortId(db, new UniqueIdCallback() {
            @Override
            public void onUniqueIdFound(String shortId) {
                String uid = firebaseUser.getUid();
                Map<String, Object> newUserMap = new HashMap<>();
                newUserMap.put("email", firebaseUser.getEmail());
                newUserMap.put("role", role);
                newUserMap.put("creationDate", FieldValue.serverTimestamp());
                newUserMap.put("accountStatus", "Active");
                newUserMap.put("shortId", shortId);
                newUserMap.put("fullName", "");
                newUserMap.put("phoneNumber", "");
                newUserMap.put("dob", "");
                newUserMap.put("address", "");

                if (companyId != null && !companyId.isEmpty()) {
                    newUserMap.put("companyId", companyId);
                }

                db.collection("users").document(uid)
                        .set(newUserMap)
                        .addOnSuccessListener(aVoid -> callback.onRegistrationSuccess(role))
                        .addOnFailureListener(e -> callback.onRegistrationFailure("Account created, but failed to save user data: " + e.getMessage()));
            }

            @Override
            public void onIdGenerationFailed(String errorMessage) {
                callback.onRegistrationFailure(errorMessage);
            }
        });
    }

    private static void generateUniqueShortId(FirebaseFirestore db, final UniqueIdCallback callback) {
        String shortId = String.valueOf(1000 + new Random().nextInt(9000));
        db.collection("users").whereEqualTo("shortId", shortId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            callback.onUniqueIdFound(shortId);
                        } else {
                            generateUniqueShortId(db, callback);
                        }
                    } else {
                        callback.onIdGenerationFailed("Database error while checking for unique ID.");
                    }
                });
    }

    public static void checkForExistingSession(final LoginCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser.getUid(), mAuth, callback);
        }
    }

    public static void loginUser(String email, String password, final LoginCallback callback) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            callback.onLoginFailure("Email and password are required.");
            return;
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserRole(firebaseUser.getUid(), mAuth, callback);
                        } else {
                            callback.onLoginFailure("Login successful, but failed to get user details.");
                        }
                    } else {
                        callback.onLoginFailure(task.getException().getMessage());
                    }
                });
    }

    private static void fetchUserRole(String uid, FirebaseAuth mAuth, final LoginCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                // ** THE FIX IS HERE **
                                if ("Active".equals(user.getAccountStatus())) {
                                    if (user.getRole() != null) {
                                        callback.onLoginSuccess(user.getRole());
                                    } else {
                                        callback.onLoginFailure("User data is incomplete (role not found).");
                                        mAuth.signOut();
                                    }
                                } else {
                                    // Account is suspended or has another status
                                    callback.onLoginFailure("This account has been suspended.");
                                    mAuth.signOut(); // IMPORTANT: Sign out the user from Firebase Auth
                                }
                            } else {
                                callback.onLoginFailure("Failed to parse user data.");
                                mAuth.signOut();
                            }
                        } else {
                            callback.onLoginFailure("User data not found.");
                            mAuth.signOut(); // User exists in Auth but not in Firestore database
                        }
                    } else {
                        callback.onLoginFailure("Failed to fetch user data.");
                    }
                });
    }

    public static void logoutUser() {
        FirebaseAuth.getInstance().signOut();
    }

    public static void fetchUserById(String userId, UserCallback<User> callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("Failed to parse user data."));
                        }
                    } else {
                        callback.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getAllUsersWithProfileCheck(final UsersLoadCallback callback) {
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            user.setId(document.getId());
                            userList.add(user);
                        }
                        callback.onUsersLoaded(userList);
                    } else {
                        callback.onDataLoadFailed("Failed to fetch users: " + task.getException().getMessage());
                    }
                });
    }
    
    public static void searchUsers(String searchText, String role, UserCallback<List<User>> callback) {
        Query query = FirebaseFirestore.getInstance().collection("users");

        if (role != null && !role.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("role", role);
        }

        query = query.orderBy("email");

        if (searchText != null && !searchText.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("email", searchText)
                    .whereLessThanOrEqualTo("email", searchText + "\uf8ff");
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        users.add(user);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    public static void saveUserProfile(User user, String fullName, String contact, String dob, String address, final ProfileCallback callback) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            callback.onProfileSaveFailure("Invalid user or user ID provided.");
            return;
        }
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("phoneNumber", contact);
        profileData.put("dob", dob);
        profileData.put("address", address);

        FirebaseFirestore.getInstance().collection("users").document(user.getId())
                .update(profileData)
                .addOnSuccessListener(aVoid -> callback.onProfileSaveSuccess())
                .addOnFailureListener(e -> callback.onProfileSaveFailure("Failed to save profile: " + e.getMessage()));
    }

    public static void updateUserProfile(String userId, Map<String, Object> updates, UserCallback<Void> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be empty."));
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
    
    public static void suspendUserProfile(String userId, UserCallback<Void> callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).update("accountStatus", "Suspended")
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public static void reinstateUserProfile(String userId, UserCallback<Void> callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).update("accountStatus", "Active")
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public static void deleteUserAccount(String uid, final UserDeleteCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onDeleteFailure("User ID is invalid.");
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onDeleteSuccess())
                .addOnFailureListener(e -> callback.onDeleteFailure("Failed to delete account: " + e.getMessage()));
    }
}
