package com.example.tomatostudy.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tomatostudy.database.TomatoDbHelper;
import com.example.tomatostudy.database.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskDao {

    private final TomatoDbHelper dbHelper;

    public TaskDao(Context context) {
        dbHelper = TomatoDbHelper.getInstance(context);
    }

    public long insertTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = taskToValues(task);
        return db.insert(TomatoDbHelper.TABLE_TASK, null, values);
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = taskToValues(task);
        return db.update(
                TomatoDbHelper.TABLE_TASK,
                values,
                "id=?",
                new String[]{String.valueOf(task.getId())}
        );
    }

    public int deleteTaskById(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                TomatoDbHelper.TABLE_TASK,
                "id=?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public Task findTaskById(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_TASK,
                null,
                "id=?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return readTask(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public List<Task> getTasksByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_TASK,
                null,
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "sort_order ASC, created_time DESC"
        );
        return readTaskList(cursor);
    }

    public List<Task> getTasksByStatus(int userId, int status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_TASK,
                null,
                "user_id=? AND status=?",
                new String[]{String.valueOf(userId), String.valueOf(status)},
                null,
                null,
                "sort_order ASC, created_time DESC"
        );
        return readTaskList(cursor);
    }

    public int updateTaskStatus(int taskId, int status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("updated_time", System.currentTimeMillis());
        return db.update(
                TomatoDbHelper.TABLE_TASK,
                values,
                "id=?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public int updateSortOrder(int taskId, int sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sort_order", sortOrder);
        values.put("updated_time", System.currentTimeMillis());
        return db.update(
                TomatoDbHelper.TABLE_TASK,
                values,
                "id=?",
                new String[]{String.valueOf(taskId)}
        );
    }

    private ContentValues taskToValues(Task task) {
        ContentValues values = new ContentValues();
        values.put("user_id", task.getUserId());
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("background_res", task.getBackgroundRes());
        values.put("focus_mode", task.getFocusMode());
        values.put("focus_minutes", task.getFocusMinutes());
        values.put("rest_minutes", task.getRestMinutes());
        values.put("priority", task.getPriority());
        values.put("status", task.getStatus());
        values.put("sort_order", task.getSortOrder());
        values.put("reminder_time", task.getReminderTime());
        values.put("collection_name", task.getCollectionName());
        values.put("created_time", task.getCreatedTime());
        values.put("updated_time", task.getUpdatedTime());
        return values;
    }

    private List<Task> readTaskList(Cursor cursor) {
        List<Task> tasks = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                tasks.add(readTask(cursor));
            }
            return tasks;
        } finally {
            cursor.close();
        }
    }

    private Task readTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        task.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        task.setBackgroundRes(cursor.getString(cursor.getColumnIndexOrThrow("background_res")));
        task.setFocusMode(cursor.getString(cursor.getColumnIndexOrThrow("focus_mode")));
        task.setFocusMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("focus_minutes")));
        task.setRestMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("rest_minutes")));
        task.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow("priority")));
        task.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
        task.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow("sort_order")));
        task.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow("reminder_time")));
        task.setCollectionName(cursor.getString(cursor.getColumnIndexOrThrow("collection_name")));
        task.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow("created_time")));
        task.setUpdatedTime(cursor.getLong(cursor.getColumnIndexOrThrow("updated_time")));
        return task;
    }
}
