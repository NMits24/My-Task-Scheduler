package com.example.groupassignment_group8;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private final TaskDao taskDao;
    private final FirebaseFirestore db;
    private final String userId;
    private final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        taskDao = database.taskDao();
        db = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        syncTasksFromFirestore();
    }

    private void syncTasksFromFirestore() {
        db.collection("tasks").whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("TaskRepository", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Task task = dc.getDocument().toObject(Task.class);
                            task.setFirestoreId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    databaseWriteExecutor.execute(() -> taskDao.insert(task));
                                    break;
                                case REMOVED:
                                    databaseWriteExecutor.execute(() -> taskDao.delete(task));
                                    break;
                            }
                        }
                    }
                });
    }

    public LiveData<List<Task>> getTasksSortedByDate() {
        return taskDao.getTasksSortedByDate(userId);
    }

    public LiveData<List<Task>> getTasksSortedByPriority() {
        return taskDao.getTasksSortedByPriority(userId);
    }

    public void insert(Task task) {
        String firestoreId = db.collection("tasks").document().getId();
        task.setFirestoreId(firestoreId);

        db.collection("tasks").document(firestoreId).set(task)
                .addOnSuccessListener(aVoid -> databaseWriteExecutor.execute(() -> taskDao.insert(task)))
                .addOnFailureListener(e -> Log.w("TaskRepository", "Error adding document", e));
    }

    public void update(Task task) {
        if (task.getFirestoreId() != null) {
            db.collection("tasks").document(task.getFirestoreId()).set(task)
                    .addOnSuccessListener(aVoid -> databaseWriteExecutor.execute(() -> taskDao.update(task)))
                    .addOnFailureListener(e -> Log.w("TaskRepository", "Error updating document", e));
        }
    }

    public void delete(Task task) {
        if (task.getFirestoreId() != null) {
            db.collection("tasks").document(task.getFirestoreId()).delete()
                    .addOnSuccessListener(aVoid -> databaseWriteExecutor.execute(() -> taskDao.delete(task)))
                    .addOnFailureListener(e -> Log.w("TaskRepository", "Error deleting document", e));
        }
    }
}
