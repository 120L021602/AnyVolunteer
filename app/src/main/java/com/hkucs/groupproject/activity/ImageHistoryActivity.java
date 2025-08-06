package com.hkucs.groupproject.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hkucs.groupproject.R;
import com.hkucs.groupproject.adapter.ImageHistoryAdapter;
import com.hkucs.groupproject.database.ImageTask;
import com.hkucs.groupproject.database.ImageTaskManager;
import com.hkucs.groupproject.database.UserManager;

import java.util.List;

public class ImageHistoryActivity extends AppCompatActivity {

    private RecyclerView rvImageHistory;
    private ImageHistoryAdapter adapter;
    private List<ImageTask> imageTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_history);

        rvImageHistory = findViewById(R.id.rvImageHistory);
        rvImageHistory.setLayoutManager(new LinearLayoutManager(this));

        // Load image tasks from database
        String userId = new UserManager(this).getCurrentUserId();
        ImageTaskManager imageTaskManager = new ImageTaskManager(this);
        imageTasks = imageTaskManager.getImageTasksByUser(userId);

        adapter = new ImageHistoryAdapter(this, imageTasks);
        rvImageHistory.setAdapter(adapter);

        Log.d("DEBUG", "Loaded image tasks: " + imageTasks.size());
    }
}
