package com.hkucs.groupproject;

public class ChatMessage {
    public enum Sender {
        USER, LLM
    }

    private String content;
    private Sender sender;
    private String imagePath;

    // 构造函数（文本消息）
    public ChatMessage(String content, Sender sender) {
        this.content = content;
        this.sender = sender;
        this.imagePath = null;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // 构造函数（带图片）
    public ChatMessage(String content, Sender sender, String imagePath) {
        this.content = content;
        this.sender = sender;
        this.imagePath = imagePath;
    }

    public String getContent() {
        return content;
    }

    public Sender getSender() {
        return sender;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }
}