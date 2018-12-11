package com.example.alvar.chatapp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
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
    private String name;
    private String status;
    //Const
    private static final String DATABASE_TREE_NAME = "Users";

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
                Toast.makeText(SettingsActivity.this, "prueba fab 1", Toast.LENGTH_SHORT).show();
            }
        });

        fabOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "prueba fab 2", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initFirebase(){
        //init Firebase auth
        mAuth = FirebaseAuth.getInstance();
        //we get current user logged in
        currentUser = mAuth.getCurrentUser();
        //init Firebase database
        database = FirebaseDatabase.getInstance();
        //init database reference
        mRef = database.getReference();
        //save unique UID from user logged in in a var String name "userID"
        userID = currentUser.getUid();
    }



    private void retrieveDataFromDb(){

        mRef.child(DATABASE_TREE_NAME).child(userID);
        // Read from the database
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Log.i(TAG, "onDataChange: data retrieved :" + dataSnapshot.toString());

                 //problem retrieving information from DB
//                name = dataSnapshot.child("name").getValue().toString();
//                String image = dataSnapshot.child("image").getValue().toString();
//                String status = dataSnapshot.child("status").getValue().toString();
//                String image_thumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();

                //set values into screen
               // textStatus.setText(status);
               // textUsername.setText(name);
                //show progressbar
                //ProgressBarHelper.showProgressBar()


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i(TAG, "Failed to read value." + error.toException());
            }
        });
    }




}
