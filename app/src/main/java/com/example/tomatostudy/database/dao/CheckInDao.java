package com.example.tomatostudy.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tomatostudy.database.TomatoDbHelper;
import com.example.tomatostudy.database.model.CheckIn;

import java.util.ArrayList;
import java.util.List;

public class CheckInDao {

    private final TomatoDbHelper dbHelper;

    public CheckInDao(Context context) {
        dbHelper = TomatoDbHelper.getInstance(context);
    }

    public long insertCheckIn(CheckIn checkIn) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", checkIn.getUserId());
        values.put("check_date", checkIn.getCheckDate());
        values.put("content", checkIn.getContent());
        values.put("created_time", checkIn.getCreatedTime());
        return db.insert(TomatoDbHelper.TABLE_CHECK_IN, null, values);
    }

    public CheckIn findByDate(int userId, String checkDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_CHECK_IN,
                null,
                "user_id=? AND check_date=?",
                new String[]{String.valueOf(userId), checkDate},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return readCheckIn(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public boolean hasCheckIn(int userId, String checkDate) {
        return findByDate(userId, checkDate) != null;
    }

    public int countByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TomatoDbHelper.TABLE_CHECK_IN + " WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            cursor.close();
        }
    }

    public List<CheckIn> getCheckInsByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_CHECK_IN,
                null,
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "check_date DESC"
        );
        List<CheckIn> checkIns = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                checkIns.add(readCheckIn(cursor));
            }
            return checkIns;
        } finally {
            cursor.close();
        }
    }

    private CheckIn readCheckIn(Cursor cursor) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        checkIn.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        checkIn.setCheckDate(cursor.getString(cursor.getColumnIndexOrThrow("check_date")));
        checkIn.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        checkIn.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow("created_time")));
        return checkIn;
    }
}
