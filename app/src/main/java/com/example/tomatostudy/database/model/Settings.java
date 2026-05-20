package com.example.tomatostudy.database.model;

public class Settings {

    private int id;
    private int userId;
    private int defaultFocusMinutes = 25;
    private int defaultRestMinutes = 5;
    private int longRestMinutes = 15;
    private int longRestInterval = 4;
    private boolean taskReminderEnabled = true;
    private boolean focusEndReminderEnabled = true;
    private boolean checkInReminderEnabled = true;
    private String dailyReminderTime = "20:00";

    public Settings() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDefaultFocusMinutes() {
        return defaultFocusMinutes;
    }

    public void setDefaultFocusMinutes(int defaultFocusMinutes) {
        this.defaultFocusMinutes = defaultFocusMinutes;
    }

    public int getDefaultRestMinutes() {
        return defaultRestMinutes;
    }

    public void setDefaultRestMinutes(int defaultRestMinutes) {
        this.defaultRestMinutes = defaultRestMinutes;
    }

    public int getLongRestMinutes() {
        return longRestMinutes;
    }

    public void setLongRestMinutes(int longRestMinutes) {
        this.longRestMinutes = longRestMinutes;
    }

    public int getLongRestInterval() {
        return longRestInterval;
    }

    public void setLongRestInterval(int longRestInterval) {
        this.longRestInterval = longRestInterval;
    }

    public boolean isTaskReminderEnabled() {
        return taskReminderEnabled;
    }

    public void setTaskReminderEnabled(boolean taskReminderEnabled) {
        this.taskReminderEnabled = taskReminderEnabled;
    }

    public boolean isFocusEndReminderEnabled() {
        return focusEndReminderEnabled;
    }

    public void setFocusEndReminderEnabled(boolean focusEndReminderEnabled) {
        this.focusEndReminderEnabled = focusEndReminderEnabled;
    }

    public boolean isCheckInReminderEnabled() {
        return checkInReminderEnabled;
    }

    public void setCheckInReminderEnabled(boolean checkInReminderEnabled) {
        this.checkInReminderEnabled = checkInReminderEnabled;
    }

    public String getDailyReminderTime() {
        return dailyReminderTime;
    }

    public void setDailyReminderTime(String dailyReminderTime) {
        this.dailyReminderTime = dailyReminderTime;
    }
}
