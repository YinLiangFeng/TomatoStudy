package com.example.tomatostudy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TomatoDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tomato_study.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USER = "user";
    public static final String TABLE_TASK = "task";
    public static final String TABLE_FOCUS_RECORD = "focus_record";
    public static final String TABLE_CHECK_IN = "check_in";
    public static final String TABLE_SETTINGS = "settings";

    private static final String CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "nickname TEXT, " +
                    "avatar TEXT, " +
                    "profile TEXT, " +
                    "is_login INTEGER NOT NULL DEFAULT 0, " +
                    "created_time INTEGER NOT NULL" +
                    ")";

    private static final String CREATE_TASK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TASK + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "background_res TEXT, " +
                    "focus_mode TEXT NOT NULL DEFAULT 'forward', " +
                    "focus_minutes INTEGER NOT NULL DEFAULT 25, " +
                    "rest_minutes INTEGER NOT NULL DEFAULT 5, " +
                    "priority INTEGER NOT NULL DEFAULT 0, " +
                    "status INTEGER NOT NULL DEFAULT 0, " +
                    "sort_order INTEGER NOT NULL DEFAULT 0, " +
                    "reminder_time TEXT, " +
                    "collection_name TEXT DEFAULT 'default', " +
                    "created_time INTEGER NOT NULL, " +
                    "updated_time INTEGER NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                    ")";

    private static final String CREATE_FOCUS_RECORD_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FOCUS_RECORD + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "task_id INTEGER DEFAULT 0, " +
                    "task_title TEXT, " +
                    "start_time INTEGER NOT NULL, " +
                    "end_time INTEGER NOT NULL, " +
                    "duration_minutes INTEGER NOT NULL DEFAULT 0, " +
                    "completed INTEGER NOT NULL DEFAULT 1, " +
                    "created_date TEXT NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                    ")";

    private static final String CREATE_CHECK_IN_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CHECK_IN + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "check_date TEXT NOT NULL, " +
                    "content TEXT, " +
                    "created_time INTEGER NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id), " +
                    "UNIQUE(user_id, check_date)" +
                    ")";

    private static final String CREATE_SETTINGS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL UNIQUE, " +
                    "default_focus_minutes INTEGER NOT NULL DEFAULT 25, " +
                    "default_rest_minutes INTEGER NOT NULL DEFAULT 5, " +
                    "long_rest_minutes INTEGER NOT NULL DEFAULT 15, " +
                    "long_rest_interval INTEGER NOT NULL DEFAULT 4, " +
                    "task_reminder_enabled INTEGER NOT NULL DEFAULT 1, " +
                    "focus_end_reminder_enabled INTEGER NOT NULL DEFAULT 1, " +
                    "check_in_reminder_enabled INTEGER NOT NULL DEFAULT 1, " +
                    "daily_reminder_time TEXT DEFAULT '20:00', " +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                    ")";

    private static TomatoDbHelper instance;

    public static synchronized TomatoDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TomatoDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TomatoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 只有当 DATABASE_VERSION 变大时，系统才会自动调用这里。
        // 当前项目还处于初始开发阶段，版本号保持 1，暂时不需要真正的升级 SQL。
        // 后续如果发布后再新增字段或新表，应在这里根据 oldVersion 逐步补充 ALTER TABLE 等升级逻辑。
        createAllTables(db);
    }

    private void createAllTables(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TASK_TABLE);
        db.execSQL(CREATE_FOCUS_RECORD_TABLE);
        db.execSQL(CREATE_CHECK_IN_TABLE);
        db.execSQL(CREATE_SETTINGS_TABLE);
    }
}
