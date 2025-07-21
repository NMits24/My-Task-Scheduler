package com.example.groupassignment_group8;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey
    @NonNull
    private String firestoreId = "";

    private String title;
    private String description;
    private boolean isCompleted;
    private int priority;
    private String userId;
    private long reminderTime;
    private String soundUri;
    private String appPackageName;
    private String locationName; // New field for location name
    private double latitude = -1;
    private double longitude = -1;

    public Task() {
        // Firestore requires a no-arg constructor
    }

    public Task(String title, String description, boolean isCompleted, int priority, String userId, long reminderTime, String soundUri, String appPackageName, String locationName, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.userId = userId;
        this.reminderTime = reminderTime;
        this.soundUri = soundUri;
        this.appPackageName = appPackageName;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // NEW: Copy constructor for creating a new Task object from an existing one
    public Task(Task other) {
        this.firestoreId = other.firestoreId;
        this.title = other.title;
        this.description = other.description;
        this.isCompleted = other.isCompleted;
        this.priority = other.priority;
        this.userId = other.userId;
        this.reminderTime = other.reminderTime;
        this.soundUri = other.soundUri;
        this.appPackageName = other.appPackageName;
        this.locationName = other.locationName;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
    }

    // Getters and setters for all fields...
    @NonNull
    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(@NonNull String firestoreId) { this.firestoreId = firestoreId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }

    public String getSoundUri() { return soundUri; }
    public void setSoundUri(String soundUri) { this.soundUri = soundUri; }

    public String getAppPackageName() { return appPackageName; }
    public void setAppPackageName(String appPackageName) { this.appPackageName = appPackageName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
