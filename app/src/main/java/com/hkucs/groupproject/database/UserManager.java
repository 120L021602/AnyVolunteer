package com.hkucs.groupproject.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hkucs.groupproject.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UserManager {

    private DatabaseHelper dbHelper;
    private static final String PREF_NAME = "user_session";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";
    private static final String KEY_LOGGED_IN_USERNAME = "logged_in_username";
    private Context context;

    public UserManager(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Hash a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register a new user
     * @param username username
     * @param password password
     * @return Return true if registration is successful, false otherwise
     */
    public boolean registerUser(String username, String password) {
        // Check if the username already exists
        if (isUsernameExists(username)) {
            return false;
        }

        // Generate a unique user ID
        String userId = UUID.randomUUID().toString();

        // Hash the password before storing
        String hashedPassword = hashPassword(password);

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            // Special handling for manager account
            boolean isManager = "manager".equals(username);
            int remainingTokens = isManager ? Integer.MAX_VALUE : 1000;  // manager account unlimited tokens
            int imageCredits = isManager ? Integer.MAX_VALUE : 3;  // manager account unlimited image credits
            
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_USERS + " (" +
                            DatabaseHelper.COLUMN_USER_ID + ", " +
                            DatabaseHelper.COLUMN_USERNAME + ", " +
                            DatabaseHelper.COLUMN_PASSWORD + ", " +
                            DatabaseHelper.COLUMN_REMAINING_TOKENS + ", " +
                            DatabaseHelper.COLUMN_IMAGE_CREDITS + ", " +
                            DatabaseHelper.COLUMN_IS_MANAGER + ") VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{userId, username, hashedPassword, remainingTokens, imageCredits, isManager ? 1 : 0});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查用户是否有足够的token
     * @param requiredTokens 需要的token数量
     * @return 如果有足够的token返回true，否则返回false
     */
    public boolean hasEnoughTokens(int requiredTokens) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, 
                     new String[]{DatabaseHelper.COLUMN_REMAINING_TOKENS, DatabaseHelper.COLUMN_IS_MANAGER},
                     DatabaseHelper.COLUMN_USER_ID + " = ?", 
                     new String[]{userId}, 
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                boolean isManager = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_MANAGER)) == 1;
                if (isManager) {
                    return true;  // 管理员账号总是返回true
                }
                int remainingTokens = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMAINING_TOKENS));
                return remainingTokens >= requiredTokens;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查用户是否有剩余的图片处理次数
     * @return 如果有剩余次数返回true，否则返回false
     */
    public boolean hasImageCredits() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, 
                     new String[]{DatabaseHelper.COLUMN_IMAGE_CREDITS, DatabaseHelper.COLUMN_IS_MANAGER},
                     DatabaseHelper.COLUMN_USER_ID + " = ?", 
                     new String[]{userId}, 
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                boolean isManager = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_MANAGER)) == 1;
                if (isManager) {
                    return true;  // 管理员账号总是返回true
                }
                int imageCredits = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_CREDITS));
                return imageCredits > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 消耗指定数量的token
     * @param tokens 要消耗的token数量
     * @return 如果成功消耗返回true，否则返回false
     */
    public boolean consumeTokens(int tokens) {
        String userId = getCurrentUserId();
        if (userId == null || !hasEnoughTokens(tokens)) {
            return false;
        }

        try (SQLiteDatabase db = dbHelper.getWritableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                     new String[]{DatabaseHelper.COLUMN_IS_MANAGER},
                     DatabaseHelper.COLUMN_USER_ID + " = ?",
                     new String[]{userId},
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                boolean isManager = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_MANAGER)) == 1;
                if (isManager) {
                    return true;  // 管理员账号不消耗token
                }
            }

            db.execSQL("UPDATE " + DatabaseHelper.TABLE_USERS + 
                    " SET " + DatabaseHelper.COLUMN_REMAINING_TOKENS + " = " + 
                    DatabaseHelper.COLUMN_REMAINING_TOKENS + " - ? " +
                    "WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? AND " + 
                    DatabaseHelper.COLUMN_IS_MANAGER + " = 0",
                    new Object[]{tokens, userId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 消耗一次图片处理机会
     * @return 如果成功消耗返回true，否则返回false
     */
    public boolean consumeImageCredit() {
        String userId = getCurrentUserId();
        if (userId == null || !hasImageCredits()) {
            return false;
        }

        try (SQLiteDatabase db = dbHelper.getWritableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                     new String[]{DatabaseHelper.COLUMN_IS_MANAGER},
                     DatabaseHelper.COLUMN_USER_ID + " = ?",
                     new String[]{userId},
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                boolean isManager = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_MANAGER)) == 1;
                if (isManager) {
                    return true;  // 管理员账号不消耗图片处理机会
                }
            }

            db.execSQL("UPDATE " + DatabaseHelper.TABLE_USERS + 
                    " SET " + DatabaseHelper.COLUMN_IMAGE_CREDITS + " = " + 
                    DatabaseHelper.COLUMN_IMAGE_CREDITS + " - 1 " +
                    "WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? AND " + 
                    DatabaseHelper.COLUMN_IS_MANAGER + " = 0",
                    new Object[]{userId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户剩余的token数量
     * @return 剩余的token数量，如果是管理员返回Integer.MAX_VALUE
     */
    public int getRemainingTokens() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return 0;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, 
                     new String[]{DatabaseHelper.COLUMN_REMAINING_TOKENS, DatabaseHelper.COLUMN_IS_MANAGER},
                     DatabaseHelper.COLUMN_USER_ID + " = ?", 
                     new String[]{userId}, 
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                boolean isManager = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_MANAGER)) == 1;
                if (isManager) {
                    return Integer.MAX_VALUE;
                }
                return cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMAINING_TOKENS));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取用户剩余的图片处理次数
     * @return 剩余的图片处理次数，如果是管理员返回Integer.MAX_VALUE
     */
    public int getRemainingImageCredits() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return 0;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, 
                     new String[]{DatabaseHelper.COLUMN_IMAGE_CREDITS, DatabaseHelper.COLUMN_IS_MANAGER},
                     DatabaseHelper.COLUMN_USER_ID + " = ?", 
                     new String[]{userId}, 
                     null, null, null)) {

            if (cursor.moveToFirst()) {
                boolean isManager = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_MANAGER)) == 1;
                if (isManager) {
                    return Integer.MAX_VALUE;
                }
                return cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_CREDITS));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Validate user login
     * @param username username
     * @param password password
     * @return Return true if login is successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                     DatabaseHelper.COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null)) {

            if (cursor.moveToFirst()) {
                String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                
                // Hash the input password before comparison
                String hashedInput = hashPassword(password);
                if (hashedInput.equals(storedPassword)) {
                    // Login successful, save user session
                    saveUserSession(userId, username);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the username already exists
     * @param username username
     * @return Return true if it exists, false otherwise
     */
    private boolean isUsernameExists(String username) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                     DatabaseHelper.COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null)) {

            return cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save user session information to SharedPreferences
     * @param userId User ID
     * @param username username
     */
    private void saveUserSession(String userId, String username) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LOGGED_IN_USER_ID, userId);
        editor.putString(KEY_LOGGED_IN_USERNAME, username);
        editor.apply();
    }

    /**
     * Get the ID of the currently logged-in user
     * @return User ID, null if not logged in
     */
    public String getCurrentUserId() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LOGGED_IN_USER_ID, null);
    }

    /**
     * Get the username of the currently logged-in user
     * @return Username, null if not logged in
     */
    public String getCurrentUsername() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LOGGED_IN_USERNAME, null);
    }

    /**
     * Get the information of the currently logged-in user
     * @return User object, null if not logged in
     */
    public User getCurrentUser() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                     DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId}, null, null, null)) {

            if (cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
                return new User(username, password, userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * User logout
     */
    public void logout() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}