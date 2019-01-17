package com.example.alvar.chatapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserProfileActivity extends AppCompatActivity {
    //log
    private static final String TAG = "OtherUserProfilePage";
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbChatRequestNodeRef, contactsNodeRef;
    //ui elements
    private CircleImageView otherUserImg;
    private TextView usernameOtherUser, statusOtherUser;
    private Button buttonSendRequest, buttonRejectRequest;
    private CoordinatorLayout coordinatorLayout;
    //vars
    private String otherUserId, senderRequestUserId;
    private String current_database_state = "not_friend_yet";
    private String username, status, imageThumbnail;

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
        buttonSendRequest = findViewById(R.id.buttonSendRequest);
        buttonRejectRequest = findViewById(R.id.buttonDeclineRequest);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    private void initFirebase(){
        //init firebase auth to get current user id
        auth = FirebaseAuth.getInstance();
        //init firebase database service
        database = FirebaseDatabase.getInstance();
        //we aim to "Users" node
        dbUsersNodeRef = database.getReference().child("Users");
        //we aim to "Chat Request" node
        dbChatRequestNodeRef = database.getReference().child("Chat_Requests");
        //we create "Contacts" node
        contactsNodeRef = database.getReference().child("Contacts");
    }

    /**
     * in this method we receive the unique user Id given by firebase to any user
     * coming from the "AllUsersActivity".
     * @return
     */
    private String receiveUserId() {
       otherUserId = getIntent().getStringExtra("otherUserId");
        Log.i(TAG, "receiveUserId: user ID: " + otherUserId);
        return otherUserId;
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

         username = dataSnapshot.child("name").getValue().toString();
         status = dataSnapshot.child("status").getValue().toString();
         imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();

        usernameOtherUser.setText(username);
        statusOtherUser.setText(status);
        if (imageThumbnail.equals("imgThumbnail")){
            otherUserImg.setImageResource(R.drawable.profile_image);
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
        current_database_state = "not_friend_yet";

        chatRequestStatus();


            //in case the current user open it's own profile in the "all users" page
        if (senderRequestUserId.equals(otherUserId)){
            //we hide "send request" button
            buttonSendRequest.setVisibility(View.INVISIBLE);

        } else{

            buttonSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //in case there is no request sent yet
                    if (current_database_state.equals("not_friend_yet")){

                        sendChatRequest();
                    }
                    //in case the request it's been sent
                    if (current_database_state.equals("request_sent")){

                        cancelChatRequest();
                    }
                    //in case the user has received a chat request
                    if(current_database_state.equals("request_received")){

                        //accept the chat request
                        acceptRequest();
                    }
                    if (current_database_state.equals("contact_added")){

                        alertMessage(getString(R.string.deleteContact), getString(R.string.deleteContactMessage));
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

                if (dataSnapshot.hasChild(otherUserId)) {

                    /*
                    here we get the type of request in order to show the user who sends the request
                    and the users whom receives the request the respective UI
                    */
                    String request_type = dataSnapshot.child(otherUserId).child("request_type").getValue().toString();

                    //if the user sends a chat request
                    if (request_type.equals("sent")){

                        current_database_state = "request_sent";
                        buttonSendRequest.setText(getString(R.string.cancelChatRequest));
                        buttonSendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark) );
                    }
                    //in case the user receives a chat request
                    else if (request_type.equals("received")){

                        current_database_state = "request_received";
                        buttonSendRequest.setText(getString(R.string.acceptChatRequest));
                        buttonSendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        buttonRejectRequest.setVisibility(View.VISIBLE);

                        buttonRejectRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //if user click "reject button" we don't do the binding in the DB
                                cancelChatRequest();
                            }
                        });
                    }

                }
                //here in this part we update the UI from the user sending the request
                else {

                    contactsNodeRef.child(senderRequestUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(otherUserId)){
                                //here we update the UI and database status of the user who sent the request
                                current_database_state = "contact_added";
                                buttonSendRequest.setText(getString(R.string.removeContact));

                                buttonRejectRequest.setVisibility(View.VISIBLE);
                                buttonRejectRequest.setEnabled(true);
                                buttonRejectRequest.setText(getString(R.string.sendMessage));
                                buttonRejectRequest.setBackgroundColor(getResources()
                                        .getColor(R.color.colorPrimaryDark));
                                buttonRejectRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //take the current user to the chat room with new friend
                                        goToChatRoom();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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

        dbChatRequestNodeRef.child(senderRequestUserId).child(otherUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                /*
                if we created the first 2 nodes now we create another 2 nodes (1 for the request receiver
                and the other for the request sender)
                */
                if (task.isSuccessful()){

                    dbChatRequestNodeRef.child(otherUserId).child(senderRequestUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    //here we update the UI in the "all users" page
                                    if (task.isSuccessful()){

                                        buttonSendRequest.setEnabled(true);
                                        current_database_state = "request_sent";
                                        buttonSendRequest.setText(getString(R.string.cancelChatRequest));

                                        //show the user the request has been successfully sent
                                        SnackbarHelper.showSnackBarLong(coordinatorLayout, getString(R.string.chatRequestSent));

                                    }
                                    //if something goes wrong show message to the user
                                    else{
                                        Toast.makeText(OtherUserProfileActivity.this,
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
            }
        });


    }

    /**
     * this method is in charge of removing the chat request info from both nodes created
     * when a user send a chat request
     */
    private void cancelChatRequest() {

        dbChatRequestNodeRef.child(senderRequestUserId).child(otherUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){


                            dbChatRequestNodeRef.child(otherUserId).child(senderRequestUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //here we update the UI in the "all users" page
                                            if (task.isSuccessful()){

                                                buttonSendRequest.setEnabled(true);
                                                current_database_state = "not_friend_yet";
                                                buttonSendRequest.setText(getString(R.string.sendChatRequest));


                                                //show the user the request has been canceled
                                                SnackbarHelper.showSnackBarLongRed(coordinatorLayout,
                                                                  getString(R.string.canceledChatRequest));

                                                buttonSendRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                                buttonRejectRequest.setVisibility(View.INVISIBLE);
                                                buttonRejectRequest.setEnabled(false);

                                            }
                                            //if something goes wrong show message to the user
                                            else{
                                                Toast.makeText(OtherUserProfileActivity.this,
                                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    });



                        }
                        //if something goes wrong show message to the user
                        else{
                            Toast.makeText(OtherUserProfileActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }


                    }
                });


    }


    /**
     * this method contains the logic of the db when a user accepts a request chat,
     * meaning that we create a new db node named "Contacts" and we must remove the request saved
     * in the "Chats Requests" node
     */
    private void acceptRequest() {


        //here we create the "contacts" node for the user sending the request
        contactsNodeRef.child(senderRequestUserId).child(otherUserId)
                .child("contact_status").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    //here we create the "contacts" node for the user receiving the request
                    contactsNodeRef.child(otherUserId).child(senderRequestUserId)
                            .child("contact_status").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                //here we delete the info of the user sending the request saved in the "Chat request" node
                                dbChatRequestNodeRef.child(senderRequestUserId).child(otherUserId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    //here we delete the info of the user receiving the request saved in the "Chat request" node
                                                    dbChatRequestNodeRef.child(otherUserId).child(senderRequestUserId)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    //here we update the UI in the "all users" page
                                                                    if (task.isSuccessful()){

                                                                        buttonSendRequest.setEnabled(true);
                                                                        current_database_state = "contact_added";
                                                                        buttonSendRequest.setText(getString(R.string.removeContact));
                                                                        buttonSendRequest.setBackgroundColor(getResources()
                                                                                .getColor(R.color.colorPrimaryDark));


                                                                        SnackbarHelper.showSnackBarLong(coordinatorLayout,
                                                                                         getString(R.string.chatRequestAccepted));
                                                                        buttonRejectRequest.setVisibility(View.VISIBLE);
                                                                        buttonRejectRequest.setEnabled(true);
                                                                        buttonRejectRequest.setText(getString(R.string.sendMessage));
                                                                        buttonRejectRequest.setBackgroundColor(getResources()
                                                                                .getColor(R.color.colorPrimaryDark));

                                                                    }
                                                                    //if something goes wrong show message to the user
                                                                    else{
                                                                        Toast.makeText(OtherUserProfileActivity.this,
                                                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }


                                                                }
                                                            });
                                                }
                                                //if something goes wrong show message to the user
                                                else{
                                                    Toast.makeText(OtherUserProfileActivity.this,
                                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });


                            }
                            //if something goes wrong show message to the user
                            else{
                                Toast.makeText(OtherUserProfileActivity.this,
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });



                }
                //if something goes wrong show message to the user
                else{
                    Toast.makeText(OtherUserProfileActivity.this,
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void removeContact() {

        contactsNodeRef.child(senderRequestUserId).child(otherUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){


                            contactsNodeRef.child(otherUserId).child(senderRequestUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //here we update the UI in the "all users" page
                                            if (task.isSuccessful()){

                                                buttonSendRequest.setEnabled(true);
                                                current_database_state = "not_friend_yet";
                                                buttonSendRequest.setText(getString(R.string.sendChatRequest));

                                                //show the user the request has been canceled
                                                SnackbarHelper.showSnackBarLongRed(coordinatorLayout,
                                                        getString(R.string.contactRemoved));

                                                buttonSendRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                                buttonRejectRequest.setVisibility(View.INVISIBLE);
                                                buttonRejectRequest.setEnabled(false);

                                            }
                                            //if something goes wrong show message to the user
                                            else{
                                                Toast.makeText(OtherUserProfileActivity.this,
                                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });



                        }
                        //if something goes wrong show message to the user
                        else{
                            Toast.makeText(OtherUserProfileActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }

    /**
     * intent to take the user from "all users" page to chat room with the contact
     */
    private void goToChatRoom(){

        Intent intentChatRoom = new Intent(OtherUserProfileActivity.this, ChatActivity.class);
        intentChatRoom.putExtra("contactID", otherUserId);
        intentChatRoom.putExtra("contactName", username);
        intentChatRoom.putExtra("contactImage", imageThumbnail);
        startActivity(intentChatRoom);
    }
    /**
     * this method contains the pop-up message when user wants to remove a contact
     * (standard alert dialog)
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

                        removeContact();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();

        return popUpWindow;
    }

}


