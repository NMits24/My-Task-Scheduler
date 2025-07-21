package com.example.groupassignment_group8;

public class TaskItem extends ListItem {
    private Task task;

    public TaskItem(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public int getType() {
        return TYPE_TASK;
    }
}
