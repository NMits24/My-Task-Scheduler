package com.example.groupassignment_group8;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, CASE WHEN reminderTime > 0 THEN 0 ELSE 1 END ASC, reminderTime ASC, priority DESC")
    LiveData<List<Task>> getTasksSortedByDate(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, priority DESC, reminderTime ASC")
    LiveData<List<Task>> getTasksSortedByPriority(String userId);
}