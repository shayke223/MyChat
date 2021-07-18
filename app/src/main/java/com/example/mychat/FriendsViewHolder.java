package com.example.mychat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This class has a view and the functions to update the User that we are bringing to the recycle view
 *
 */
public class FriendsViewHolder extends RecyclerView.ViewHolder {

    View view;

    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

//    public void setDate(String date) {
//        TextView userDateView = (TextView) view.findViewById(R.id.user_single_status);
//        userDateView.setText(date);
//    }
    public void setStatus(String status) {
        TextView userStatus = (TextView) view.findViewById(R.id.user_single_status);
        userStatus.setText(status);
    }
    public void setName(String name) {
        TextView userNameView = (TextView) view.findViewById(R.id.user_single_name);
        userNameView.setText(name);
    }
    public void setConnectionStatus(String connectionStatus) {
        TextView userConnectionStatus = (TextView) view.findViewById(R.id.user_single_connection);
        userConnectionStatus.setText((connectionStatus.equals("true"))?"Online":"Offline");

        if(connectionStatus.equals("true"))
            userConnectionStatus.setTextColor(Color.parseColor("#9ADF6F"));
        else userConnectionStatus.setTextColor(Color.parseColor("#9B9B9B"));

    }

    public void setThumbImage(String thumbImage) {
        CircleImageView imageView = (CircleImageView) view.findViewById(R.id.user_single_image);
        /**
         *  Use the image offline
         */
        Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(imageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(thumbImage).placeholder(R.drawable.profile).into(imageView);
            }
        });
    }

}
