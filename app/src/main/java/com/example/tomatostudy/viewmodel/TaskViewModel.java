package com.example.tomatostudy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.FocusRepository;
import com.example.tomatostudy.repository.TaskRepository;
import com.example.tomatostudy.repository.UserRepository;
import com.example.tomatostudy.util.AppExecutors;

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

    public void loadTasksAsync(final AppExecutors.Callback<List<Task>> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final List<Task> tasks = loadTasks();
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(tasks);
                        }
                    }
                });
            }
        });
    }

    public Task getTaskById(int taskId) {
        return taskRepository.getTaskById(taskId);
    }

    public void getTaskByIdAsync(final int taskId, final AppExecutors.Callback<Task> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final Task task = getTaskById(taskId);
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(task);
                        }
                    }
                });
            }
        });
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

    public void saveTaskAsync(final Task task, final AppExecutors.Callback<Boolean> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final boolean success = saveTask(task);
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(success);
                        }
                    }
                });
            }
        });
    }

    public boolean deleteTask(int taskId) {
        User currentUser = userRepository.getCurrentUser();
        Task task = taskRepository.getTaskById(taskId);
        if (currentUser == null || task == null || task.getUserId() != currentUser.getId()) {
            return false;
        }
        return taskRepository.deleteTask(taskId);
    }

    public void deleteTaskAsync(final int taskId, final AppExecutors.Callback<Boolean> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final boolean success = deleteTask(taskId);
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(success);
                        }
                    }
                });
            }
        });
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

    public void loadTaskFocusSummaryAsync(final int taskId,
                                          final AppExecutors.Callback<TaskFocusSummary> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final TaskFocusSummary summary = new TaskFocusSummary(
                        loadTaskFocusCount(taskId),
                        loadTaskFocusMinutes(taskId)
                );
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(summary);
                        }
                    }
                });
            }
        });
    }

    public static class TaskFocusSummary {

        private final int focusCount;
        private final int focusMinutes;

        public TaskFocusSummary(int focusCount, int focusMinutes) {
            this.focusCount = focusCount;
            this.focusMinutes = focusMinutes;
        }

        public int getFocusCount() {
            return focusCount;
        }

        public int getFocusMinutes() {
            return focusMinutes;
        }
    }
}
