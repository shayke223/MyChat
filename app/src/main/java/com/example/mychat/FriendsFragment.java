package com.example.mychat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {
    private DatabaseReference friendsDataBase;
    private DatabaseReference userDataBase;
    private FirebaseAuth mAuth;
    private String current_user;
    private RecyclerView friend_list;
    private View mainView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friend_list = (RecyclerView) mainView.findViewById(R.id.friend_list);
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser().getUid();
        friendsDataBase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user);
        friendsDataBase.keepSynced(true);
        userDataBase = FirebaseDatabase.getInstance().getReference("Users");

        friend_list.setHasFixedSize(true);
        friend_list.setLayoutManager(new LinearLayoutManager(getContext()));
        return mainView;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend, FriendsViewHolder> friendsRecycleAdapter = new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(Friend.class, R.layout.users_single_layout, FriendsViewHolder.class, friendsDataBase) {
            @Override
            protected void populateViewHolder(FriendsViewHolder friendsViewHolder, Friend friends, int i) {
//                friendsViewHolder.setDate(friends.getDate());
                String list_user_id = getRef(i).getKey();
                userDataBase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userName = snapshot.child("name").getValue().toString();
                        String status = snapshot.child("status").getValue().toString();
                        String userThumbImage = snapshot.child("thumb_image").getValue().toString();
                        String connection_status = snapshot.child("online").getValue().toString();

                        friendsViewHolder.setStatus(status);
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setThumbImage(userThumbImage);
                        friendsViewHolder.setConnectionStatus(connection_status);

                        friendsViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which)
                                        {
                                            case 0:
                                                startActivity(new Intent(getContext(), ProfileActivity.class).putExtra("userId",list_user_id));
                                                break;

                                            case 1:
                                                startActivity(new Intent(getContext(), ChatActivity.class)
                                                        .putExtra("userId",list_user_id)
                                                        .putExtra("userName", userName)
                                                        .putExtra("userThumbImage", userThumbImage));
                                                break;

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        friend_list.setAdapter(friendsRecycleAdapter);
    }
}