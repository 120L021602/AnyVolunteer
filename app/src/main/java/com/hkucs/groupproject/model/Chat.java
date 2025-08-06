package com.hkucs.groupproject.model;

import com.hkucs.groupproject.response.LlmResponse;

public interface Chat {
    public LlmResponse chat(String base64Image, String task);
}
