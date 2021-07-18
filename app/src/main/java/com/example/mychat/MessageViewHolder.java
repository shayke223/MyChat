package com.example.mychat;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView messageText;
    public CircleImageView profileImage;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        messageText = (TextView)itemView.findViewById(R.id.message_text_layout);
        profileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_layout);
    }

}
