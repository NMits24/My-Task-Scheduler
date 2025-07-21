package com.example.groupassignment_group8;

public abstract class ListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK = 1;
    public static final int TYPE_PRIORITY_HEADER = 2; // New type

    abstract public int getType();
}