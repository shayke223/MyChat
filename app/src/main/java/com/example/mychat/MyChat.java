package com.example.mychat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class MyChat extends Application {

    private DatabaseReference userDataBase;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {

        super.onCreate();
        /**
         * Enable offline activity
         */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser current_user = mAuth.getCurrentUser();
        Log.i("MyChat Activity report:", "current user successfully");
        if(current_user == null)
        {
            Log.i("MyChat Activity report:", "No current user!");
            return;
        }
        userDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid());
        rootRef = FirebaseDatabase.getInstance().getReference();
        /**
         * Actions for Disconnect and Connect
         */
        userDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot != null)
                {
                    userDataBase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
