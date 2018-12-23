package com.example.alvar.chatapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.ContactsFragment;
import com.example.alvar.chatapp.Fragments.GroupsFragment;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    //UI elements
    private Toolbar toolbarMain;
    private ViewPager viewPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: init");

        //init UI elements
        bindUI();
        //init toolbar and set title
        setToolbar(getString(R.string.app_name));
        //init firebase
        initFirebase();
        // viewPagerAdapter init
        initPageAdapter(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }


    /**
     * bind UI elements
     */
    private void bindUI(){
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    /**
     * this method sets toolbar and it details
     * @param title
     */
    private void setToolbar(String title){
        //create toolbar
        toolbarMain = findViewById(R.id.toolbarMain);
        //we set the toolbar
        setSupportActionBar(toolbarMain);
        //we pass the title
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //bind menu xml file to code
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Metohod in charge of taking the user to pages from option menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOut:
                signOut();
                Toast.makeText(this, getString(R.string.signing_Out) , Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settingsAccount:
                goToSettingAccount();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initFirebase() {
        //Firebase auth init
        mAuth = FirebaseAuth.getInstance();
        //database init
        database = FirebaseDatabase.getInstance();
        //database ref init
        dbRef = database.getReference();
    }


    /**
     * this method is in charge of creating the tabs and setting it's title
     * @param viewPager
     */
    private void initPageAdapter(ViewPager viewPager ){

        ViewPagerAdapter Adapter  = new ViewPagerAdapter(getSupportFragmentManager());
        Adapter.addFragment(new GroupsFragment(), getString(R.string.groups));
        Adapter.addFragment(new  ChatsFragment(), getString(R.string.chat) );
        Adapter.addFragment(new  ContactsFragment(),getString(R.string.friends)  );
        viewPager.setAdapter(Adapter);

    }

    /**
     * method in charge of signin out from firebase console
     */
    private void signOut(){
        //sign out from firebase service and app
        FirebaseAuth.getInstance().signOut();
        Intent intentSignOut = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intentSignOut);
        finish();
    }

    /**
     * this method is in charge of taking the user to settings page
     */
    private void goToSettingAccount(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


}

