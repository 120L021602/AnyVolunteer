package com.hkucs.groupproject.model;

import static com.hkucs.groupproject.config.Config.service;

import com.hkucs.groupproject.response.LlmResponse;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionContentPart;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;

import java.util.ArrayList;
import java.util.List;

public class DoubaoVisionPro32k extends Model implements Chat{

    public DoubaoVisionPro32k() {
        this.modelId = "doubao-vision-pro-32k-241028";
        this.modelName = "doubao-vision-pro-32k";
        this.totalTokenConsumed = 0;
    }

    @Override
    public LlmResponse chat(String base64Image, String task) {
        LlmResponse llmResponse = new LlmResponse();
        List<ChatCompletionContentPart> multiParts = new ArrayList<>();
        multiParts.add(ChatCompletionContentPart.builder().type("text").text(
                task
        ).build());

        multiParts.add(ChatCompletionContentPart.builder().type("image_url").imageUrl(
                new ChatCompletionContentPart.ChatCompletionContentPartImageURL(
                        "data:image/jpg;base64," + base64Image
                )
        ).build());

        ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER)
                .multiContent(multiParts).build();
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(modelId)
                .messages(chatMessages)
                .build();

        System.out.println("Sending request.");

        var response = service.createChatCompletion(chatCompletionRequest);

        System.out.println("Response received.");
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            llmResponse.setSuccess(true);
            String replyContent = (String) response.getChoices().get(0).getMessage().getContent();
            System.out.println(replyContent);
            llmResponse.setReply(replyContent);
        }
        else {
            return new LlmResponse("Something wrong, the response of llm is invalid!", false);
        }

        return llmResponse;
    }

}
