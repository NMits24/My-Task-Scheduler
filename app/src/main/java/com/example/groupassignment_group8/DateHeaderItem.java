package com.example.groupassignment_group8;

public class DateHeaderItem extends ListItem {
    private String date;

    public DateHeaderItem(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
