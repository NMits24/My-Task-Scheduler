package com.example.groupassignment_group8; // IMPORTANT: Use your actual package name

// This interface allows MainPart to communicate results back to the calling Activity.
public interface UserSyncCallback {
    // You will need a 'User' model class (POJO) if you don't have one yet.
    // It should contain fields like username, email, etc. that match your Firestore document structure.
    void onUserSyncSuccess(User userProfile); // Pass the retrieved/created user object
    void onUserSyncFailure(String errorMessage);
}