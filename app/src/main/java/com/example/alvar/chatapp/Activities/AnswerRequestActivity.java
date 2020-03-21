package com.example.alvar.chatapp.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class AnswerRequestActivity extends AppCompatActivity {

    private static final String TAG = "AnswerRequestPage";

    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef , dbContactsNodeRef, dbRequestsNodeRef;
    //ui elements
    private ImageView imageView;
    private TextView otherUserName;
    private Button acceptButton, declineButton;
    //vars
    private String currentUserID, otherUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_request);

        //we get user id from "Request Fragment"
        otherUserID = getIntent().getStringExtra("otherUserID");

        bindUI();
        initFirebase();
        fetchInfo();
        
        currentUserID = auth.getCurrentUser().getUid();
        acceptOrDeclineRequest();

        Log.i(TAG, "onCreate: other user ID: " + otherUserID);

    }

    /**
     * this method contains los click listener for accept and decline buttons
     */
    private void acceptOrDeclineRequest() {

        //if user press "accept" button
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptChatRequest();
            }
        });

        //if user press "decline" button
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineChatRequest();
            }
        });
    } 


    private void bindUI(){

        imageView = findViewById(R.id.requestImage);
        otherUserName = findViewById(R.id.requestName);
        acceptButton = findViewById(R.id.requestButtonAccept);
        declineButton = findViewById(R.id.requestButtonDecline);

    }

    private void initFirebase(){

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbContactsNodeRef = database.getReference().child("Contacts");
        dbRequestsNodeRef = database.getReference().child("Chat_Requests");

    }

    /**
     * this method is in charge of fetching info from "Users" node and set up the
     * name and image
     */
    private void fetchInfo(){

        dbUsersNodeRef.child(otherUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    otherUserName.setText(name);

                    if (image.equals("image")){
                        imageView.setImageResource(R.drawable.profile_image);
                    }else {
                        Glide.with(getApplicationContext()).load(image).into(imageView);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method in charge of accepting chat request
     */
    private void acceptChatRequest() {

            /*at this point since we have accepted the chat request
              we add the new contact in the "Contacts" node
             */
        dbContactsNodeRef.child(currentUserID).child(otherUserID)
                .child("contact_status")
                .setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            dbContactsNodeRef.child(otherUserID).child(currentUserID)
                                    .child("contact_status")
                                    .setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                    /*now from this point onward we remove request from request tab
                                                      by deleting such request from the "Chat_Requests" node
                                                     */

                                                dbRequestsNodeRef.child(currentUserID).child(otherUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    dbRequestsNodeRef.child(otherUserID).child(currentUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {

                                                                                        Toast.makeText(AnswerRequestActivity.this, 
                                                                                                "friend added", Toast.LENGTH_SHORT).show();
                                                                                        finish();

                                                                                    }
                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });

                                            }
                                        }
                                    });


                        }

                    }
                });
    }


    /**
     * method in charge of declining chat request
     */
    private void declineChatRequest() {

        dbRequestsNodeRef.child(currentUserID).child(otherUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            dbRequestsNodeRef.child(otherUserID).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(AnswerRequestActivity.this, "decline", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }
                                    });
                        }

                    }
                });



    }

}
