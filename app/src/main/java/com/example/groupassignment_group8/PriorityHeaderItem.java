package com.example.groupassignment_group8;

public class PriorityHeaderItem extends ListItem {
    private String priority;

    public PriorityHeaderItem(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }

    @Override
    public int getType() {
        return TYPE_PRIORITY_HEADER;
    }
}