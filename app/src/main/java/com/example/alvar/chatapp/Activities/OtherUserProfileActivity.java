package com.example.alvar.chatapp.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbChatRequestNodeRef;
    //ui elements
    private CircleImageView otherUserImg;
    private TextView usernameOtherUser, statusOtherUser;
    private Button btnOther;
    //vars
    private String otherUserIdReceived, senderRequestUserId;
    private String current_database_state = "new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        initFirebase();
        receiveUserId();
        retrieveInfo();
        bindUI();
        manageChatRequest();

    }

    private void bindUI() {
        otherUserImg = findViewById(R.id.otherUsersImgProf);
        usernameOtherUser = findViewById(R.id.usernameOtherUser);
        statusOtherUser = findViewById(R.id.statusOtherUser);
        btnOther = findViewById(R.id.buttonOtherUser);
    }

    private void initFirebase(){
        //init firebase auth to get current user id
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //we aim to "Users" node
        dbUsersNodeRef = database.getReference().child("Users");
        //we aim to "Chat Request" node
        dbChatRequestNodeRef = database.getReference().child("Chat Requests");
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

        dbUsersNodeRef.child(receiveUserId()).addValueEventListener(new ValueEventListener() {
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

    /**
     * this method is in charge of the chat requests
     */
    private void manageChatRequest(){

        //we get current user id
        senderRequestUserId = auth.getCurrentUser().getUid();
        current_database_state = "new";

        chatRequestStatus();


            //in case the current user open it's own profile in the "all users" page
        if (senderRequestUserId.equals(otherUserIdReceived)){
            //we hide "send request" button
            btnOther.setVisibility(View.INVISIBLE);

        } else{

            //do something here
            btnOther.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (current_database_state.equals("new")){

                        sendChatRequest();

                    }

                }
            });
            
        }
    }

    /**
     * this method is in charge of uploading the current status of a chat request
     */
    private void chatRequestStatus() {


        dbChatRequestNodeRef.child(senderRequestUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(otherUserIdReceived)) {

                    String request_type = dataSnapshot.child(otherUserIdReceived).child("request_type").getValue().toString();

                    if (request_type.equals("sent")){
                        current_database_state = "request_sent";
                        btnOther.setText(getString(R.string.cancelChatRequest));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * this method is in charge of sending a chat request
     */
    private void sendChatRequest() {

        //now we create 2 nodes ( 1 for the sender request and the other for the receiver request

        dbChatRequestNodeRef.child(senderRequestUserId).child(otherUserIdReceived).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                /*
                is we created the first 2 nodes now we create another 2 nodes (1 for the request receiver
                and the other for the request sender
                */
                if (task.isSuccessful()){

                    dbChatRequestNodeRef.child(otherUserIdReceived).child(senderRequestUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        btnOther.setEnabled(true);
                                        current_database_state = "request_sent";
                                        btnOther.setText(getString(R.string.cancelChatRequest));

                                        Toast.makeText(OtherUserProfileActivity.this,
                                                getString(R.string.chatRequestSent), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            }
        });


    }


}
