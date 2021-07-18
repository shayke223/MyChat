package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    //Views
    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileStatus;
    private TextView profileFriendsCount;
    //Buttons
    private Button profileSendRequestBtn;
    private Button profileRejectRequestBtn;
    //Database References
    private DatabaseReference friendsDataBase;
    private DatabaseReference userDataBase;
    private DatabaseReference friendsRequestDataBase;
    private DatabaseReference notificationDataBase;
    //Others
    private ProgressDialog progressDialog;
    private FirebaseUser current_user;
    private Toolbar ToolBar;

    /**
     * 0 - Not Friends
     * 1 - Request Sent
     * 2 - Request received
     * 3 - Friends, Can delete now
     */
    private int current_state;

    /**
     * Main Event
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Get the id of the profile from the last intent
        String profileUserId = getIntent().getStringExtra("userId");
        initiateVariables();

        userDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String display_name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();

                profileName.setText(display_name);
                profileStatus.setText(status);
                if (!image.toString().equals("default"))
                    Picasso.get().load(image).placeholder(R.drawable.download_image).into(profileImage);


                /**
                 *  Friend List / Request Feature
                 *  First we check if the current user is existed in the Friends Request table
                 *  If it does it will reference it in the snapshot
                 *  From the snapshot we will take the value of the friend
                 */
                friendsRequestDataBase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        updateAddButton(snapshot, profileUserId);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              profileSendRequestBtn.setEnabled(false);

                /**
                 * Not Friends
                 * Use "/" to make a child.
                 */
                if (current_state == 0)
                    sendFriendRequestToProfile(profileUserId);

                /**
                 * Cancel a request
                 * CurrentState 1 is Request Sent
                 */
                if (current_state == 1)
                    removeFriendRequest(profileUserId,0);

                /**
                 * Request Received, Choose if you want to accept the request.
                 */
                if (current_state == 2)
                    acceptFriendRequestFromProfile(profileUserId);

                /**
                 * Already friends, Remove the friend here
                 */
                if (current_state == 3) {
                    removeFriend(profileUserId);
                }

            }
        });

        profileRejectRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFriendRequest(profileUserId,0);
            }
        });

        /**
         * Can't send friend request to my self.
         */
        if(current_user.getUid().toString().equals(profileUserId))
            profileSendRequestBtn.setVisibility(View.INVISIBLE);

    }
