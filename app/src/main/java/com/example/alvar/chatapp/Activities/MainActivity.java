package com.example.alvar.chatapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.Dialogs.ImageProfileShow;
import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.GroupsFragment;
import com.example.alvar.chatapp.Fragments.RequestsFragment;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private DatabaseReference dbUsersRef;
    //UI elements
    private Toolbar toolbarMain;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private CircleImageView image;
    private TextView usernameNav, statusNav;
    private View navViewHeader;
    private ActionBarDrawerToggle burgerIcon;
    //vars
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: init");

        //init UI elements
        bindUI();
        //init toolbar and set title
        setToolbar("ChatUp", true);
        //init firebase
        initFirebase();
        //set image and username from db to toolbar
        fetchInfoFromDb();
        //when image within drawer is clicked by the user
        drawerImagePressed();
        // viewPagerAdapter init
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                drawerOptionsMenu(menuItem);

                return false;
            }
        });
        initPageAdapter(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        //we set "no" as typing state in the db as soon as the app is launched
        typingState("no");
    }

    @Override
    protected void onResume() {
        super.onResume();
            //this method will pass "Online" to the database as soon as the user is using the app
            updateDateTime("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
             updateDateTime("Offline");
    }

    /**
     * init UI elements
     */
    private void bindUI() {

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        navigationView = findViewById(R.id.navView);
        //drawer layout
        drawerLayout = findViewById(R.id.navigationDrawerLayout);
        burgerIcon = new ActionBarDrawerToggle(MainActivity.this, drawerLayout , toolbarMain, R.string.drawerOpen, R.string.drawerClose );
        drawerLayout.addDrawerListener(burgerIcon);
        burgerIcon.syncState();
        //elements within nav header
        navViewHeader = navigationView.inflateHeaderView(R.layout.nav_header);
        image = navViewHeader.findViewById(R.id.imageNavDrawer);
        usernameNav = navViewHeader.findViewById(R.id.usernameNavDrawer);
        statusNav = navViewHeader.findViewById(R.id.statusNavDrawer);

    }

    /**
     * this method sets toolbar and it's details
     * @param title
     */
    private void setToolbar(String title, Boolean backOption) {
        //create toolbar
        toolbarMain = findViewById(R.id.toolbarMain);
        //we set the toolbar
        setSupportActionBar(toolbarMain);
        //we pass the title
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    /**
     * this methods handles the action taken in the drawer menu
     * @param menuItem
     */
    private void drawerOptionsMenu(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.contacts:
                Intent intentContacts = new Intent(MainActivity.this, ContactsActivity.class);
                startActivity(intentContacts);
                break;
            case R.id.maps:
                Log.i(TAG, "drawerOptionsMenu: maps pressed");
                break;
            case R.id.settingsAccount:
                goToSettingAccount();
                Log.i(TAG, "onOptionsItemSelected: setting btn pressed");
                break;
            case R.id.menuAllUsers:
                goToAllUsers();
                Log.i(TAG, "onOptionsItemSelected: all users btn pressed");
                break;
            case R.id.signOut:
                alertMessage(getString(R.string.alertDialogTitle), getString(R.string.alertDialogMessage));
                Log.i(TAG, "onOptionsItemSelected: log out button pressed");
                break;

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (burgerIcon.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * init firebase services
     */
    private void initFirebase() {
        //Firebase auth init
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        //database init
        database = FirebaseDatabase.getInstance();
        //database ref init and get access ti "Users" branch in the db
        dbUsersRef = database.getReference().child("Users");
    }

    /**
     * this method is in charge of creating the tabs and setting it's title
     * @param viewPager
     */
    private void initPageAdapter(ViewPager viewPager) {

        ViewPagerAdapter Adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Adapter.addFragment(new GroupsFragment(), getString(R.string.groups));
        Adapter.addFragment(new ChatsFragment(), getString(R.string.chat));
        Adapter.addFragment(new RequestsFragment(), getString(R.string.requests));
        viewPager.setAdapter(Adapter);
        //this line sets the second fragment as default when app is launched.
        viewPager.setCurrentItem(1);
    }

    /**
     * method in charge of signing out from firebase console
     */
    private void signOut() {
        //sign out from firebase service and app
        FirebaseAuth.getInstance().signOut();
        Intent intentSignOut = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intentSignOut);
        finish();
    }

    /**
     * this method is in charge of taking the user to settings page
     */
    private void goToSettingAccount() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * this method is in charge of taking the user to all users page
     */
    private void goToAllUsers() {
        Intent intentAllUsers = new Intent(MainActivity.this, AllUsersActivity.class);
        startActivity(intentAllUsers);
    }

    /**
     * this method contains the pop-up message when user clicks log out from menu option
     * (standard alert dialog)
     * @param title
     * @param message
     * @return
     */
    private AlertDialog alertMessage(String title, String message) {


        AlertDialog popUpWindow = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.signing_Out), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();

        return popUpWindow;
    }

    /**
     * this method is in charge of fetching info from the db and set it into the toolbar
     */
    private void fetchInfoFromDb() {

        currentUserID = mAuth.getCurrentUser().getUid();

        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    String imageThumbnailToolbar = dataSnapshot.child("imageThumbnail").getValue().toString();
                    String usernameToolbar = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    Log.i(TAG, "onDataChange: username set");
                    usernameNav.setText(usernameToolbar);
                    statusNav.setText(status);

                    if (imageThumbnailToolbar.equals("imgThumbnail")) {
                        image.setImageResource(R.drawable.profile_image);
                    } else {
                        Log.i(TAG, "onDataChange: image set");
                        Glide.with(getApplicationContext()).load(imageThumbnailToolbar).into(image);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong with your network", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


    }

    /**
     * method handle events when toolbar has been clicked
     */
    private void drawerImagePressed(){

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertDialog();
                Log.i(TAG, "onClick: toolbarMain pressed!!!!");

            }
        });
    }

    /**
     * method in charge of init "ImageProfileShow" dialog class
     */
    private void showAlertDialog() {

        ImageProfileShow imageDialog = new ImageProfileShow();
        imageDialog.show(getSupportFragmentManager(), "showImageProfile");

    }

    /**
     * this method is in charge of closing the drawer when pressing back button
     */
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    /**
     * method in charge of getting the user's current state, time and Date to update in db
     */
    private void updateDateTime(String state){

        String currentTime, currentDate;

        Calendar calendar =  Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MMM/yyyy");
        currentDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        currentTime = time.format(calendar.getTime());

        //lets save all this info in a map to uploaded to the Firebase database.
        //NOTE: we use HashMap instead of an Object because the database doesn't accept a Java Object
        // when the database will be updated when using "updateChildren" whereas when using setValue you can use a Java Object.
        HashMap<String , Object> userState = new HashMap<>();
        userState.put("state", state);
        userState.put("date", currentDate);
        userState.put("time", currentTime);

        dbUsersRef.child(currentUserID).child("userState").updateChildren(userState);

    }

    /**
     * method in charge of updating the other user's typing state in the db in real time
     * @param typingState
     */
    private void typingState(String typingState){

        HashMap<String, Object> typingStateMap = new HashMap<>();
        typingStateMap.put("typing" , typingState);

        dbUsersRef.child(currentUserID).child("userState").updateChildren(typingStateMap);

    }



}
