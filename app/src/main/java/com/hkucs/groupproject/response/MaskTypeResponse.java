package com.hkucs.groupproject.response;

public class MaskTypeResponse extends Response{

    private String maskType;
    private String interpretation;
    private float score;

    public MaskTypeResponse() {
        super();
        this.score = Float.POSITIVE_INFINITY;
        this.interpretation = "";
        this.maskType = "";

    }

    public MaskTypeResponse(String message, Boolean success) {
        super();
    }

    public MaskTypeResponse(String message, Boolean success, float score, String interpretation, String maskType) {
        super(message, success);
        this.maskType = maskType;
    }

    public String getMaskType() {
        return maskType;
    }

    public void setMaskType(String maskType) {
        this.maskType = maskType;
    }
}
