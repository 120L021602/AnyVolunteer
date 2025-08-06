package com.hkucs.groupproject.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hkucs.groupproject.ChatSummary;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryManager {

    private DatabaseHelper dbHelper;
    private UserManager userManager;

    public ChatHistoryManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        userManager = new UserManager(context);
    }

//    public List<ChatSummary> getChatHistory() {
//        List<ChatSummary> historyList = new ArrayList<>();
//
//        // Get current logged-in user ID
//        String currentUserId = userManager.getCurrentUserId();
//        if (currentUserId == null) {
//            // If no user is logged in, return empty list
//            return historyList;
//        }
//
//        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
//             Cursor cursor = db.query(DatabaseHelper.TABLE_CHAT_HISTORY, null,
//                     DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{currentUserId},
//                     null, null, DatabaseHelper.COLUMN_TIMESTAMP + " DESC")) {
//
//            while (cursor.moveToNext()) {
//                String chatId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAT_ID));
//                String summary = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUMMARY));
//                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));
//                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path")); //new
//                String taskType = cursor.getString(cursor.getColumnIndexOrThrow("task_type")); //new
//
//                try {
//                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                }
//
//                historyList.add(new ChatSummary(summary, timestamp, chatId, imagePath, taskType)); //new
//            }
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
//        Log.d("DEBUG", "Querying chat for user: " + currentUserId);
//        return historyList;
//
//    }
    public List<ChatSummary> getChatHistory() {
        List<ChatSummary> historyList = new ArrayList<>();

        String currentUserId = userManager.getCurrentUserId();
        Log.d("DEBUG", "[QUERY] currentUserId = " + currentUserId);

        if (currentUserId == null) {
            Log.d("DEBUG", "[QUERY] No user logged in");
            return historyList;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_CHAT_HISTORY, null,
                     DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{currentUserId},
                     null, null, DatabaseHelper.COLUMN_TIMESTAMP + " DESC")) {

            Log.d("DEBUG", "[QUERY] result count = " + cursor.getCount());

            while (cursor.moveToNext()) {
                String chatId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHAT_ID));
                String summary = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUMMARY));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));
                Log.d("DEBUG", "[QUERY] row = " + summary + " / " + timestamp);
                historyList.add(new ChatSummary(summary, timestamp, chatId, null, null));
            }
        } catch (Exception e) {
            Log.e("DEBUG", "[QUERY] failed: " + e.getMessage());
        }

        return historyList;
    }

//    public void saveChatHistory(String chatId, String summary, String timestamp) {
//        // Get current logged-in user ID
//        String currentUserId = userManager.getCurrentUserId();
//
//        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
//            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_CHAT_HISTORY + "(" +
//                            DatabaseHelper.COLUMN_CHAT_ID + ", " +
//                            DatabaseHelper.COLUMN_SUMMARY + ", " +
//                            DatabaseHelper.COLUMN_TIMESTAMP + ", " +
//                            DatabaseHelper.COLUMN_USER_ID + ") VALUES (?, ?, ?, ?)",
//                    new Object[]{chatId, summary, timestamp, currentUserId});
//        }
//        Log.d("DEBUG", "Saving chat for user: " + currentUserId);
//
//    }
    public void saveChatHistory(String chatId, String summary, String timestamp) {
        String currentUserId = userManager.getCurrentUserId();
        Log.d("DEBUG", "[SAVE] chatId = " + chatId);
        Log.d("DEBUG", "[SAVE] userId = " + currentUserId);
        Log.d("DEBUG", "[SAVE] summary = " + summary);
        Log.d("DEBUG", "[SAVE] timestamp = " + timestamp);

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_CHAT_HISTORY + "(" +
                            DatabaseHelper.COLUMN_CHAT_ID + ", " +
                            DatabaseHelper.COLUMN_SUMMARY + ", " +
                            DatabaseHelper.COLUMN_TIMESTAMP + ", " +
                            DatabaseHelper.COLUMN_USER_ID + ") VALUES (?, ?, ?, ?)",
                    new Object[]{chatId, summary, timestamp, currentUserId});
            Log.d("DEBUG", "[SAVE] insert success");
        } catch (Exception e) {
            Log.e("DEBUG", "[SAVE] insert failed: " + e.getMessage());
        }
    }


    public String getChatContentById(String chatId) {
        String chatContent = null;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_CHAT_HISTORY, null,
                     DatabaseHelper.COLUMN_CHAT_ID + " = ?", new String[]{chatId}, null, null, null)) {

            if (cursor.moveToFirst()) {
                chatContent = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUMMARY));
            }
        }
        return chatContent;
    }

    public void deleteChatHistoryByIdRange(int startId, int endId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_CHAT_HISTORY + " WHERE " +
                    DatabaseHelper.COLUMN_ID + " BETWEEN ? AND ?", new Object[]{startId, endId});
        }
    }

    public void saveChatHistory(String chatId, String summary, String timestamp, String imagePath) {
        // 获取当前登录用户 ID
        String currentUserId = userManager.getCurrentUserId();

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_CHAT_HISTORY + "(" +
                            DatabaseHelper.COLUMN_CHAT_ID + ", " +
                            DatabaseHelper.COLUMN_SUMMARY + ", " +
                            DatabaseHelper.COLUMN_TIMESTAMP + ", " +
                            DatabaseHelper.COLUMN_USER_ID + ", " +
                            "image_path" +
                            ") VALUES (?, ?, ?, ?, ?)",
                    new Object[]{chatId, summary, timestamp, currentUserId, imagePath});
        }
    }
}