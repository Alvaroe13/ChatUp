package com.example.alvar.chatapp.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.Dialogs.ImageProfileShow;
import com.example.alvar.chatapp.views.AllUsersFragment;
import com.example.alvar.chatapp.views.ChatsFragment;
import com.example.alvar.chatapp.views.ContactsFragment;
import com.example.alvar.chatapp.views.GroupsFragment;
import com.example.alvar.chatapp.views.RequestsFragment;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.views.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.alvar.chatapp.Utils.Constant.TOKEN_PREFS;
import static com.example.alvar.chatapp.Utils.Constant.USER_ID_PREFS;
import static com.example.alvar.chatapp.Utils.Constant.USER_INFO_PREFS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, tokenNodeRef;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userDocRef;
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
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bindUI();
        setToolbar("ChatUp", true);
        initFirebase();
        getToken();

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
        burgerIcon = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbarMain, R.string.drawerOpen, R.string.drawerClose);
        drawerLayout.addDrawerListener(burgerIcon);
        burgerIcon.syncState();
        //elements within nav header
        navViewHeader = navigationView.inflateHeaderView(R.layout.nav_header);
        image = navViewHeader.findViewById(R.id.imageNavDrawer);
        usernameNav = navViewHeader.findViewById(R.id.usernameNavDrawer);
        statusNav = navViewHeader.findViewById(R.id.statusNavDrawer);

    }

    private void getToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()){
                    deviceToken = task.getResult().getToken();

                    saveTokenInDB(deviceToken);
                } else{
                    Toast.makeText(MainActivity.this, "no token", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void saveTokenInDB(final String deviceToken) {
        //set token value id database's token child
        dbUsersNodeRef.child(currentUserID).child("token")
                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    tokenNodeRef.child(currentUserID).child("token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: token " + deviceToken);
                        }
                    });

                }
            }
        });
    }

    /**
     * this method sets toolbar and it's details
     *
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
     *
     * @param menuItem
     */
    private void drawerOptionsMenu(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.contacts:
                launchFragment(new ContactsFragment());
                break;
            case R.id.settingsAccount:
                launchFragment(new SettingsFragment());
                break;
            case R.id.menuAllUsers:
                launchFragment(new AllUsersFragment());
                break;
            case R.id.signOut:
                alertMessage(getString(R.string.alertDialogTitle), getString(R.string.alertDialogMessage));
                break;

        }


    }

    /**
     * by passing the fragment we want to launch as param it'll inflate the view
     * @param fragment
     */
    private void launchFragment(Fragment fragment){

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment );
        transaction.addToBackStack(null);
        transaction.commit();

        //this piece of code is able to close drawer
        drawerLayout.closeDrawer(GravityCompat.START);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (burgerIcon.onOptionsItemSelected(item)) {
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
        //database init
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
        tokenNodeRef= database.getReference().child("Tokens");
    }

    /**
     * init firestore services
     */
    private void initFirestore() {
        mDb = FirebaseFirestore.getInstance();
        userDocRef = mDb.collection(getString(R.string.users_ref)).document(currentUserID);
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
     * this method contains the pop-up message when user clicks log out from menu option
     * (standard alert dialog)
     *
     * @param title
     * @param message
     * @return
     */
    private AlertDialog alertMessage(String title, String message) {


        AlertDialog popUpWindow = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning)
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

        dbUsersNodeRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    String imageThumbnailToolbar, usernameToolbar, status, email, imageProfile, password;


                        imageThumbnailToolbar = dataSnapshot.child(getString(R.string.imageThumbnail_db)).getValue().toString();
                        usernameToolbar = dataSnapshot.child(getString(R.string.name_db)).getValue().toString();
                        status = dataSnapshot.child(getString(R.string.status_db)).getValue().toString();

                        //these are to populate firestore only
                        email = dataSnapshot.child(getString(R.string.email_db)).getValue().toString();
                        imageProfile = dataSnapshot.child(getString(R.string.image_db)).getValue().toString();
                        password = dataSnapshot.child(getString(R.string.password_db)).getValue().toString();

                    //pass info to firestore db
                    //User user = new User(usernameToolbar, email, password, status, imageProfile, imageThumbnailToolbar, token, currentUserID);

                    populateFirestore(usernameToolbar, email, password, status, imageProfile, imageThumbnailToolbar, deviceToken);
                    saveTokenOnPreferences(currentUserID, deviceToken);


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
    private void drawerImagePressed() {

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

        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed();
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }



    /**
     * method in charge of getting the user's current state, time and Date to update in db
     */
    private void updateDateTime(String state) {

        String currentTime, currentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MMM/yyyy");
        currentDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        currentTime = time.format(calendar.getTime());

        //lets save all this info in a map to uploaded to the Firebase database.
        //NOTE: we use HashMap instead of an Object because the database doesn't accept a Java Object
        // when the database will be updated when using "updateChildren" whereas when using setValue you can use a Java Object.
        HashMap<String, Object> userState = new HashMap<>();
        userState.put(getString(R.string.state_db), state);
        userState.put(getString(R.string.date_db), currentDate);
        userState.put(getString(R.string.time_db), currentTime);
        userState.put( "location", "Off");

        dbUsersNodeRef.child(currentUserID).child(getString(R.string.user_state_db)).updateChildren(userState);

    }

    /**
     * method in charge of updating the other user's typing state in the db in real time
     *
     * @param typingState
     */
    private void typingState(String typingState) {

        HashMap<String, Object> typingStateMap = new HashMap<>();
        typingStateMap.put(getString(R.string.typing_db), typingState);

        dbUsersNodeRef.child(currentUserID).child(getString(R.string.user_state_db)).updateChildren(typingStateMap);

    }

    private void populateFirestore(String username, String email, String password, String status, String profilePic, String imgThumbnail, String token) {

        initFirestore();

        HashMap<String, Object> userFirestore = new HashMap<>();
        userFirestore.put(getString(R.string.name_db), username);
        userFirestore.put(getString(R.string.email_db), email);
        userFirestore.put(getString(R.string.password_db), password);
        userFirestore.put(getString(R.string.status_db), status);
        userFirestore.put(getString(R.string.image_db), profilePic);
        userFirestore.put(getString(R.string.imageThumbnail_db), imgThumbnail);
        userFirestore.put(getString(R.string.token_db), token);


        userDocRef.set(userFirestore).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: population to Firestore done");
                } else {
                    Log.i(TAG, "onComplete: error: " + task.getException().getMessage());
                }
            }
        });

    }

    private void saveTokenOnPreferences(String userID , String token) {
        SharedPreferences prefs = getSharedPreferences(USER_INFO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_PREFS, token);
        editor.putString(USER_ID_PREFS, userID);
        editor.apply();
    }


    @Override
    protected void onPause() {
        super.onPause();
        updateDateTime("Offline");
    }


}
