package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private Button saveBtn;
    private TextInputLayout statusInput;

    private DatabaseReference statusDataBase;
    private FirebaseUser currentUser;
    private ProgressDialog progressDialog;

    private String oldStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();

        /**
         * Find all the object references
         */
        statusDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        saveBtn = (Button)findViewById(R.id.save_btn);
        statusInput = (TextInputLayout)findViewById(R.id.status_input);

        /**
         * Old status initialize
         */
        oldStatus = getIntent().getStringExtra("key1"); //get the old status value
        statusInput.getEditText().setText(oldStatus);

        /**
         * Adding Tool bar with title and option to go back
         * Where to going back written in Manifest
         */
        mToolBar = (Toolbar)findViewById(R.id.status_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("My Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Update the status of the user to the DataBase and show Loading Bar.
         */
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(StatusActivity.this); //Using statusActivity.this because we are inside a method.
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Updating Status");
                progressDialog.show();

                String status = statusInput.getEditText().getText().toString();
                /**
                 * We are already in "Users" tab, now we need the child "status" and we can edit it.
                 */
                statusDataBase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            progressDialog.dismiss();
                        else Toast.makeText(getApplicationContext(), "Error saving status",Toast.LENGTH_LONG);
                    }
                });

            }
        });

    }
}