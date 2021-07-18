package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.telecom.Call;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

/**
 * Setting Activity, handle profile picture and status
 * Upload the data to the Storage and update the image URL and thumb_image URL ( Fast loading )
 */
public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private DatabaseReference userDataBase;
    private FirebaseUser currentUser;
    private CircleImageView displayImage;
    private TextView displayName;
    private TextView displayStatus;
    private Button changeStatusBtn;
    private Button changeImageBtn;
    private static final int GALLERY_PICK = 1;
    private ProgressDialog progressDialog;
    private StorageReference imageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /**
         * Find objects
         */
        displayImage =  (CircleImageView)findViewById(R.id.user_profile_pic);
        displayName =  (TextView)findViewById(R.id.user_display_name);
        displayStatus =  (TextView)findViewById(R.id.user_status);

        changeStatusBtn = (Button)findViewById(R.id.change_status_btn);
        changeImageBtn = (Button)findViewById(R.id.change_image_btn);

        imageStorage = FirebaseStorage.getInstance().getReference();
        /**
         * Adding Tool bar with title and option to go back
         * Where to going back written in Manifest
         */
        mToolBar = (Toolbar)findViewById(R.id.settings_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * retrieve the data from the database.
         * First we need to get the current User.
         * Then we want the user's ID and get the data from the "Users" table with the ID.
         */
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser.getUid();
        userDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        userDataBase.keepSynced(true); //for offline quaries

        /**
         * In case we change the DataBase, we want this commands to work.
         * It will update the information in setting activity for every change.
         */
        userDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String thumb_image = snapshot.child("thumb_image").getValue().toString();
                displayName.setText(name);
                displayStatus.setText(status);

//                if(!image.toString().equals("default"))
//                    Picasso.get().load(image).placeholder(R.drawable.download_image).into(displayImage);
                /**
                 *  Use the image offline
                 */
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(displayImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(displayImage);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        /**
         * Move to Status Activity and give it a value to pass called "key1" = statusValue.
         * In Status activity it will take the data from "key1" and use it.
         */
        changeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusValue = displayStatus.getText().toString();
                startActivity(new Intent(SettingsActivity.this, StatusActivity.class).putExtra("key1", statusValue) );
            }
        });
        /**
         * While clicking on this button, we will open the gallery
         * Create an Intent with a type and make it available to get content from there(pictures)
         */
        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"), GALLERY_PICK);
            }
        });

    }

    /**
     * After we clicked on changeImageBtn we started an activity with the function "startActivityForResult"
     * The result of this activity will be handled in this part.
     * @param requestCode request to open the gallery
     * @param resultCode if the gallery has opened safely and we got data(picture) from there, we can keep cropping.
     * @param data the picture that we took from the gallery.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check if got the picture from the gallery
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        // Check if the Request to crop the image has approved
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Again, check if the picture cropped well.
            if (resultCode == RESULT_OK)
            {
                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setMessage("Please wait while we upload your image");
                progressDialog.setTitle("Uploading Image");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                /**
                 * Here I put the new file inside the storage
                 * I used addOnCompleteListener to check if the action worked well.
                 */
                Uri resultUri = result.getUri();
                String currentUserID = currentUser.getUid();

                /**
                 * Stop for a minute to Limit the quality and the size of the images that we upload
                 * convert to bitmap
                 */
                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] thumb_byte = baos.toByteArray(); //in order to upload to firebase we need this

                /**
                 * Add image and compressed image to Folders
                 */
                StorageReference filePath = imageStorage.child("profile_images").child(currentUserID + ".jpg"); // Add the image to profile_images folder
                StorageReference thumb_file_path = imageStorage.child("profile_images").child("thumbs").child(currentUserID + ".jpg"); //Add the compressed image to thumb folder.

                /**
                 * Update Thumb_Image URL
                 */
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        UploadTask uploadTask = (UploadTask) thumb_file_path.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                updateUserImageURL(taskSnapshot, "thumb_image");
                            }
                        });

                        /**
                         * Update Image URL (Not Thumb_Image)
                         */
                        updateUserImageURL(taskSnapshot, "image");
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

    }

    /**
     * After the putFile function has succeed, We can use updateChildren Method
     */
    private void updateUserImageURL(UploadTask.TaskSnapshot taskSnapshot, String image) {
        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Map update_hashMap = new HashMap<>();
                update_hashMap.put(image, uri.toString());

                userDataBase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            progressDialog.dismiss();
                        else {
                            progressDialog.hide();
                            Toast.makeText(SettingsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

}
