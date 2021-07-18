package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
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

import org.w3c.dom.Text;

/**
 *  In this activity there is an option to connect to the db or to move to register activity.
 *  Example username:
 *  stam@stam.co.il
 *  a123456789
 */
public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mRegBtn,loginBtn;
    private TextInputLayout email;
    private TextInputLayout password;
    private Toolbar mToolBar;
    private ConstraintLayout myLayout; //Save this if you want to change the color of the background.
    private ProgressDialog progressDialog; //Loading screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        progressDialog  = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance(); //authenticator
        mRegBtn = (Button)findViewById(R.id.regBtn); //Register Button
        loginBtn = (Button)findViewById(R.id.loginBtn); //Login Button
        email =  (TextInputLayout)findViewById(R.id.myEmail);   //email from text box
        password =  (TextInputLayout)findViewById(R.id.myPassword); //password from text box
        myLayout = (ConstraintLayout)findViewById(R.id.startLayout);

        /**
         * Adding Tool bar with title and option to go back
         * Where to going back written in Manifest
         */
        mToolBar = (Toolbar)findViewById(R.id.start_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Start");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // myLayout.setBackgroundColor(getResources().getColor(R.color.purple_500));

        /**
         *  Click on Register Button and then it goes to register page
         */
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextIn = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(nextIn);
            }
        });
        /**
         *  Click on Login Button and it will log in to the user from the text boxes.
         *  Auth will be by using the method "signInWithEmailAndPassword" and then getting the current user.
         */
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayEmail =  email.getEditText().getText().toString();
                String displayPassword =  password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayEmail) && !TextUtils.isEmpty(displayPassword))
                {
                    progressDialog.setTitle("Logging In");
                    progressDialog.setMessage("Please wait a moment");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    loginUser(displayEmail,displayPassword);
                }

            }
        });
    }

    public void loginUser(String displayEmail,String displayPassword){
        mAuth.signInWithEmailAndPassword(displayEmail,displayPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    startActivity(new Intent(StartActivity.this, MainActivity.class)); // Move to the scene where we are already connected.
                    finish();
                }
                else
                {
                    progressDialog.hide();
                    Toast.makeText(StartActivity.this, "Cannot Sign In", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}