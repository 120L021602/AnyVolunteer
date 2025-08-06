package com.hkucs.groupproject.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hkucs.groupproject.adapter.ChatAdapter;
import com.hkucs.groupproject.ChatMessage;
import com.hkucs.groupproject.R;
import com.hkucs.groupproject.database.ChatHistoryManager;

import java.util.ArrayList;
import java.util.List;

public class TextTaskDetailActivity extends AppCompatActivity {

    private RecyclerView rvChatDetail;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_task_detail);

        String chatId = getIntent().getStringExtra("chat_id");

        rvChatDetail = findViewById(R.id.rvChatDetail);
        rvChatDetail.setLayoutManager(new LinearLayoutManager(this));

        // Load chat history from database
        chatMessages = loadChatMessagesFromDatabase(chatId);

        chatAdapter = new ChatAdapter(chatMessages);
        rvChatDetail.setAdapter(chatAdapter);
    }

    private List<ChatMessage> loadChatMessagesFromDatabase(String chatId) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatHistoryManager chatHistoryManager = new ChatHistoryManager(this);

        // Get the stored complete chat history string
        String chatContent = chatHistoryManager.getChatContentById(chatId);

        if (chatContent != null) {
            // Try to split the entire message
            String[] parts = chatContent.split("\\|");
            if (parts.length >= 4) {
                // Extract user original message
                String userMessage = parts[0].trim();
                if (userMessage.startsWith("User message:")) {
                    userMessage = userMessage.substring("User message:".length()).trim();
                    messages.add(new ChatMessage("User message: " +userMessage, ChatMessage.Sender.USER));
                }

                // Extract desensitized content
                String desensitizedContent = parts[1].trim();
                if (desensitizedContent.startsWith("Desensitized content:")) {
                    desensitizedContent = desensitizedContent.substring("Desensitized content:".length()).trim();
                    messages.add(new ChatMessage("Desensitized content: " + desensitizedContent, ChatMessage.Sender.USER));
                }

                // Extract LLM reply
                String llmReply = parts[2].trim();
                if (llmReply.startsWith("LLM's reply:")) {
                    llmReply = llmReply.substring("LLM's reply:".length()).trim();
                    messages.add(new ChatMessage("LLM's reply: " + llmReply, ChatMessage.Sender.LLM));
                }

                // Extract restored information
                String restoredInfo = parts[3].trim();
                if (restoredInfo.startsWith("Restored information:")) {
                    restoredInfo = restoredInfo.substring("Restored information:".length()).trim();
                    messages.add(new ChatMessage("Restored information: " + restoredInfo, ChatMessage.Sender.LLM));
                }
            } else {
                // If format doesn't match, at least show original content
                messages.add(new ChatMessage("Message " + chatContent, ChatMessage.Sender.USER));
            }
        }
        return messages;
    }
}