package com.example.groupassignment_group8;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 13, exportSchema = false) // Version incremented to 13
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "task_database")
                            .fallbackToDestructiveMigration() // This will delete and recreate the DB on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}