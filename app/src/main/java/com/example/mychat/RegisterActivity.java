package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * In this activity the user suppose to register to the firebase db.
 */
public class RegisterActivity extends AppCompatActivity {

    //TODO Request Permissions! How to add permissions in google

    private FirebaseAuth mAuth;
    private TextInputLayout name;
    private TextInputLayout email;
    private TextInputLayout password;
    private Button createAccount;
    private Toolbar mToolBar;
    private ProgressDialog progressDialog;
    private DatabaseReference dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog  = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        name =  (TextInputLayout)findViewById(R.id.id);
        email =  (TextInputLayout)findViewById(R.id.email);
        password =  (TextInputLayout)findViewById(R.id.password);
        createAccount = (Button)findViewById(R.id.createAccountBtn);

        /**
         * Adding Tool bar with title and option to go back
         * Where to going back written in Manifest
         */
        mToolBar = (Toolbar)findViewById(R.id.register_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Take the details from the textInputLayout and register the user
         */
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName =  name.getEditText().getText().toString();
                String displayEmail =  email.getEditText().getText().toString();
                String thePassword =  password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(displayEmail) && !TextUtils.isEmpty(thePassword))
                {
                    progressDialog.setTitle("Registering");
                    progressDialog.setMessage("Please wait a moment");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    register_user(displayEmail, thePassword, displayName);
            }   }
        });


    }

    /**
     * Register the user by using email and password method through firebase.
     * After that, log in with the new user automaticly
     * @param displayEmail email to register
     * @param thePassword new password for the account
     */
    private void register_user(String displayEmail, String thePassword, String displayName) {
        mAuth.createUserWithEmailAndPassword(displayEmail,thePassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            /**
                             * Add content to the database
                             */
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uID = currentUser.getUid();
                            dataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name",displayName);
                            userMap.put("status", "Hi, I am using MyChat");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            dataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                            });

                        }
                        else
                        {
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot Sign In", Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }
}