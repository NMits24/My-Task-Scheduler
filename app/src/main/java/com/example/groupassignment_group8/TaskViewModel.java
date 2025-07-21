package com.example.groupassignment_group8;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<ListItem>> tasksWithDateHeaders;
    private LiveData<List<ListItem>> tasksWithPriorityHeaders;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            repository = new TaskRepository(application);

            LiveData<List<Task>> tasksSortedByDate = repository.getTasksSortedByDate();
            tasksWithDateHeaders = Transformations.map(tasksSortedByDate, this::addDateHeaders);

            LiveData<List<Task>> tasksSortedByPriority = repository.getTasksSortedByPriority();
            tasksWithPriorityHeaders = Transformations.map(tasksSortedByPriority, this::addPriorityHeaders);
        }
    }

    public LiveData<List<ListItem>> getTasksWithDateHeaders() {
        return tasksWithDateHeaders;
    }

    public LiveData<List<ListItem>> getTasksWithPriorityHeaders() {
        return tasksWithPriorityHeaders;
    }

    private List<ListItem> addDateHeaders(List<Task> tasks) {
        List<ListItem> itemsWithHeaders = new ArrayList<>();
        if (tasks == null) return itemsWithHeaders;

        String lastHeader = "";
        for (Task task : tasks) {
            String header = getHeaderForTask(task);
            if (!header.equals(lastHeader)) {
                itemsWithHeaders.add(new DateHeaderItem(header));
                lastHeader = header;
            }
            itemsWithHeaders.add(new TaskItem(task));
        }
        return itemsWithHeaders;
    }

    private List<ListItem> addPriorityHeaders(List<Task> tasks) {
        List<ListItem> itemsWithHeaders = new ArrayList<>();
        if (tasks == null) return itemsWithHeaders;

        int lastPriority = -1;
        for (Task task : tasks) {
            if (task.getPriority() != lastPriority) {
                itemsWithHeaders.add(new PriorityHeaderItem(getPriorityHeader(task.getPriority())));
                lastPriority = task.getPriority();
            }
            itemsWithHeaders.add(new TaskItem(task));
        }
        return itemsWithHeaders;
    }

    private String getPriorityHeader(int priority) {
        switch (priority) {
            case 3: return "High Priority";
            case 2: return "Medium Priority";
            default: return "Low Priority";
        }
    }

    private String getHeaderForTask(Task task) {
        if (task.getReminderTime() <= 0) {
            return "No Date";
        }

        Calendar taskDate = Calendar.getInstance();
        taskDate.setTimeInMillis(task.getReminderTime());

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        if (taskDate.before(today) && !isSameDay(taskDate, today)) {
            return "Overdue";
        } else if (isSameDay(taskDate, today)) {
            return "Today";
        } else if (isSameDay(taskDate, tomorrow)) {
            return "Tomorrow";
        } else {
            return "Upcoming";
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public void insert(Task task) { repository.insert(task); }
    public void update(Task task) { repository.update(task); }
    public void delete(Task task) { repository.delete(task); }
}
