package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.TimeZone;


/**
 *     @author Shay Maryuma
 *     This page is for the user after he connected to the db.
 *     It includes TabLayout with 3 tabs with an adapter to control the tabs
 *     The adapter is sectionsPagerAdapter and it decides what page to represent.
 *     All the content of the fragments is inside the ViewPager.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Authenticator to firebase
     */
    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter; // I've created that class
    private TabLayout tabLayout;
    private DatabaseReference userRefDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Adding Tool bar with title and option to go back
         * Where to going back written in Manifest
         */
        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Shay-Chat App");
        mAuth = FirebaseAuth.getInstance(); //connect to FireBase
        FirebaseUser curr_user = mAuth.getCurrentUser();
        if(curr_user != null)
            userRefDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(curr_user.getUid());

        /**
         * ViewPager called main_tab_pager: is the view where we can put the fragments in.
         * TabLayout main_tabs: The bottom purple bar that holds the tabs under the toolbar
         */
        viewPager = (ViewPager)findViewById(R.id.main_tabPager);
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        /**
         * Setting Adapter:
         * It means that we have a tab layout that includes 3 tabs
         * and we need an adapter in order to control it.
         * So we have created SectionsPagerAdapter Class and in this class we
         * have created rules for the layout tabs.
         * Lastly, we have put the viewPager to represent the Fragments.
         */
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     *  currentUser means the user is connected.
     */
    @Override
    protected void onStart() {
        super.onStart();

        //if not registered return null
        if(mAuth == null)
            return;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
            sendToStart();
        else
        {
            if(userRefDataBase != null)
                   userRefDataBase.child("online").setValue(true);
        }

    }

    /**
     * When app get minimized or shut down
     */
    @Override
    protected void onStop() {
        super.onStop();
//        if(userRefDataBase != null)
//        {
//            userRefDataBase.child("online").setValue(ServerValue.TIMESTAMP);
//        }
    }

    /**
     * Add options to the main menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SpannableString s = new SpannableString("Disconnect");
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        menu.findItem(R.id.main_logout_btn).setTitle(s);

        s = new SpannableString("Settings");
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0 , s.length(), 0);
        menu.findItem(R.id.main_settings_btn).setTitle(s);

        s = new SpannableString("All Users");
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0 , s.length(), 0);
        menu.findItem(R.id.main_allUsers_btn).setTitle(s);
        return true;
    }

    /**
     * Choose one of the options of the 3 dots symbol that I've put on the Bar and do something
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_btn)
        {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId() == R.id.main_settings_btn)
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        if(item.getItemId() == R.id.main_allUsers_btn)
        {
            startActivity(new Intent(MainActivity.this, UserActivity.class));
        }
        return true;
    }

    /**
     * Go to activity Start and avoid clicking "back" to go back
     */
    public void sendToStart()
    {
        Intent startIntent =  new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish(); //unable to go back
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode==KeyEvent.KEYCODE_BACK)
//            return false;
//        return super.onKeyDown(keyCode, event);
//    }
}