package com.example.mychat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    private DatabaseReference chatDataBase;
    private DatabaseReference userDataBase;
    private FirebaseAuth mAuth;
    private String current_user;
    private RecyclerView chat_list;
    private View mainView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_chats, container, false);

        chat_list = (RecyclerView) mainView.findViewById(R.id.chat_list);
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser().getUid();
        chatDataBase = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user);
        chatDataBase.keepSynced(true);
        userDataBase = FirebaseDatabase.getInstance().getReference("Users");

        chat_list.setHasFixedSize(true);
        chat_list.setLayoutManager(new LinearLayoutManager(getContext()));
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Chat, ChatsViewHolder> chatsRecycleAdapter = new FirebaseRecyclerAdapter<Chat, ChatsViewHolder>(Chat.class, R.layout.users_single_layout, ChatsViewHolder.class, chatDataBase) {
            @Override
            protected void populateViewHolder(ChatsViewHolder chatsViewHolder, Chat chats, int i) {
                String list_user_id = getRef(i).getKey();

                userDataBase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userName = snapshot.child("name").getValue().toString();
                        String userThumbImage = snapshot.child("thumb_image").getValue().toString();
                        String connection_status = snapshot.child("online").getValue().toString();

                        chatsViewHolder.setLastMessage("Send a message to " + userName);
                        chatsViewHolder.setName(userName);
                        chatsViewHolder.setThumbImage(userThumbImage);
                        chatsViewHolder.setConnectionStatus(connection_status);

                        chatsViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                        startActivity(new Intent(getContext(), ChatActivity.class)
                                .putExtra("userId",list_user_id)
                                .putExtra("userName", userName)
                                .putExtra("userThumbImage", userThumbImage));

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        };

        chat_list.setAdapter(chatsRecycleAdapter);
    }
}

