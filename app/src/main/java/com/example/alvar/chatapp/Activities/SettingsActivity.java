package com.example.alvar.chatapp.Activities;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.StatusChangeActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    //log
    private static final String TAG = "SettingsPage";
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    //UI elements
    private CircleImageView imageProfile;
    private TextView textStatus, textUsername;
    private FloatingActionButton fabOption1, fabOption2;
    //Vars
    private String userID;
    private String name, status, image, imageThumbnail, email;
    //Const firebase database
    private static final String DATABASE_NODE = "Users";
    private static final String DATABASE_CHILD_NAME = "name";
    private static final String DATABASE_CHILD_EMAIL = "email";
    private static final String DATABASE_CHILD_IMAGE = "image";
    private static final String DATABASE_CHILD_IMAGE_THUMBNAIL = "imageThumbnail";
    private static final String DATABASE_CHILD_STATUS = "status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bindUI();
        //fab buttons listeners
        fabButtonClicked();
        //init firebase and get current user ID
        initFirebase();
        //init retrieve data from firebase database method
        retrieveDataFromDb();
    }

    /**
     * UI elements
     */
    private void bindUI(){
        imageProfile    = findViewById(R.id.settingImgProfile);
        textStatus     = findViewById(R.id.settingsUserStatus);
        textUsername    = findViewById(R.id.settingsUsername);
        fabOption1      = findViewById(R.id.fabOption1);
        fabOption2      = findViewById(R.id.fabOption2);
    }

    /**
     * Method in charge of event when fab buttons are clicked
     */
    private void fabButtonClicked(){
        fabOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "prueba fab 2", Toast.LENGTH_SHORT).show();
            }
        });

        fabOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentStatus = textStatus.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, StatusChangeActivity.class);
                intent.putExtra("currentStatus", currentStatus);
                startActivity(intent);
            }
        });
    }
    /**
     method in charge of initialize firebase service
     */
    private void initFirebase(){
        //init Firebase auth
        mAuth = FirebaseAuth.getInstance();
        //we get current user logged in
        currentUser = mAuth.getCurrentUser();
        //save unique UID from user logged-in to a var type String named "userID"
        userID = currentUser.getUid();
        //init Firebase database
        database = FirebaseDatabase.getInstance();
        //init database reference and we aim to the users data by passing "userID" as child.
        mRef = database.getReference(DATABASE_NODE).child(userID);
        Log.i(TAG, "initFirebase: userid: " + userID);
    }

    /**
     * method in charge of fetching data from database
     */
    private void retrieveDataFromDb(){

        // Read from the database
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i(TAG, "onDataChange: data retrieved :" + dataSnapshot);
                infoFetched(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i(TAG, "Failed to read value." + error.toException());
            }
        });
    }


        private void infoFetched(DataSnapshot dataSnapshot){
            //save info retrieved from DB into String vars
            name = dataSnapshot.child(DATABASE_CHILD_NAME).getValue().toString();
            email = dataSnapshot.child(DATABASE_CHILD_EMAIL).getValue().toString();
            status = dataSnapshot.child(DATABASE_CHILD_STATUS).getValue().toString();
            image = dataSnapshot.child(DATABASE_CHILD_IMAGE).getValue().toString();
            imageThumbnail = dataSnapshot.child(DATABASE_CHILD_IMAGE_THUMBNAIL).getValue().toString();
            //set values to display
            textUsername.setText(name);
            textStatus.setText(status);

            Log.i(TAG, "infoFetched: name: " + name);
            Log.i(TAG, "infoFetched: status: " + status);
            Log.i(TAG, "infoFetched: image: " + image);
            Log.i(TAG, "infoFetched: imgThumbnail: " + imageThumbnail);
            Log.i(TAG, "infoFetched: email: " + email);
        }




}
