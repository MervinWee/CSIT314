package com.example.csit314sdm;

import android.text.TextUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class User {

    private String id;
    private String email;
    private String uid;
    private String fullName;
    private String phoneNumber;
    private String dob;
    private String role;
    private String accountStatus;
    private String address;
    private String shortId;
    private Date creationDate;
    private String companyId;

    // Callback for registration
    public interface RegistrationCallback {
        void onRegistrationSuccess(String role);
        void onRegistrationFailure(String errorMessage);
    }
    
    // Callback for unique ID generation
    private interface UniqueIdCallback {
        void onUniqueIdFound(String shortId);
        void onIdGenerationFailed(String errorMessage);
    }

    public User() {}

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

    @PropertyName("dob")
    public String getDob() { return dob; }
    @PropertyName("dob")
    public void setDob(String dob) { this.dob = dob; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getShortId() { return shortId; }
    public void setShortId(String shortId) { this.shortId = shortId; }

    @PropertyName("creationDate")
    public Date getCreationDate() { return creationDate; }
    @PropertyName("creationDate")
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    @PropertyName("companyId")
    public String getCompanyId() { return companyId; }
    @PropertyName("companyId")
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    @PropertyName("createdAt")
    public void setCreatedAt(Date date) {
        if (this.creationDate == null) {
            this.creationDate = date;
        }
    }

    // --- Start of new logic moved from RegistrationController ---

    // for csr mainly
    public static void createUser(String email, String password, String role, String companyId, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }
        createUserWithSecondaryAuth(email, password, role, companyId, callback);
    }

    // For users that do not need a companyID, such as "PIN" or "Admin" users
    public static void createUser(String email, String password, String role, final RegistrationCallback callback) {
        if (!isInputValid(email, password, callback)) {
            return;
        }
        createUserWithSecondaryAuth(email, password, role, null, callback);
    }

    private static void createUserWithSecondaryAuth(String email, String password, String role, String companyId, final RegistrationCallback callback) {
        try {
            FirebaseApp mainApp = FirebaseApp.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String secondaryAppName = "user-creation-instance-" + System.currentTimeMillis();
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
                String email = firebaseUser.getEmail();

                Map<String, Object> newUserMap = new HashMap<>();
                newUserMap.put("email", email);
                newUserMap.put("role", role);
                newUserMap.put("creationDate", FieldValue.serverTimestamp());
                newUserMap.put("accountStatus", "Active");
                newUserMap.put("shortId", shortId);
                newUserMap.put("fullName", "");
                newUserMap.put("dateOfBirth", "");
                newUserMap.put("address", "");
                newUserMap.put("phoneNumber", "");

                if (companyId != null && !companyId.isEmpty()) {
                    newUserMap.put("companyId", companyId);
                }

                db.collection("users").document(uid)
                        .set(newUserMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onRegistrationSuccess(role);
                            } else {
                                callback.onRegistrationFailure("Account created, but failed to save user data: " + task.getException().getMessage());
                            }
                        });
            }

            @Override
            public void onIdGenerationFailed(String errorMessage) {
                callback.onRegistrationFailure(errorMessage);
            }
        });
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

    private static void generateUniqueShortId(FirebaseFirestore db, final UniqueIdCallback callback) {
        int randomId = new Random().nextInt(9000) + 1000;
        String shortId = String.valueOf(randomId);

        db.collection("users").whereEqualTo("shortId", shortId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            callback.onUniqueIdFound(shortId);
                        } else {
                            generateUniqueShortId(db, callback); // Recurse
                        }
                    } else {
                        callback.onIdGenerationFailed("Database error while checking for unique ID.");
                    }
                });
    }
}
