package com.example.tomatostudy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.FocusRepository;
import com.example.tomatostudy.repository.TaskRepository;
import com.example.tomatostudy.repository.UserRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FocusRepository focusRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
        userRepository = new UserRepository(application);
        focusRepository = new FocusRepository(application);
    }

    public List<Task> loadTasks() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return new java.util.ArrayList<>();
        }
        return taskRepository.loadTasks(currentUser.getId());
    }

    public Task getTaskById(int taskId) {
        return taskRepository.getTaskById(taskId);
    }

    public boolean saveTask(Task task) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        if (task.getUserId() == 0) {
            task.setUserId(currentUser.getId());
        }
        if (task.getCollectionName() == null || task.getCollectionName().trim().isEmpty()) {
            task.setCollectionName("default");
        }

        if (task.getId() == 0) {
            task.setStatus(Task.STATUS_PENDING);
            return taskRepository.addTask(task) > 0;
        }
        return taskRepository.updateTask(task);
    }

    public boolean deleteTask(int taskId) {
        User currentUser = userRepository.getCurrentUser();
        Task task = taskRepository.getTaskById(taskId);
        if (currentUser == null || task == null || task.getUserId() != currentUser.getId()) {
            return false;
        }
        return taskRepository.deleteTask(taskId);
    }

    public int loadTaskFocusCount(int taskId) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        return focusRepository.loadTaskFocusCount(currentUser.getId(), taskId);
    }

    public int loadTaskFocusMinutes(int taskId) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        return focusRepository.loadTaskFocusMinutes(currentUser.getId(), taskId);
    }
}
