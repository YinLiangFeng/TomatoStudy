package com.example.tomatostudy.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tomatostudy.database.TomatoDbHelper;
import com.example.tomatostudy.database.model.FocusRecord;

import java.util.ArrayList;
import java.util.List;

public class FocusDao {

    private final TomatoDbHelper dbHelper;

    public FocusDao(Context context) {
        dbHelper = TomatoDbHelper.getInstance(context);
    }

    public long insertFocusRecord(FocusRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", record.getUserId());
        values.put("task_id", record.getTaskId());
        values.put("task_title", record.getTaskTitle());
        values.put("start_time", record.getStartTime());
        values.put("end_time", record.getEndTime());
        values.put("duration_minutes", record.getDurationMinutes());
        values.put("completed", record.isCompleted() ? 1 : 0);
        values.put("created_date", record.getCreatedDate());
        return db.insert(TomatoDbHelper.TABLE_FOCUS_RECORD, null, values);
    }

    public List<FocusRecord> getRecordsByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_FOCUS_RECORD,
                null,
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "start_time DESC"
        );
        return readRecordList(cursor);
    }

    public List<FocusRecord> getRecordsByTaskId(int userId, int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_FOCUS_RECORD,
                null,
                "user_id=? AND task_id=?",
                new String[]{String.valueOf(userId), String.valueOf(taskId)},
                null,
                null,
                "start_time DESC"
        );
        return readRecordList(cursor);
    }

    public List<FocusRecord> getRecordsByDate(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_FOCUS_RECORD,
                null,
                "user_id=? AND created_date=?",
                new String[]{String.valueOf(userId), date},
                null,
                null,
                "start_time DESC"
        );
        return readRecordList(cursor);
    }

    public int getTotalFocusCount(int userId) {
        return queryInt(
                "SELECT COUNT(*) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND completed=1",
                new String[]{String.valueOf(userId)}
        );
    }

    public int getTotalFocusMinutes(int userId) {
        return queryInt(
                "SELECT IFNULL(SUM(duration_minutes), 0) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND completed=1",
                new String[]{String.valueOf(userId)}
        );
    }

    public int getDailyFocusCount(int userId, String date) {
        return queryInt(
                "SELECT COUNT(*) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND created_date=? AND completed=1",
                new String[]{String.valueOf(userId), date}
        );
    }

    public int getDailyFocusMinutes(int userId, String date) {
        return queryInt(
                "SELECT IFNULL(SUM(duration_minutes), 0) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND created_date=? AND completed=1",
                new String[]{String.valueOf(userId), date}
        );
    }

    public int getTaskFocusCount(int userId, int taskId) {
        return queryInt(
                "SELECT COUNT(*) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND task_id=? AND completed=1",
                new String[]{String.valueOf(userId), String.valueOf(taskId)}
        );
    }

    public int getTaskFocusMinutes(int userId, int taskId) {
        return queryInt(
                "SELECT IFNULL(SUM(duration_minutes), 0) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND task_id=? AND completed=1",
                new String[]{String.valueOf(userId), String.valueOf(taskId)}
        );
    }

    private int queryInt(String sql, String[] args) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            cursor.close();
        }
    }

    private List<FocusRecord> readRecordList(Cursor cursor) {
        List<FocusRecord> records = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                records.add(readRecord(cursor));
            }
            return records;
        } finally {
            cursor.close();
        }
    }

    private FocusRecord readRecord(Cursor cursor) {
        FocusRecord record = new FocusRecord();
        record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        record.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        record.setTaskId(cursor.getInt(cursor.getColumnIndexOrThrow("task_id")));
        record.setTaskTitle(cursor.getString(cursor.getColumnIndexOrThrow("task_title")));
        record.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow("start_time")));
        record.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow("end_time")));
        record.setDurationMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes")));
        record.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1);
        record.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow("created_date")));
        return record;
    }
}
