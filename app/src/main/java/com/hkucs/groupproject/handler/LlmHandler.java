package com.hkucs.groupproject.handler;

import com.hkucs.groupproject.config.Config;
import com.hkucs.groupproject.response.LlmResponse;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import android.util.Log;

import java.util.*;
import java.util.regex.*;

public class LlmHandler {
    private static ArkService service = Config.service;

    private static final String desensitizationTask = "My input consists of two parts: the first part is content containing private information, and the second part is instructions. Please respond to the instructions based on the content I provide. Format your response as follows: The first paragraph should be your response to my instructions, the second paragraph can include any additional information or context you want to provide. Only place line breaks between these paragraphs.";

    public static LlmResponse sendRequestAndGetResponse(String modelId, String message) {
        LlmResponse llmResponse = new LlmResponse();

        // Local desensitization processing
        Map<String, String> sensitiveInfoMap = new HashMap<>();
        String desensitizedMessage = desensitizeMessage(message, sensitiveInfoMap);

        // Construct request content
        List<ChatMessage> messagesForReqList = new ArrayList<>();
        ChatMessage elementForMessagesForReqList0 =
                ChatMessage.builder()
                        .role(ChatMessageRole.USER)
                        .content(desensitizedMessage + desensitizationTask)
                        .build();
        messagesForReqList.add(elementForMessagesForReqList0);

        ChatCompletionRequest req =
                ChatCompletionRequest.builder()
                        .model(modelId)
                        .messages(messagesForReqList)
                        .build();

        // Call LLM service
        var response = service.createChatCompletion(req);
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            llmResponse.setSuccess(true);
            var usage = response.getUsage();
            llmResponse.setTokenConsumed((int) usage.getTotalTokens());

            // Parse returned content
            String reply = (String) response.getChoices().get(0).getMessage().getContent();

            Log.d("LLM_RESPONSE", "Raw reply from LLM: " + reply);

            String[] parts;
            if (modelId.equals("deepseek-r1-250120")){
                parts = reply.split("---", 2);
            }
            else{
                parts = reply.split("\n", 2); // Split into two parts by newline
            }

            // Apply three-part display in client
            llmResponse.setReply(desensitizedMessage); // First part: desensitized text
            if (parts.length > 0) {
                llmResponse.setAdditionalInfo(parts[0]); // Second part: reply to instructions

                // Third part: restore sensitive information
                String restoredInfo = restoreSensitiveInfo(parts[0], sensitiveInfoMap);
                llmResponse.setRestoredInfo(restoredInfo);
            } else {
                return new LlmResponse("Invalid response format!", false);
            }
        } else {
            return new LlmResponse("Something wrong, the response of llm is invalid!", false);
        }

        // Close service after request ends
        service.shutdownExecutor();

        Log.d("LLM_RESPONSE_FULL",
                "Success: " + llmResponse.getSuccess() +
                        " | Token: " + llmResponse.getTokenConsumed() +
                        " | Reply (脱敏): " + llmResponse.getReply() +
                        " | Additional Info: " + llmResponse.getAdditionalInfo() +
                        " | Restored Info: " + llmResponse.getRestoredInfo()
        );


        return llmResponse;
    }

    // Use regular expressions for sensitive information desensitization
    private static String desensitizeMessage(String message, Map<String, String> sensitiveInfoMap) {
        StringBuilder desensitizedText = new StringBuilder(message);

        // Define regular expression patterns for various sensitive information
        Map<String, Pattern> patterns = new HashMap<>();

        // Mainland China mobile phone number
        patterns.put("PHONE_CN", Pattern.compile("1[3-9]\\d{9}"));

        // Landline phone
        patterns.put("PHONE_FIXED", Pattern.compile("(?:0\\d{2,3}[-\\s]?)?\\d{7,8}"));

        // Hong Kong mobile phone number
        patterns.put("PHONE_HK", Pattern.compile("(?:\\+852\\s?|852\\s?)?[5-69]\\d{7}"));

        // Email
        patterns.put("EMAIL", Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"));

        // Date
        patterns.put("DATE", Pattern.compile("\\d{4}[年/\\-.]\\d{1,2}[月/\\-.]\\d{1,2}日?|\\d{1,2}[月/\\-.]\\d{1,2}[日/\\-.](\\d{4})?"));

        // Mainland China ID card
        patterns.put("ID_CARD_CN", Pattern.compile("[1-9]\\d{5}(?:19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]"));

        // Hong Kong ID card
        patterns.put("ID_CARD_HK", Pattern.compile("[A-Z]{1,2}\\d{6}\\(\\w\\)"));

        // Bank card number
        patterns.put("BANK_CARD", Pattern.compile("(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9]{2})[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})"));

        // Chinese name
        patterns.put("NAME_CN", Pattern.compile("[\\u4e00-\\u9fa5]{2,4}(?:先生|女士|同志)"));

        // English name (must have titles like Mr/Ms/Mrs/Dr)
        patterns.put("NAME_EN", Pattern.compile("[A-Z][a-z]+(?: [A-Z][a-z]+)*(?: (?:Mr|Ms|Mrs|Miss|Dr|Prof|Sir))"));

        // Address
        patterns.put("ADDRESS", Pattern.compile("[\\u4e00-\\u9fa5]{2,}(?:路|街|道|区|号|楼|大厦)(?:[0-9０-９]+号?)"));

        // Company name
        patterns.put("COMPANY", Pattern.compile("[\\u4e00-\\u9fa5]{2,}(?:公司|集团|企业|有限责任|股份)"));
        // Process each pattern and replace matches
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            String type = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(desensitizedText);

            // Use StringBuffer to avoid matching errors caused by position changes
            StringBuffer sb = new StringBuffer();
            int count = 0;

            while (matcher.find()) {
                String match = matcher.group();
                String placeholder = "{" + type + count++ + "}";
                sensitiveInfoMap.put(placeholder, match);
                matcher.appendReplacement(sb, placeholder);
            }
            matcher.appendTail(sb);
            desensitizedText = new StringBuilder(sb.toString());
        }

        return desensitizedText.toString();
    }

    // Restore sensitive information
    private static String restoreSensitiveInfo(String text, Map<String, String> sensitiveInfoMap) {
        String restoredText = text;
        for (Map.Entry<String, String> entry : sensitiveInfoMap.entrySet()) {
            String placeholder = entry.getKey();
            String originalValue = entry.getValue();
            restoredText = restoredText.replace(placeholder, originalValue);
        }
        return restoredText;
    }
}