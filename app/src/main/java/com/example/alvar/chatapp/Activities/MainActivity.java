package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.ContactsFragment;
import com.example.alvar.chatapp.Fragments.RequestsFragment;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    //UI elements
    private Toolbar toolbarMain;
    private ViewPager viewPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: init");
        //Firebase auth init
        mAuth = FirebaseAuth.getInstance();
        // we get firebase current user
        getCurrentUser();
        //init UI elements
        bindUI();
        //init toolbar and set title
        setToolbar(getString(R.string.app_name));
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
            case R.id.allUsers:
                allUsers();
                return true;
            case R.id.settingsAccount:
                goToSettingAccount();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initPageAdapter(ViewPager viewPager ){

        ViewPagerAdapter Adapter  = new ViewPagerAdapter(getSupportFragmentManager());
        Adapter.addFragment(new  RequestsFragment(), "Requests");
        Adapter.addFragment(new  ChatsFragment(), "chat" );
        Adapter.addFragment(new  ContactsFragment(),"Friends"  );
        viewPager.setAdapter(Adapter);

    }

    private FirebaseUser getCurrentUser(){
        // we get current firebase user information
        return currentUser = mAuth.getCurrentUser();
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

    private void allUsers(){
        Toast.makeText(this, "Show all users", Toast.LENGTH_SHORT).show();
    }

    private void goToSettingAccount(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}

