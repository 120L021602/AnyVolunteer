package com.hkucs.groupproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hkucs.groupproject.R;
import com.hkucs.groupproject.activity.ImageTaskDetailActivity;
import com.hkucs.groupproject.database.ImageTask;


import java.util.List;

public class ImageHistoryAdapter extends RecyclerView.Adapter<ImageHistoryAdapter.ViewHolder> {

    private Context context;
    private List<ImageTask> imageTasks;

    public ImageHistoryAdapter(Context context, List<ImageTask> imageTasks) {
        this.context = context;
        this.imageTasks = imageTasks;
    }

    @NonNull
    @Override
    public ImageHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_history, parent, false);
        return new ImageHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHistoryAdapter.ViewHolder holder, int position) {
        ImageTask task = imageTasks.get(position);

        holder.tvSummary.setText("Task: " + task.taskText + "\nLLM Reply: " + task.llmReply);
        holder.tvTimestamp.setText(task.timestamp);

        if (task.imagePathOriginal != null) {
            holder.ivImage.setImageBitmap(BitmapFactory.decodeFile(task.imagePathOriginal));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageTaskDetailActivity.class);
            intent.putExtra("chat_id", task.chatId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageTasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSummary, tvTimestamp;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}
