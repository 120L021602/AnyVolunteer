package com.hkucs.groupproject.response;

public class Response {
    private String message;
    private Boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Response() {
        this.message = "failed";
        this.success = false;
    }

    public Response(String message, Boolean success) {
        this.message = message;
        this.success = success;
    }
}
