package com.raslan.taskmanager.enums;

public enum SortBy {
    CREATED_AT("createdAt"),
    DEADLINE("deadline"),
    PRIORITY("priority");

    private final String field;
    SortBy(String value) {
        this.field = value;
    }

    public String getField() {
        return field;
    }
}
