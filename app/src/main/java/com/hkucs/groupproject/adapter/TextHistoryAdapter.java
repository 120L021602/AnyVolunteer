package com.hkucs.groupproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hkucs.groupproject.ChatSummary;
import com.hkucs.groupproject.R;
import com.hkucs.groupproject.activity.TextTaskDetailActivity;


import java.util.List;

public class TextHistoryAdapter extends RecyclerView.Adapter<TextHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<ChatSummary> historyList;

    public TextHistoryAdapter(Context context, List<ChatSummary> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_summary, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatSummary item = historyList.get(position);
        holder.tvSummary.setText(item.getSummary());
        holder.tvTimestamp.setText(item.getTimestamp());

        // Click to navigate to ChatDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TextTaskDetailActivity.class);
            intent.putExtra("chat_id", item.getChatId()); // Pass chatId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSummary, tvTimestamp;
        ImageView ivPreview; // 新增图片预览

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

        }
    }
}