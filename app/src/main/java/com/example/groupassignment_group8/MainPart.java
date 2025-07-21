package com.example.groupassignment_group8; // IMPORTANT: Use your actual package name

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// This is a helper class responsible for handling Firestore user profile data.
// It does NOT have any UI or Android Activity lifecycle.
public class MainPart {

    private static final String TAG = "MainPartHelper";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Constructor to initialize FirebaseFirestore instance
    public MainPart() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Fetches the user's profile from Firestore or creates a new one if it doesn't exist.
     * Uses a callback to notify the calling component of success or failure.
     *
     * @param userId The UID of the currently authenticated Firebase user.
     * @param callback The callback interface to send results back to the calling Activity/Fragment.
     */
    public void fetchOrCreateUserData(String userId, UserSyncCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onUserSyncFailure("User ID is null or empty.");
            return;
        }

        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Data exists: User's profile is found.
                        Log.d(TAG, "User data retrieved from Firestore for UID: " + userId);
                        User userProfile = documentSnapshot.toObject(User.class); // Convert to your User model
                        if (userProfile != null) {
                            callback.onUserSyncSuccess(userProfile);
                        } else {
                            // This case can happen if the document exists but data mapping fails
                            callback.onUserSyncFailure("Failed to parse user profile data. Document might be empty or malformed.");
                        }

                    } else {
                        // User data does NOT exist: Create a new profile.
                        Log.d(TAG, "No user data found for UID: " + userId + ". Creating new entry...");
                        createNewUserFirestoreProfile(userId, userDocRef, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data for UID: " + userId, e);
                    callback.onUserSyncFailure("Error fetching user data: " + e.getMessage());
                });
    }

    /**
     * Creates a new user profile document in Firestore with initial data.
     * This is called internally by fetchOrCreateUserData if the profile doesn't exist.
     */
    private void createNewUserFirestoreProfile(String userId, DocumentReference userDocRef, UserSyncCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String email = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "unknown@example.com";
        // Attempt to get display name from Google/Email auth, otherwise generate a default
        String username = (currentUser != null && currentUser.getDisplayName() != null) ? currentUser.getDisplayName() : "User_" + userId.substring(0, 5);

        // Create a new User object using your User model
        User newUser = new User(email, username, System.currentTimeMillis());
        // You can set additional default fields here if needed:
        // newUser.setSomeDefaultPreference(true);

        userDocRef.set(newUser) // Use .set() to create the document
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user profile created successfully for UID: " + userId);
                    callback.onUserSyncSuccess(newUser); // Notify success with the newly created object
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating new user profile for UID: " + userId, e);
                    callback.onUserSyncFailure("Error creating user profile: " + e.getMessage());
                });
    }

    /**
     * Example method to update a user's data (e.g., username).
     * This is just one example; you'd have similar methods for other updates.
     *
     * @param userId The UID of the user whose data is to be updated.
     * @param updates A Map of fields and their new values.
     * @param callback Optional callback for update success/failure.
     */
    public void updateUserData(String userId, Map<String, Object> updates, @Nullable UserSyncCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onUserSyncFailure("User ID is null or empty for update.");
            return;
        }

        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data updated for UID: " + userId + ". Fields: " + updates.keySet());
                    if (callback != null) {
                        // For a simple update, we might just call success with a null or a confirmation message
                        // For complex updates, you might re-fetch the User object and return it.
                        callback.onUserSyncSuccess(null); // Indicates update success, no new user object returned
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user data for UID: " + userId, e);
                    if (callback != null) callback.onUserSyncFailure("Error updating data: " + e.getMessage());
                });
    }
}