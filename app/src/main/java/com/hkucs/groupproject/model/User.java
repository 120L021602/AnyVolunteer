package com.hkucs.groupproject.model;

public class User {
    private String username;
    private String password;
    private String userId;
    private int remainingTokens;
    private int imageCredits;
    private boolean isManager;

    public User(String username, String password, String userId) {
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.remainingTokens = 1000;  // 默认1000 tokens
        this.imageCredits = 3;        // 默认3次图片处理机会
        this.isManager = false;       // 默认非管理员
    }

    public User(String username, String password, String userId, int remainingTokens, int imageCredits, boolean isManager) {
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.remainingTokens = remainingTokens;
        this.imageCredits = imageCredits;
        this.isManager = isManager;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public void setRemainingTokens(int remainingTokens) {
        this.remainingTokens = remainingTokens;
    }

    public int getImageCredits() {
        return imageCredits;
    }

    public void setImageCredits(int imageCredits) {
        this.imageCredits = imageCredits;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }
}