package com.hkucs.groupproject;

public class ChatSummary {
    private String summary;
    private String timestamp;
    private String chatId;
    private String imagePath;
    private String taskType;

    public ChatSummary(String summary, String timestamp, String chatId, String imagePath, String taskType) {
        this.summary = summary;
        this.timestamp = timestamp;
        this.chatId = chatId;
        this.imagePath = imagePath;
        this.taskType = taskType;
    }

    public String getSummary() {
        return summary;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    public String getTaskType() {
        return taskType;
    }

    public boolean isImageTask() {
        return "image".equals(taskType);
    }
}