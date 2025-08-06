package com.hkucs.groupproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.hkucs.groupproject.R;

public class HistoryNavigateActivity extends AppCompatActivity {

    private Button btnTextHistory, btnImageHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_navigate);

        btnTextHistory = findViewById(R.id.btnTextHistory);
        btnImageHistory = findViewById(R.id.btnImageHistory);

        btnTextHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, TextHistoryActivity.class);
            startActivity(intent);
        });

        btnImageHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageHistoryActivity.class);
            startActivity(intent);
        });
    }
}