package com.example.alvar.chatapp.Activities;

import android.app.Dialog;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.Fragments.ChatsFragment;
import com.example.alvar.chatapp.Fragments.ContactsFragment;
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
    private CircleImageView imageToolbarMain;
    private TextView usernameToolbarMain;
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
        setToolbar("", false);
        //init firebase
        initFirebase();
        //set image and username from db to toolbar
        fetchInfoFromDb();
        //when toolbar is clicked by the user
        toolbarOnClick();
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
        imageToolbarMain = findViewById(R.id.imageToolbar);
        usernameToolbarMain = findViewById(R.id.usernameMainToolbar);

    }

    /**
     * this method sets toolbar and it details
     * @param title
     */
    private void setToolbar(String title, Boolean backOpion){
        //create toolbar
        toolbarMain = findViewById(R.id.toolbarMain);
        //we set the toolbar
        setSupportActionBar(toolbarMain);
        //we pass the title
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOpion);
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

            case R.id.settingsAccount:
                goToSettingAccount();
                Log.i(TAG, "onOptionsItemSelected: setting btn pressed");
                return true;
            case R.id.menuAllUsers:
                goToAllUsers();
                Log.i(TAG, "onOptionsItemSelected: all users btn pressed");
                return true;
            case R.id.signOut:
                alertMessage(getString(R.string.alertDialogTitle), getString(R.string.alertDialogMessage));
                Log.i(TAG, "onOptionsItemSelected: log out button pressed");
                return true;
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
        dbUsersRef = database.getReference().child("Users");
    }


    /**
     * this method is in charge of creating the tabs and setting it's title
     * @param viewPager
     */
    private void initPageAdapter(ViewPager viewPager ){

        ViewPagerAdapter Adapter  = new ViewPagerAdapter(getSupportFragmentManager());
        Adapter.addFragment(new GroupsFragment(), getString(R.string.groups));
        Adapter.addFragment(new  ChatsFragment(), getString(R.string.chat) );
        Adapter.addFragment(new  ContactsFragment(),getString(R.string.requests)  );
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


    /**
     * this method is in charge of taking the user to all users page
     */
    private void goToAllUsers() {
        Intent intentAllUsers = new Intent(MainActivity.this, AllUsersActivity.class);
        startActivity(intentAllUsers);
    }

    /**
     * this method contains the pop up message when user clicks log out from menu option
     * @param title
     * @param message
     * @return
     */
    private AlertDialog alertMessage(String title, String message){


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
    private void fetchInfoFromDb(){

        currentUserID = mAuth.getCurrentUser().getUid();

        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String imageThumbnailToolbar = dataSnapshot.child("imageThumbnail").getValue().toString();
                    String usernameToolbar = dataSnapshot.child("name").getValue().toString();

                    Log.i(TAG, "onDataChange: username set");
                    usernameToolbarMain.setText(usernameToolbar);

                    if (imageThumbnailToolbar.equals("imgThumbnail")){
                        imageToolbarMain.setImageResource(R.drawable.profile_image);
                    }else{
                        Log.i(TAG, "onDataChange: image set");
                        Glide.with(MainActivity.this).load(imageThumbnailToolbar).into(imageToolbarMain);
                    }

                } else{
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
    private void toolbarOnClick(){


        toolbarMain.setEnabled(true);
        toolbarMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageProfileDialog();
                Log.i(TAG, "onClick: toolbarMain pressed!!!!");

            }
        });
    }

    /**
     * method in charge of displaying the image profile once the toolbar has been clicked
     * @return
     */
    private AlertDialog.Builder imageProfileDialog(){
        //create alertDialog
        AlertDialog.Builder imageDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        //create Dialog's view
        View imageProfileView = getLayoutInflater().inflate(R.layout.profile_dialog, null);
        //bind imageView from layout into the code
        final ImageView imageProfileDialog = imageProfileView.findViewById(R.id.imageProfileDialog);
        //set View to it's dialog builder
        imageDialogBuilder.setView(imageProfileView);

        //we access db containing info to be fetched
        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //we store the image from the db into String var
                String image = dataSnapshot.child("image").getValue().toString();

                //set image from firebase databsae to UI
                if ( image.equals("image")){
                    imageProfileDialog.setImageResource(R.drawable.profile_image);
                }else{
                    Glide.with(MainActivity.this).load(image).into(imageProfileDialog);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //show alert dialog builder
        imageDialogBuilder.show();

        return imageDialogBuilder ;
    }


}

