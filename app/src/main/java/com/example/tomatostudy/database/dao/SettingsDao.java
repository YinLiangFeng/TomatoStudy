package com.example.tomatostudy.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tomatostudy.database.TomatoDbHelper;
import com.example.tomatostudy.database.model.Settings;

public class SettingsDao {

    private final TomatoDbHelper dbHelper;

    public SettingsDao(Context context) {
        dbHelper = TomatoDbHelper.getInstance(context);
    }

    public long insertSettings(Settings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(TomatoDbHelper.TABLE_SETTINGS, null, settingsToValues(settings));
    }

    public int updateSettings(Settings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(
                TomatoDbHelper.TABLE_SETTINGS,
                settingsToValues(settings),
                "user_id=?",
                new String[]{String.valueOf(settings.getUserId())}
        );
    }

    public void saveSettings(Settings settings) {
        Settings oldSettings = findByUserId(settings.getUserId());
        if (oldSettings == null) {
            insertSettings(settings);
        } else {
            updateSettings(settings);
        }
    }

    public Settings findByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_SETTINGS,
                null,
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return readSettings(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private ContentValues settingsToValues(Settings settings) {
        ContentValues values = new ContentValues();
        values.put("user_id", settings.getUserId());
        values.put("default_focus_minutes", settings.getDefaultFocusMinutes());
        values.put("default_rest_minutes", settings.getDefaultRestMinutes());
        values.put("long_rest_minutes", settings.getLongRestMinutes());
        values.put("long_rest_interval", settings.getLongRestInterval());
        values.put("task_reminder_enabled", settings.isTaskReminderEnabled() ? 1 : 0);
        values.put("focus_end_reminder_enabled", settings.isFocusEndReminderEnabled() ? 1 : 0);
        values.put("check_in_reminder_enabled", settings.isCheckInReminderEnabled() ? 1 : 0);
        values.put("daily_reminder_time", settings.getDailyReminderTime());
        return values;
    }

    private Settings readSettings(Cursor cursor) {
        Settings settings = new Settings();
        settings.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        settings.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        settings.setDefaultFocusMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("default_focus_minutes")));
        settings.setDefaultRestMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("default_rest_minutes")));
        settings.setLongRestMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("long_rest_minutes")));
        settings.setLongRestInterval(cursor.getInt(cursor.getColumnIndexOrThrow("long_rest_interval")));
        settings.setTaskReminderEnabled(cursor.getInt(cursor.getColumnIndexOrThrow("task_reminder_enabled")) == 1);
        settings.setFocusEndReminderEnabled(cursor.getInt(cursor.getColumnIndexOrThrow("focus_end_reminder_enabled")) == 1);
        settings.setCheckInReminderEnabled(cursor.getInt(cursor.getColumnIndexOrThrow("check_in_reminder_enabled")) == 1);
        settings.setDailyReminderTime(cursor.getString(cursor.getColumnIndexOrThrow("daily_reminder_time")));
        return settings;
    }
}
