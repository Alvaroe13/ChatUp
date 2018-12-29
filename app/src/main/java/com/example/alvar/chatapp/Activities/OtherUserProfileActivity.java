package com.example.alvar.chatapp.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserProfileActivity extends AppCompatActivity {
    //log
    private static final String TAG = "OtherUserProfilePage";
    //firebase
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    //ui elements
    private CircleImageView otherUserImg;
    private TextView usernameOtherUser, statusOtherUser;
    private Button btnOther;
    //vars
    private String otherUserIdReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);


        initFirebase();
        receiveUserId();
        retrieveInfo();
        binUI();


    }

    private void binUI() {
        otherUserImg = findViewById(R.id.otherUsersImgProf);
        usernameOtherUser = findViewById(R.id.usernameOtherUser);
        statusOtherUser = findViewById(R.id.statusOtherUser);
        btnOther = findViewById(R.id.buttonOtherUser);
    }

    private void initFirebase(){
        database = FirebaseDatabase.getInstance();
        //we aim to "Users" node
        dbRef = database.getReference().child("Users");
    }

    /**
     * in this method we receive the unique user Id given by firebase to any user.
     * @return
     */
    private String receiveUserId() {
       otherUserIdReceived = getIntent().getStringExtra("otherUserId");
        Log.i(TAG, "receiveUserId: user ID: " + otherUserIdReceived);
        return otherUserIdReceived;
    }

    /**
     * here in this method is th logic to fetch info from the databse
     */
    private void retrieveInfo(){

        dbRef.child(receiveUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                setInfo(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * this method is in charge of setting the info fetched from the db into the UI
     * @param dataSnapshot
     */
    private void setInfo(DataSnapshot dataSnapshot) {

        String username = dataSnapshot.child("name").getValue().toString();
        String status = dataSnapshot.child("status").getValue().toString();
        String image = dataSnapshot.child("image").getValue().toString();
        String imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();

        usernameOtherUser.setText(username);
        statusOtherUser.setText(status);
        if (imageThumbnail.equals("imgThumbnail")){
            otherUserImg.setImageResource(R.drawable.imgdefault);
        } else{
            //here we set image from database into imageView
            Glide.with(OtherUserProfileActivity.this).load(imageThumbnail).into(otherUserImg);

        }


    }

}
