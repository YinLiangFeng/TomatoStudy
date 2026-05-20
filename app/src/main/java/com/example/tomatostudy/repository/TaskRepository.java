package com.example.tomatostudy.repository;

import android.content.Context;

import com.example.tomatostudy.database.dao.TaskDao;
import com.example.tomatostudy.database.model.Task;

import java.util.List;

public class TaskRepository {

    private final TaskDao taskDao;

    public TaskRepository(Context context) {
        taskDao = new TaskDao(context);
    }

    public long addTask(Task task) {
        long now = System.currentTimeMillis();
        if (task.getCreatedTime() == 0) {
            task.setCreatedTime(now);
        }
        task.setUpdatedTime(now);
        if (task.getFocusMode() == null) {
            task.setFocusMode(Task.FOCUS_MODE_FORWARD);
        }
        if (task.getCollectionName() == null) {
            task.setCollectionName("default");
        }
        return taskDao.insertTask(task);
    }

    public boolean updateTask(Task task) {
        task.setUpdatedTime(System.currentTimeMillis());
        return taskDao.updateTask(task) > 0;
    }

    public boolean deleteTask(int taskId) {
        return taskDao.deleteTaskById(taskId) > 0;
    }

    public Task getTaskById(int taskId) {
        return taskDao.findTaskById(taskId);
    }

    public List<Task> loadTasks(int userId) {
        return taskDao.getTasksByUserId(userId);
    }

    public List<Task> loadTasksByStatus(int userId, int status) {
        return taskDao.getTasksByStatus(userId, status);
    }

    public boolean updateTaskStatus(int taskId, int status) {
        return taskDao.updateTaskStatus(taskId, status) > 0;
    }

    public boolean updateSortOrder(int taskId, int sortOrder) {
        return taskDao.updateSortOrder(taskId, sortOrder) > 0;
    }
}
