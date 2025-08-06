package com.hkucs.groupproject.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat_history.db";
    private static final int DATABASE_VERSION = 6;
    // Chat history table
    public static final String TABLE_CHAT_HISTORY = "chat_history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_SUMMARY = "summary";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TOKENS_USED = "tokens_used";
    
    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_REMAINING_TOKENS = "remaining_tokens";
    public static final String COLUMN_IMAGE_CREDITS = "image_credits";
    public static final String COLUMN_IS_MANAGER = "is_manager";

    // image chat history table
    public static final String TABLE_IMAGE_TASKS = "image_tasks";
    public static final String COLUMN_TASK_TEXT = "task_text";
    public static final String COLUMN_IMAGE_ORIGINAL = "image_path_original";
    public static final String COLUMN_IMAGE_PROCESSED = "image_path_processed";
    public static final String COLUMN_LLM_REPLY = "llm_reply";

    private static final String CREATE_TABLE_IMAGE_TASKS =
            "CREATE TABLE " + TABLE_IMAGE_TASKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CHAT_ID + " TEXT, " +
                    COLUMN_TASK_TEXT + " TEXT, " +
                    COLUMN_IMAGE_ORIGINAL + " TEXT, " +
                    COLUMN_IMAGE_PROCESSED + " TEXT, " +
                    COLUMN_LLM_REPLY + " TEXT, " +
                    COLUMN_TIMESTAMP + " TEXT, " +
                    COLUMN_USER_ID + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_CHAT_HISTORY =
            "CREATE TABLE " + TABLE_CHAT_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CHAT_ID + " TEXT, " +
                    COLUMN_SUMMARY + " TEXT, " +
                    COLUMN_TIMESTAMP + " TEXT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_TOKENS_USED + " INTEGER DEFAULT 0" +
                    ");";  
                    
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT UNIQUE, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_REMAINING_TOKENS + " INTEGER DEFAULT 1000, " +
                    COLUMN_IMAGE_CREDITS + " INTEGER DEFAULT 3, " +
                    COLUMN_IS_MANAGER + " INTEGER DEFAULT 0" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHAT_HISTORY);
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_IMAGE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL("ALTER TABLE " + TABLE_CHAT_HISTORY + " ADD COLUMN COLUMN_USER_ID TEXT");
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_HISTORY + " ADD COLUMN image_path TEXT");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_HISTORY + " ADD COLUMN task_type TEXT");
        }

        if (oldVersion < 5) {
            db.execSQL(CREATE_TABLE_IMAGE_TASKS);
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_REMAINING_TOKENS + " INTEGER DEFAULT 1000");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_IMAGE_CREDITS + " INTEGER DEFAULT 3");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_IS_MANAGER + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_HISTORY + " ADD COLUMN " + COLUMN_TOKENS_USED + " INTEGER DEFAULT 0");
        }
    }
}