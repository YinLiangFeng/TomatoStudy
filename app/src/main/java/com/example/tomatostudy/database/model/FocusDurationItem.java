package com.example.tomatostudy.database.model;

public class FocusDurationItem {

    private final String taskTitle;
    private final int durationMinutes;

    public FocusDurationItem(String taskTitle, int durationMinutes) {
        this.taskTitle = taskTitle;
        this.durationMinutes = durationMinutes;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
