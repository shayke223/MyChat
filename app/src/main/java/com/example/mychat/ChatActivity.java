package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUserId,chatUserName, chatUserThumbImage;
    private Toolbar toolbar;
    private DatabaseReference rootRef;
    private TextView titleView;
    private TextView lastSeenView;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;

    private ImageButton chatAddBtn;
    private ImageButton chatSendBtn;
    private TextInputLayout chatMessageView;

    private RecyclerView messagesList;

    private final List<Messages> list_of_messages = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessageAdapter adapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

    private SwipeRefreshLayout refreshLayout;

    private int itemPos = 0;
    private String lastKey = "";
    private String prevKey = "";

    private static final int GALLERY_PICK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        chatUserId = getIntent().getStringExtra("userId");
        chatUserName = getIntent().getStringExtra("userName");
        chatUserThumbImage = getIntent().getStringExtra("userThumbImage");

        toolbar = (Toolbar)findViewById(R.id.chat_page_toolbar);

        chatAddBtn = (ImageButton)findViewById(R.id.chat_add_btn);
        chatSendBtn = (ImageButton)findViewById(R.id.chat_send_btn);
        chatMessageView = (TextInputLayout)findViewById(R.id.chat_message_view);
//        chatSendBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);


        /**
         * Control the swipe
         */
        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);

        /**
         * set the adapter
         */
        adapter = new MessageAdapter(list_of_messages,chatUserThumbImage);
        messagesList = (RecyclerView)findViewById(R.id.messages_list);

        /**
         * We need a manager for the layouts
         */
        linearLayout = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayout);
        messagesList.setAdapter(adapter);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        /**
         * Load the messages from the db
         */
        loadMessages();
        messagesList.scrollToPosition(list_of_messages.size()-1); //Go down

        /**
         * I have created another bar to put on the toolbar.
         * Need to inflate the bar with these actions.
         * Name of the bar on top(xml): chat_custom_bar
         */
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        /**
         * Action bar items
         */
        titleView = (TextView)findViewById(R.id.custom_bar_displayName);
        lastSeenView = (TextView)findViewById(R.id.custom_bar_lastSeen);
        profileImage = (CircleImageView)findViewById(R.id.custom_bar_image);
        titleView.setText(chatUserName);
        /**
         * Load the picture
         */
        Picasso.get().load(chatUserThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(profileImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(chatUserThumbImage).placeholder(R.drawable.profile).into(profileImage);
            }
        });

        /**
         * What time are we going to represent
         */
            rootRef.child("Users").child(chatUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String online = snapshot.child("online").getValue().toString();

                    if(online.equals("true"))
                    {
                        lastSeenView.setText("Online");
                        lastSeenView.setTextColor(Color.parseColor("#9ADF6F"));
                    }
                    else
                    {
                        long lastTime = Long.parseLong(online);
                        String lastSeenTime = TimeStampCalculator.getTimeAgo(lastTime);
                        lastSeenView.setText(lastSeenTime);
                        lastSeenView.setTextColor(Color.parseColor("#9B9B9B"));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        Map<String,Object>  chatAddMap = new HashMap<>();
        chatAddMap.put("seen",false);
        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

        DatabaseReference chatRef =  rootRef.child("Chat").child(current_user.getUid()).child(chatUserId).getRef();
        //Add to Chat
        chatRef.updateChildren(chatAddMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Checkkkkk","ssss");
            }
        });

        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                //updateChatDetails();
                v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fragment_fade_enter));
            }
        });
        chatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"),GALLERY_PICK);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPos = 0;
                loadAnotherPage();
            }
        });

    }

    /**
     * Update the database of last spoken
     */
    private void updateChatDetails(){
        Map<String,Object>  chatAddMap = new HashMap<>();
        chatAddMap.put("seen",false);
        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

        DatabaseReference chatRef =  rootRef.child("Chat").child(current_user.getUid()).child(chatUserId).getRef();
        //Add to Chat
        chatRef.updateChildren(chatAddMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Checkkkkk","ssss");
            }
        });
    }
    private void sendMessage()
    {
        String message = chatMessageView.getEditText().getText().toString();
        if(!TextUtils.isEmpty(message))
        {

            DatabaseReference user_message_push = rootRef.child("messages").child(current_user.getUid()).child(chatUserId).push();
            String push_id = user_message_push.getKey();

            DatabaseReference current_user_ref = rootRef.child("messages").child(current_user.getUid()).child(chatUserId).child(push_id);
            DatabaseReference chat_user_ref = rootRef.child("messages").child(chatUserId).child(current_user.getUid()).child(push_id);


            Map<String,Object>  chatAddMap = new HashMap<>();
            chatAddMap.put("message",message);
            chatAddMap.put("seen", false);
            chatAddMap.put("type","text");
            chatAddMap.put("time", ServerValue.TIMESTAMP);
            chatAddMap.put("from",current_user.getUid());
            chatMessageView.getEditText().setText("");


            current_user_ref.updateChildren(chatAddMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
            chat_user_ref.updateChildren(chatAddMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });

        }

    }
    private void loadMessages(){
        DatabaseReference messageRef = rootRef.child("messages").child(current_user.getUid()).child(chatUserId);
        Query messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD * currentPage);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                itemPos++;

                if(itemPos == 1)
                {
                    String message_key = snapshot.getKey();
                    lastKey = message_key;
                    prevKey = message_key;
                }

                list_of_messages.add(message);
                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                messagesList.scrollToPosition(list_of_messages.size()-1); //Go down
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void loadAnotherPage(){
        DatabaseReference messageRef = rootRef.child("messages").child(current_user.getUid()).child(chatUserId);
        /**
         * In order to prevent loading all data again and again, we use orderByKey to load from the exect point
         */
        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                String messageKey = snapshot.getKey();

                if(!prevKey.equals(messageKey))
                    list_of_messages.add(itemPos,message);
                else prevKey = lastKey;


                if(itemPos == 1)
                {
//                    String message_key = snapshot.getKey();
//                    lastKey = message_key;
                    lastKey = messageKey;
                }

                adapter.notifyDataSetChanged();
                messagesList.scrollToPosition(list_of_messages.size()-1);
                refreshLayout.setRefreshing(false);
             //   linearLayout.scrollToPositionWithOffset(TOTAL_ITEMS_TO_LOAD,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
//            final String current_user_ref = "messages"
        }
    }
}