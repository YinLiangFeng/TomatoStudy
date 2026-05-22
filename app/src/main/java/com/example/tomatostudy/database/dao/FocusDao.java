package com.example.tomatostudy.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tomatostudy.database.TomatoDbHelper;
import com.example.tomatostudy.database.model.FocusDailyTrendItem;
import com.example.tomatostudy.database.model.FocusDurationItem;
import com.example.tomatostudy.database.model.FocusRecord;
import com.example.tomatostudy.database.model.FocusTimePeriodItem;
import com.example.tomatostudy.util.TimeZoneUtils;

import java.util.ArrayList;
import java.util.Calendar;
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

    public int getTotalFocusDays(int userId) {
        return queryInt(
                "SELECT COUNT(DISTINCT created_date) FROM " + TomatoDbHelper.TABLE_FOCUS_RECORD +
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

    public List<FocusDurationItem> getDailyTaskDurationDistribution(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 统计页“专注时长分布”按当天真实任务标题汇总专注分钟数，只统计已经完成且时长有效的专注记录。
        Cursor cursor = db.rawQuery(
                "SELECT task_title, IFNULL(SUM(duration_minutes), 0) AS total_minutes FROM " +
                        TomatoDbHelper.TABLE_FOCUS_RECORD +
                        " WHERE user_id=? AND created_date=? AND completed=1" +
                        " AND duration_minutes>0 AND task_title IS NOT NULL AND TRIM(task_title)<>''" +
                        " GROUP BY task_id, task_title" +
                        " ORDER BY total_minutes DESC",
                new String[]{String.valueOf(userId), date}
        );

        List<FocusDurationItem> items = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                items.add(new FocusDurationItem(
                        cursor.getString(cursor.getColumnIndexOrThrow("task_title")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("total_minutes"))
                ));
            }
            return items;
        } finally {
            cursor.close();
        }
    }

    public List<FocusTimePeriodItem> getMonthlyTimePeriodDistribution(int userId,
                                                                      long monthStartTime,
                                                                      long nextMonthStartTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 统计页“本月专注时段分布”统一用 Java 从 start_time 毫秒时间戳取本地小时，避免 SQLite localtime 转换造成小时偏移。
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_FOCUS_RECORD,
                new String[]{"start_time", "duration_minutes"},
                "user_id=? AND completed=1 AND duration_minutes>0 AND start_time>=? AND start_time<?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(monthStartTime),
                        String.valueOf(nextMonthStartTime)
                },
                null,
                null,
                "start_time ASC"
        );

        int[] minutesByHour = new int[24];
        Calendar calendar = Calendar.getInstance(TimeZoneUtils.CHINA_TIME_ZONE);
        try {
            while (cursor.moveToNext()) {
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow("start_time"));
                int durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes"));
                calendar.setTimeInMillis(startTime);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour >= 0 && hour <= 23) {
                    minutesByHour[hour] += durationMinutes;
                }
            }
        } finally {
            cursor.close();
        }

        List<FocusTimePeriodItem> items = new ArrayList<>();
        for (int hour = 0; hour < minutesByHour.length; hour++) {
            if (minutesByHour[hour] > 0) {
                items.add(new FocusTimePeriodItem(hour, minutesByHour[hour]));
            }
        }
        return items;
    }

    public List<FocusDailyTrendItem> getMonthlyFocusTrend(int userId,
                                                          long monthStartTime,
                                                          long nextMonthStartTime,
                                                          int daysInMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_FOCUS_RECORD,
                new String[]{"start_time", "duration_minutes"},
                "user_id=? AND completed=1 AND duration_minutes>0 AND start_time>=? AND start_time<?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(monthStartTime),
                        String.valueOf(nextMonthStartTime)
                },
                null,
                null,
                "start_time ASC"
        );

        int validDaysInMonth = Math.max(1, daysInMonth);
        int[] minutesByDay = new int[validDaysInMonth + 1];
        Calendar calendar = Calendar.getInstance(TimeZoneUtils.CHINA_TIME_ZONE);
        try {
            while (cursor.moveToNext()) {
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow("start_time"));
                int durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes"));
                calendar.setTimeInMillis(startTime);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                if (dayOfMonth >= 1 && dayOfMonth <= validDaysInMonth) {
                    minutesByDay[dayOfMonth] += durationMinutes;
                }
            }
        } finally {
            cursor.close();
        }

        List<FocusDailyTrendItem> items = new ArrayList<>();
        for (int day = 1; day <= validDaysInMonth; day++) {
            items.add(new FocusDailyTrendItem(day, minutesByDay[day]));
        }
        return items;
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
