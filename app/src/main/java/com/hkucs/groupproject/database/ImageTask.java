package com.hkucs.groupproject.database;

public class ImageTask {
    public String chatId;
    public String taskText;
    public String imagePathOriginal;
    public String imagePathProcessed;
    public String llmReply;

    public String timestamp;

    public String userId;

    public ImageTask(String chatId, String taskText, String imagePathOriginal, String imagePathProcessed, String llmReply, String timestamp, String userId) {
        this.chatId = chatId;
        this.taskText = taskText;
        this.imagePathOriginal = imagePathOriginal;
        this.imagePathProcessed = imagePathProcessed;
        this.llmReply = llmReply;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public ImageTask() {}
}
