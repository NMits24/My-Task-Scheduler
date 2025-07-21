package com.example.groupassignment_group8; // IMPORTANT: Use your actual package name
// Or: package com.example.groupassignment_group8.models; if you put it in a sub-package

// This class represents a user's profile data stored in Firestore.
public class User {
    private String email;
    private String username;
    private long createdAt;
    private String avatarUrl; // Example additional field

    // No-argument constructor required for Firestore's .toObject()
    public User() {
    }

    // Constructor for creating new User objects with initial data
    public User(String email, String username, long createdAt) {
        this.email = email;
        this.username = username;
        this.createdAt = createdAt;
        this.avatarUrl = "default_avatar.png"; // Set a default avatar URL
    }

    // --- Getters (Required for Firestore's .toObject()) ---
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // --- Setters (Optional, but good practice if you modify fields in code) ---
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}