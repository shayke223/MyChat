package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

/**
 * An activity which has a recycle viewer, it means we have a bunch of views (View = represent the user and his picture)
 * and we can represent it as a list view.(Scroll down)
 */
public class UserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView userList;
    private DatabaseReference usersDataBase;
    private FirebaseAuth mAuth;
    private String current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user2);

        toolbar = (Toolbar)findViewById(R.id.users_appBar);
        userList = (RecyclerView)findViewById(R.id.users_list);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersDataBase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser().getUid();
        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     * Creating an Adapter for the recycler.
     * The adapter should use the User attributes, The layout of each user and the view holder.
     */
    @Override
    protected void onStart() {
        super.onStart();
        /**
         * Adapter helps us to control the recycle viewer.
         * We just need to give it the Database information.
         * In this case we gave it userDataBase which contains all users.
         */
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class, R.layout.users_single_layout, UsersViewHolder.class, usersDataBase) {
            /**
             *
             * @param userViewHolder
             * @param users
             * @param position Determine the position of our mouse on the view. By using getRef(position) we will get the reference of the view.
             *
             */
            @Override
            protected void populateViewHolder(UsersViewHolder userViewHolder, Users users, int position) {

                String userId = getRef(position).getKey();
                usersDataBase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userViewHolder.setName(users.getName());
                        userViewHolder.setStatus(users.getStatus());
                        userViewHolder.setThumbImage(users.getThumb_image());
                        String connection_status = snapshot.child("online").getValue().toString();
                        userViewHolder.setConnectionStatus(connection_status);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                userViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(UserActivity.this, ProfileActivity.class).putExtra("userId" , userId));

                    }
                });
            }
        };
        //Setting the adapter to the recycle that we have created
        userList.setAdapter(firebaseRecyclerAdapter);
    }

    }
