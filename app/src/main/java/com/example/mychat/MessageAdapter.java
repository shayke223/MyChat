package com.example.mychat;

import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<Messages> messageList;
    private FirebaseAuth mAuth;
    private String chatUserThumbImage;
    private RelativeLayout rl;

    public MessageAdapter(List<Messages> messageList,String chatUserThumbImage)
    {
        this.messageList = messageList;
        this.chatUserThumbImage = chatUserThumbImage;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        rl = (RelativeLayout)v.findViewById(R.id.message_single_layout);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        if(mAuth == null)
            return;
        String current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = messageList.get(position);
        String from_user = c.getFrom();

        /**
         *Check if i've sent the message or the other guy
         */
        if(from_user != null && from_user.equals(current_user_id))
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.BLACK);
//            rl.setGravity(Gravity.RIGHT);
//            holder.profileImage.setVisibility(View.INVISIBLE);

        }
        else
        {
//            Picasso.get().load(chatUserThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(holder.profileImage);
            holder.messageText.setBackgroundResource(R.drawable.message_text_background2);
            holder.messageText.setTextColor(Color.WHITE);
//            rl.setGravity(Gravity.LEFT);


        }

        holder.messageText.setText(c.getMessage());
//        holder.timeText.setText(c.getTime());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

}
