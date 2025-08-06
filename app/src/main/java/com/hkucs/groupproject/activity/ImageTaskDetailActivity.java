package com.hkucs.groupproject.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hkucs.groupproject.R;
import com.hkucs.groupproject.database.ImageTask;
import com.hkucs.groupproject.database.ImageTaskManager;


public class ImageTaskDetailActivity extends AppCompatActivity {

    private ImageView ivImage, ivImageProcessed;
    private TextView tvUserMessage, tvLlmReply, tvTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_task_detail);

        ivImage = findViewById(R.id.ivImage);
        ivImageProcessed = findViewById(R.id.ivImageProcessed);
        tvUserMessage = findViewById(R.id.tvUserMessage);
        tvLlmReply = findViewById(R.id.tvLlmReply);
        tvTimestamp = findViewById(R.id.tvTimestamp);

        String chatId = getIntent().getStringExtra("chat_id");

        ImageTaskManager imageTaskManager = new ImageTaskManager(this);
        ImageTask task = imageTaskManager.getImageTaskByChatId(chatId);

        if (task != null) {
            if (task.imagePathOriginal != null) {
                ivImage.setImageBitmap(BitmapFactory.decodeFile(task.imagePathOriginal));
            }
            if (task.imagePathProcessed != null) {
                ivImageProcessed.setImageBitmap(BitmapFactory.decodeFile(task.imagePathProcessed));
            }
            tvUserMessage.setText("User message: " + task.taskText);
            tvLlmReply.setText("LLM reply: " + task.llmReply);
            tvTimestamp.setText("Timestamp: " + task.timestamp);
        }
    }
}