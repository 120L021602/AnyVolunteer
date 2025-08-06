package com.hkucs.groupproject.response;

public class LlmResponse extends Response {
    private String reply; // First part: desensitized text
    private String additionalInfo; // Second part: reply to instructions
    private String restoredInfo; // Third part: information restored on device side
    private Integer tokenConsumed;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getRestoredInfo() {
        return restoredInfo;
    }

    public void setRestoredInfo(String restoredInfo) {
        this.restoredInfo = restoredInfo;
    }

    public Integer getTokenConsumed() {
        return tokenConsumed;
    }

    public void setTokenConsumed(Integer tokenConsumed) {
        this.tokenConsumed = tokenConsumed;
    }

    public LlmResponse() {
        super();
        this.reply = "";
        this.additionalInfo = "";
        this.restoredInfo = "";
        this.tokenConsumed = 0;
    }

    public LlmResponse(String message, Boolean success) {
        super(message, success);
        this.reply = "";
        this.additionalInfo = "";
        this.restoredInfo = "";
        this.tokenConsumed = 0;
    }
}