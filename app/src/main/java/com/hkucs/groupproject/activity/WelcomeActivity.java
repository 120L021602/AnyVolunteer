package com.hkucs.groupproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.hkucs.groupproject.R;

public class WelcomeActivity extends AppCompatActivity {

    private EditText etFirstMessage;
    private Button btnStartChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        etFirstMessage = findViewById(R.id.etFirstMessage);
        btnStartChat = findViewById(R.id.btnStartChat);

        btnStartChat.setOnClickListener(v -> {
            String message = etFirstMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // Navigate to ChatDetailActivity with the first message
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.putExtra("first_message", message);
                startActivity(intent);
                finish(); // Don't return to welcome page
            }
        });
    }
}