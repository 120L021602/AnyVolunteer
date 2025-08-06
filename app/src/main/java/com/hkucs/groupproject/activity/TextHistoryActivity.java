package com.hkucs.groupproject.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hkucs.groupproject.ChatSummary;
import com.hkucs.groupproject.adapter.TextHistoryAdapter;
import com.hkucs.groupproject.R;
import com.hkucs.groupproject.database.ChatHistoryManager;

import java.util.List;

public class TextHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistoryList;
    private List<ChatSummary> historyList;
    private TextHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_history);

        rvHistoryList = findViewById(R.id.rvHistoryList);
        rvHistoryList.setLayoutManager(new LinearLayoutManager(this));

        // Load history records from database
        ChatHistoryManager chatHistoryManager = new ChatHistoryManager(this);
        historyList = chatHistoryManager.getChatHistory();

        // Set adapter
        adapter = new TextHistoryAdapter(this, historyList);
        rvHistoryList.setAdapter(adapter);
        Log.d("DEBUG", "History count = " + historyList.size());
    }
}