/**
//-------------------------------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------Functions---------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------------------------------
**/

    /**
     * Get a profile ID and delete friend from friend list, update tables on Database
     * @param profileUserId
     */
    private void removeFriend(String profileUserId) {
        friendsDataBase.child(current_user.getUid()).child(profileUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    enableAddFriend();
                }
            }
        });
        friendsDataBase.child(profileUserId).child(current_user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                   // enableAddFriend();
                }
            }
        });
    }

    /**
     * Get a profile ID and accept friend's request, update tables on Database
     * @param profileUserId
     */
    private void acceptFriendRequestFromProfile(String profileUserId) {
        String current_date = DateFormat.getDateTimeInstance().format(new Date());
        friendsDataBase.child(current_user.getUid()).child(profileUserId).child("date").setValue(current_date).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                friendsDataBase.child(profileUserId).child(current_user.getUid()).child("date").setValue(current_date).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        removeFriendRequest(profileUserId,1);
                    }
                });
            }
        });
    }

    /**
     * Get a profile ID and send a friends request, update tables on Database
     * @param profileUserId
     */
    private void sendFriendRequestToProfile(String profileUserId) {
        Map requestMap = new HashMap();
        requestMap.put( current_user.getUid() + "/" + profileUserId +  "/request_type","sent");
        requestMap.put( profileUserId + "/" + current_user.getUid() +  "/request_type","received");
        friendsRequestDataBase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                HashMap<String,String> notificationData = new HashMap<>();
                notificationData.put("from",current_user.getUid());
                notificationData.put("type", "request");
                notificationDataBase.child(profileUserId).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        changeAddFriendToCancelRequest();
                    }
                });
            }
        });
    }

    /**
     * Decide what to do with the Add Button based on the status of the friend request.
     * @param snapshot
     * @param profileUserId
     */
    private void updateAddButton(@NonNull DataSnapshot snapshot, String profileUserId) {
        if(snapshot.hasChild(profileUserId))
        {
            String request_type = snapshot.child(profileUserId).child("request_type").getValue().toString();

            if(request_type.equals("received"))
                changeAddFriendToAcceptFriend();

            if(request_type.equals("sent"))
                changeAddFriendToCancelRequest();

        }
        else
            checkIfAlreadyFriends(profileUserId);
        progressDialog.dismiss();
    }

    /**
     * Initiate all the values.
     */
    private void initiateVariables() {

        /**
         * Adding Tool bar with title and option to go back
         * Where to going back written in Manifest
         */
        ToolBar = (Toolbar)findViewById(R.id.profile_page_toolbar);
        setSupportActionBar(ToolBar);
        getSupportActionBar().setTitle("View Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String profileUserId = getIntent().getStringExtra("userId");
        userDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(profileUserId);
        friendsRequestDataBase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendsDataBase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDataBase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        current_user = FirebaseAuth.getInstance().getCurrentUser();

        profileName = (TextView) findViewById(R.id.profile_name);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        profileStatus = (TextView) findViewById(R.id.profile_status);
        profileFriendsCount = (TextView) findViewById(R.id.profile_friends_counter);
        profileSendRequestBtn = (Button) findViewById(R.id.add_friends_btn);
        profileRejectRequestBtn = (Button) findViewById(R.id.reject_friend_btn);

        current_state = 0; // Not friends

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Information");
        progressDialog.setMessage("Please wait while we are loading user's data");
        progressDialog.show();
    }

    /**
     * If there is no friend requests, we Should check if we are already friends
     * and If we are, Set remove option.
     */
    private void checkIfAlreadyFriends(String profileUserId) {
        friendsDataBase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(profileUserId))    //If true it means we are friends
                    changeAddFriendToRemoveFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * The reason why we use reasonOfRemove is to decide how the "AddFriend" button will look at the end
     * If the reason was to cancel the request that we sent, then the button will be like it was before
     * if the reason was accepting the request, the button will offer the option to unfriend.
     * @param profileUserId
     * @param reasonOfRemove 0 for canceling request, 1 for approve request.
     */
    private void removeFriendRequest(String profileUserId, int reasonOfRemove) {
        friendsRequestDataBase.child(current_user.getUid()).child(profileUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendsRequestDataBase.child(profileUserId).child(current_user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(reasonOfRemove == 0)
                                enableAddFriend();
                            if(reasonOfRemove == 1)
                                changeAddFriendToRemoveFriends();
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Can add friend in this situation
     */
    public void enableAddFriend(){
        profileSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.orange));
        profileSendRequestBtn.setEnabled(true);
        current_state = 0; // "No friends"
        profileSendRequestBtn.setText("Add Friend");
        profileRejectRequestBtn.setVisibility(View.INVISIBLE);
    }

    /**
     * Change the "Add Friend" button to "Cancel Request"
     */
    public void changeAddFriendToCancelRequest(){
        profileSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.grey));
        profileSendRequestBtn.setEnabled(true);
        current_state = 1; // "request sent"
        profileSendRequestBtn.setText("Cancel Friend Request");
        profileRejectRequestBtn.setVisibility(View.INVISIBLE);
    }

    /**
     * Someone sent me a friend request:
     * If a user sent us a friend request, we can Accept it instead of sending a request back to him.
     * There is an option to reject too.
     */
    public void changeAddFriendToAcceptFriend(){
        profileSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.dark_orange));
        profileSendRequestBtn.setEnabled(true);
        current_state = 2; // "request received"
        profileSendRequestBtn.setText("Accept Friend Request");
        profileRejectRequestBtn.setVisibility(View.VISIBLE);

    }
    /**
     * Already friends, Can remove friends now
     */
    public void changeAddFriendToRemoveFriends(){
        profileSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.grey));
        profileSendRequestBtn.setEnabled(true);
        current_state = 3; // "friends"
        profileSendRequestBtn.setText("Remove Friend");
        profileRejectRequestBtn.setVisibility(View.INVISIBLE);

    }
}