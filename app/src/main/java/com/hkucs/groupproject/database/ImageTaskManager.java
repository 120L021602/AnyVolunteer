package com.hkucs.groupproject.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ImageTaskManager {
    private DatabaseHelper dbHelper;

    public ImageTaskManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void saveImageTask(String chatId, String taskText, String imagePathOriginal, String imagePathProcessed, String llmReply, String timestamp, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("chat_id", chatId);
        values.put("task_text", taskText);
        values.put("image_path_original", imagePathOriginal);
        values.put("image_path_processed", imagePathProcessed);
        values.put("llm_reply", llmReply);
        values.put("timestamp", timestamp);
        values.put("user_id", userId);
        db.insert(DatabaseHelper.TABLE_IMAGE_TASKS, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<ImageTask> getImageTasksByUser(String userId) {
        List<ImageTask> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_IMAGE_TASKS, null, "user_id = ?", new String[]{userId}, null, null, "timestamp DESC");

        while (cursor.moveToNext()) {
            ImageTask task = new ImageTask();
            task.chatId = cursor.getString(cursor.getColumnIndex("chat_id"));
            task.taskText = cursor.getString(cursor.getColumnIndex("task_text"));
            task.imagePathOriginal = cursor.getString(cursor.getColumnIndex("image_path_original"));
            task.imagePathProcessed = cursor.getString(cursor.getColumnIndex("image_path_processed"));
            task.llmReply = cursor.getString(cursor.getColumnIndex("llm_reply"));
            task.timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
            list.add(task);
        }

        cursor.close();
        db.close();
        return list;
    }


    public void updateTaskAndReply(String chatId, String taskText, String llmReply) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("task_text", taskText);
        values.put("llm_reply", llmReply);
        db.update(DatabaseHelper.TABLE_IMAGE_TASKS, values, "chat_id = ?", new String[]{chatId});
        db.close();
    }


    public ImageTask getImageTaskByChatId(String chatId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ImageTask task = null;

        Cursor cursor = db.query(
                "image_tasks",
                null,
                "chat_id = ?",
                new String[]{chatId},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String taskText = cursor.getString(cursor.getColumnIndexOrThrow("task_text"));
            String imagePathOriginal = cursor.getString(cursor.getColumnIndexOrThrow("image_path_original"));
            String imagePathProcessed = cursor.getString(cursor.getColumnIndexOrThrow("image_path_processed"));
            String llmReply = cursor.getString(cursor.getColumnIndexOrThrow("llm_reply"));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
            String userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));

            task = new ImageTask(chatId, taskText, imagePathOriginal, imagePathProcessed, llmReply, timestamp, userId);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return task;
    }


}