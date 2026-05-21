package com.example.tomatostudy.database.model;

public class Task {

    public static final String FOCUS_MODE_FORWARD = "forward";
    public static final String FOCUS_MODE_COUNTDOWN = "countdown";
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_COMPLETED = 1;

    private int id;
    private int userId;
    private String title;
    private String description;
    private String backgroundRes;
    private String focusMode = FOCUS_MODE_FORWARD;
    private int focusMinutes = 25;
    private int restMinutes = 5;
    private int priority;
    private int status;
    private int sortOrder;
    private String reminderTime;
    private String collectionName = "default";
    private long createdTime;
    private long updatedTime;

    public Task() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackgroundRes() {
        return backgroundRes;
    }

    public void setBackgroundRes(String backgroundRes) {
        this.backgroundRes = backgroundRes;
    }

    public String getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(String focusMode) {
        this.focusMode = focusMode;
    }

    public int getFocusMinutes() {
        return focusMinutes;
    }

    public void setFocusMinutes(int focusMinutes) {
        this.focusMinutes = focusMinutes;
    }

    public int getRestMinutes() {
        return restMinutes;
    }

    public void setRestMinutes(int restMinutes) {
        this.restMinutes = restMinutes;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
