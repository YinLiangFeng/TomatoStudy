package com.example.tomatostudy.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tomatostudy.database.TomatoDbHelper;
import com.example.tomatostudy.database.model.User;

public class UserDao {

    private final TomatoDbHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = TomatoDbHelper.getInstance(context);
    }

    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("nickname", user.getNickname());
        values.put("avatar", user.getAvatar());
        values.put("profile", user.getProfile());
        values.put("is_login", user.isLogin() ? 1 : 0);
        values.put("created_time", user.getCreatedTime());
        return db.insert(TomatoDbHelper.TABLE_USER, null, values);
    }

    public User findById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_USER,
                null,
                "id=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return readUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public User findByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_USER,
                null,
                "username=?",
                new String[]{username},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return readUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public User findByUsernameAndPassword(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_USER,
                null,
                "username=? AND password=?",
                new String[]{username, password},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return readUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public User findLoginUser() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TomatoDbHelper.TABLE_USER,
                null,
                "is_login=1",
                null,
                null,
                null,
                "id DESC",
                "1"
        );
        try {
            if (cursor.moveToFirst()) {
                return readUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public int clearLoginStatus() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_login", 0);
        return db.update(TomatoDbHelper.TABLE_USER, values, null, null);
    }

    public int updateLoginStatus(int userId, boolean login) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_login", login ? 1 : 0);
        return db.update(
                TomatoDbHelper.TABLE_USER,
                values,
                "id=?",
                new String[]{String.valueOf(userId)}
        );
    }

    public int updateProfile(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nickname", user.getNickname());
        values.put("avatar", user.getAvatar());
        values.put("profile", user.getProfile());
        return db.update(
                TomatoDbHelper.TABLE_USER,
                values,
                "id=?",
                new String[]{String.valueOf(user.getId())}
        );
    }

    private User readUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow("avatar")));
        user.setProfile(cursor.getString(cursor.getColumnIndexOrThrow("profile")));
        user.setLogin(cursor.getInt(cursor.getColumnIndexOrThrow("is_login")) == 1);
        user.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow("created_time")));
        return user;
    }
}
