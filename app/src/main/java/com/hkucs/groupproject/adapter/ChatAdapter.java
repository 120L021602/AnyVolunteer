package com.hkucs.groupproject.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hkucs.groupproject.ChatMessage;
import com.hkucs.groupproject.R;

import java.io.File;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatList;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);

        if (message.getSender() == ChatMessage.Sender.USER) {
            if (!message.getContent().isEmpty()) {
                holder.tvRightMessage.setVisibility(View.VISIBLE);
                holder.tvRightMessage.setBackgroundResource(R.drawable.bg_user_bubble);
                holder.tvRightMessage.setText(message.getContent());
            } else {
                holder.tvRightMessage.setVisibility(View.GONE);
                holder.tvRightMessage.setBackground(null);
                // hide the height of the TextView to 0
                holder.tvRightMessage.getLayoutParams().height = 0;
//               holder.tvRightMessage.requestLayout();
            }
            // user's messages
            holder.tvRightMessage.setVisibility(View.VISIBLE);
            holder.tvRightMessage.setText(message.getContent());

            if (message.hasImage()) {
                holder.imgRightImage.setVisibility(View.VISIBLE);
                holder.imgRightImage.setImageURI(Uri.fromFile(new File(message.getImagePath())));
            } else {
                holder.imgRightImage.setVisibility(View.GONE);
            }

            holder.tvLeftMessage.setVisibility(View.GONE);

        } else {
            // LLM's messages
            holder.tvLeftMessage.setVisibility(View.VISIBLE);
            holder.tvLeftMessage.setText(message.getContent());

            holder.tvRightMessage.setVisibility(View.GONE);
            holder.imgRightImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeftMessage, tvRightMessage;
        ImageView imgRightImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeftMessage = itemView.findViewById(R.id.tvLeftMessage);
            tvRightMessage = itemView.findViewById(R.id.tvRightMessage);
            imgRightImage = itemView.findViewById(R.id.imgRightImage);
        }
    }
}