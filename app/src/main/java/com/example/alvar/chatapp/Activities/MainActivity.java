package com.example.alvar.chatapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.Dialogs.ImageProfileShow;
import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.RequestsFragment;
import com.example.alvar.chatapp.Fragments.GroupsFragment;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
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
     * this method sets toolbar and it details
     *
     * @param title
     */
    private void setToolbar(String title, Boolean backOpion) {
        //create toolbar
        toolbarMain = findViewById(R.id.toolbarMain);
        //we set the toolbar
        setSupportActionBar(toolbarMain);
        //we pass the title
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOpion);
    }


    private void drawerOptionsMenu(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.contacts:
                Intent intentContacts = new Intent(MainActivity.this, ContactsActivity.class);
                startActivity(intentContacts);
                break;
            case R.id.maps:
                Toast.makeText(this, "maps pressed", Toast.LENGTH_SHORT).show();
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

    //
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        //bind menu xml file to code
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        return true;
//    }
//

//
//    /**
//     * Metohod in charge of taking the user to pages from option menu
//     * @param item
//     * @return
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//
//            case R.id.settingsAccount:
//                goToSettingAccount();
//                Log.i(TAG, "onOptionsItemSelected: setting btn pressed");
//                return true;
//            case R.id.menuAllUsers:
//                goToAllUsers();
//                Log.i(TAG, "onOptionsItemSelected: all users btn pressed");
//                return true;
//            case R.id.signOut:
//                alertMessage(getString(R.string.alertDialogTitle), getString(R.string.alertDialogMessage));
//                Log.i(TAG, "onOptionsItemSelected: log out button pressed");
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }


    /**
     * init firebase services
     */
    private void initFirebase() {
        //Firebase auth init
        mAuth = FirebaseAuth.getInstance();
        //database init
        database = FirebaseDatabase.getInstance();
        //database ref init
        dbUsersRef = database.getReference().child("Users");
    }


    /**
     * this method is in charge of creating the tabs and setting it's title
     *
     * @param viewPager
     */
    private void initPageAdapter(ViewPager viewPager) {

        ViewPagerAdapter Adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Adapter.addFragment(new GroupsFragment(), getString(R.string.groups));
        Adapter.addFragment(new ChatsFragment(), getString(R.string.chat));
        Adapter.addFragment(new RequestsFragment(), getString(R.string.requests));
        viewPager.setAdapter(Adapter);

    }

    /**
     * method in charge of signin out from firebase console
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
     *
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
     * method handle even when toolbar has been clicked
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
     * this method is in charge os closing the drawer when pressing back button
     */
    @Override
    public void onBackPressed() {


        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }



    }
}

