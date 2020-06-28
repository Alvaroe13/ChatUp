package com.example.alvar.chatapp.activities;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Notifications.ChatRequestNotification;
import com.example.alvar.chatapp.Notifications.Data;
import com.example.alvar.chatapp.Notifications.NotificationAPI;
import com.example.alvar.chatapp.Notifications.PushNotification;
import com.example.alvar.chatapp.Notifications.RequestNotification;
import com.example.alvar.chatapp.Notifications.ResponseFCM;
import com.example.alvar.chatapp.Notifications.RetrofitClient;
import com.example.alvar.chatapp.Notifications.Token;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;

public class OtherUserProfileActivity extends AppCompatActivity {
    //log
    private static final String TAG = "OtherUserProfilePage";
    //firebase
    private FirebaseAuth auth;
    private DatabaseReference dbUsersNodeRef, dbChatRequestNodeRef, contactsNodeRef, dbChatsNodeRef, dbTokensNodeRef, dbChatListRef;
    private ValueEventListener removeListener;
    //ui elements
    private CircleImageView otherUserImg;
    private TextView usernameOtherUser, statusOtherUser;
    private Button buttonFirst, buttonSecond;
    private CoordinatorLayout coordinatorLayout;
    //vars
    private String contactID, currentUserID;
    private String current_database_state = "not_friend_yet";
    private String username, status, imageThumbnail, imageProfile;
    private NotificationAPI apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        initFirebase();
        receiveUserId();
        retrieveInfo();
        bindUI();
        manageChatRequest();
        imageProfilePressed();
        retrofit();

    }

    private void bindUI() {
        otherUserImg = findViewById(R.id.otherUsersImgProf);
        usernameOtherUser = findViewById(R.id.usernameOtherUser);
        statusOtherUser = findViewById(R.id.statusOtherUser);
        buttonFirst = findViewById(R.id.buttonSendRequest);
        buttonSecond = findViewById(R.id.buttonDeclineRequest);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //nodes
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
        dbChatRequestNodeRef = database.getReference().child(getString(R.string.chats_requests_ref));
        contactsNodeRef = database.getReference().child(getString(R.string.contacts_ref));
        dbChatsNodeRef = database.getReference().child(getString(R.string.chats_ref)).child(getString(R.string.messages_ref));
        dbTokensNodeRef = database.getReference().child("Tokens");
        dbChatListRef = database.getReference().child("ChatList");
    }

    /**
     * in this method we receive the unique user Id given by firebase to any user
     * coming from the "AllUsersActivity".
     *
     * @return
     */
    private String receiveUserId() {
        contactID = getIntent().getStringExtra("otherUserId");
        Log.i(TAG, "receiveUserId: user ID: " + contactID);
        return contactID;
    }

    /**
     * here in this method is the logic to fetch info from the database
     */
    private void retrieveInfo() {

        dbUsersNodeRef.child(receiveUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    setInfo(dataSnapshot);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * this method is in charge of setting up the info fetched from the db into the UI
     *
     * @param dataSnapshot
     */
    private void setInfo(DataSnapshot dataSnapshot) {

        username = dataSnapshot.child(getString(R.string.name_db)).getValue().toString();
        status = dataSnapshot.child(getString(R.string.status_db)).getValue().toString();
        imageThumbnail = dataSnapshot.child(getString(R.string.imageThumbnail_db)).getValue().toString();
        imageProfile = dataSnapshot.child(getString(R.string.image_db)).getValue().toString();

        usernameOtherUser.setText(username);
        statusOtherUser.setText(status);

        //GLIDE
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(options)
                .load(imageThumbnail)
                .into(otherUserImg);
    }

    /**
     * this method is in charge of managing the chat requests
     */
    private void manageChatRequest() {

        //we get current user id
        currentUserID = auth.getCurrentUser().getUid();
        current_database_state = "not_friend_yet";

        chatRequestStatus();


        //in case the current user open it's own profile in the "all users" page
        if (currentUserID.equals(contactID)) {
            //we hide "send request" button
            buttonFirst.setVisibility(View.INVISIBLE);

        } else {

            buttonFirst.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //in case there is no request sent yet
                    if (current_database_state.equals("not_friend_yet")) {

                        sendChatRequest();
                    }
                    //in case the request it's been sent
                    if (current_database_state.equals("request_sent")) {

                        cancelChatRequest();
                    }
                    //in case the user has received a chat request
                    if (current_database_state.equals("request_received")) {

                        //accept the chat request
                        acceptRequest();
                    }
                    if (current_database_state.equals("contact_added")) {

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


        dbChatRequestNodeRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(contactID)) {

                    /*
                    here we get the type of request in order to show the user who sends the request
                    and the users whom receives the request the respective UI
                    */
                    String request_type = dataSnapshot.child(contactID).child("request_type").getValue().toString();

                    //if the user sends a chat request
                    if (request_type.equals("sent")) {

                        current_database_state = "request_sent";
                        buttonFirst.setText(getString(R.string.cancelChatRequest));
                        buttonFirst.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    }
                    //in case the user receives a chat request
                    else if (request_type.equals("received")) {

                        current_database_state = "request_received";
                        buttonFirst.setText(getString(R.string.acceptChatRequest));
                        buttonFirst.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        buttonSecond.setVisibility(View.VISIBLE);

                        buttonSecond.setOnClickListener(new View.OnClickListener() {
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

                    contactsNodeRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(contactID)) {
                                //here we update the UI and database status of the user who sent the request
                                current_database_state = "contact_added";
                                buttonFirst.setText(getString(R.string.removeContact));

                                buttonSecond.setVisibility(View.VISIBLE);
                                buttonSecond.setEnabled(true);
                                buttonSecond.setText(getString(R.string.sendMessage));
                                buttonSecond.setBackgroundColor(getResources()
                                        .getColor(R.color.colorPrimaryDark));
                                buttonSecond.setOnClickListener(new View.OnClickListener() {
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

        dbChatRequestNodeRef.child(currentUserID).child(contactID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                /*
                if we created the first 2 nodes now we create another 2 nodes (1 for the request receiver
                and the other for the request sender)
                */
                        if (task.isSuccessful()) {

                            dbChatRequestNodeRef.child(contactID).child(currentUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //here we update the UI in the "all users" page
                                            if (task.isSuccessful()) {

                                                buttonFirst.setEnabled(true);
                                                current_database_state = "request_sent";
                                                buttonFirst.setText(getString(R.string.cancelChatRequest));

                                                sendNotification();


                                            }
                                            //if something goes wrong show message to the user
                                            else {
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

        dbChatRequestNodeRef.child(currentUserID).child(contactID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {


                            dbChatRequestNodeRef.child(contactID).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //here we update the UI in the "all users" page
                                            if (task.isSuccessful()) {

                                                buttonFirst.setEnabled(true);
                                                current_database_state = "not_friend_yet";
                                                buttonFirst.setText(getString(R.string.sendChatRequest));


                                                //show the user the request has been canceled
                                                SnackbarHelper.showSnackBarLongRed(coordinatorLayout,
                                                        getString(R.string.canceledChatRequest));

                                                buttonFirst.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                                buttonSecond.setVisibility(View.INVISIBLE);
                                                buttonSecond.setEnabled(false);

                                            }
                                            //if something goes wrong show message to the user
                                            else {
                                                Toast.makeText(OtherUserProfileActivity.this,
                                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    });


                        }
                        //if something goes wrong show message to the user
                        else {
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
        contactsNodeRef.child(currentUserID).child(contactID)
                .child("contact_status").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    //here we create the "contacts" node for the user receiving the request
                    contactsNodeRef.child(contactID).child(currentUserID)
                            .child("contact_status").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                //here we delete the info of the user sending the request saved in the "Chat request" node
                                dbChatRequestNodeRef.child(currentUserID).child(contactID)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    //here we delete the info of the user receiving the request saved in the "Chat request" node
                                                    dbChatRequestNodeRef.child(contactID).child(currentUserID)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    //here we update the UI in the "all users" page
                                                                    if (task.isSuccessful()) {

                                                                        buttonFirst.setEnabled(true);
                                                                        current_database_state = "contact_added";
                                                                        buttonFirst.setText(getString(R.string.removeContact));
                                                                        buttonFirst.setBackgroundColor(getResources()
                                                                                .getColor(R.color.colorPrimaryDark));


                                                                        SnackbarHelper.showSnackBarLong(coordinatorLayout,
                                                                                getString(R.string.chatRequestAccepted));
                                                                        buttonSecond.setVisibility(View.VISIBLE);
                                                                        buttonSecond.setEnabled(true);
                                                                        buttonSecond.setText(getString(R.string.sendMessage));
                                                                        buttonSecond.setBackgroundColor(getResources()
                                                                                .getColor(R.color.colorPrimaryDark));

                                                                    }
                                                                    //if something goes wrong show message to the user
                                                                    else {
                                                                        Toast.makeText(OtherUserProfileActivity.this,
                                                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }


                                                                }
                                                            });
                                                }
                                                //if something goes wrong show message to the user
                                                else {
                                                    Toast.makeText(OtherUserProfileActivity.this,
                                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });


                            }
                            //if something goes wrong show message to the user
                            else {
                                Toast.makeText(OtherUserProfileActivity.this,
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
                //if something goes wrong show message to the user
                else {
                    Toast.makeText(OtherUserProfileActivity.this,
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * this method is in charge of removing a contact
     */
    private void removeContact() {

        contactsNodeRef.child(currentUserID).child(contactID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {


                            contactsNodeRef.child(contactID).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //here we update the UI in the "all users" page
                                            if (task.isSuccessful()) {

                                                buttonFirst.setEnabled(true);
                                                current_database_state = "not_friend_yet";
                                                buttonFirst.setText(getString(R.string.sendChatRequest));

                                                //we delete chat between contacts
                                                eraseChatRoom(currentUserID, contactID);

                                                //show the user the request has been canceled
                                                SnackbarHelper.showSnackBarLongRed(coordinatorLayout,
                                                        getString(R.string.contactRemoved));

                                                buttonFirst.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                                buttonSecond.setVisibility(View.INVISIBLE);
                                                buttonSecond.setEnabled(false);

                                            }
                                            //if something goes wrong show message to the user
                                            else {
                                                Toast.makeText(OtherUserProfileActivity.this,
                                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });


                        }
                        //if something goes wrong show message to the user
                        else {
                            Toast.makeText(OtherUserProfileActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }

    /**
     * intent to take the user from "all users" page to chat room with the contact
     */
    private void goToChatRoom() {

        Intent intentChatRoom = new Intent(OtherUserProfileActivity.this, ChatActivity.class);
        intentChatRoom.putExtra(CONTACT_ID, contactID);
        intentChatRoom.putExtra(CONTACT_NAME, username);
        intentChatRoom.putExtra(CONTACT_IMAGE, imageThumbnail);
        intentChatRoom.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentChatRoom);
        finish();
    }

    /**
     * this method contains the pop-up message when user wants to remove a contact
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

                        removeContact();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();

        return popUpWindow;
    }

    private void retrofit() {
        apiService = RetrofitClient.getRetrofit().create(NotificationAPI.class);
    }


    /**
     * method in charge of saving information in the "Notifications" node
     */
    private void sendNotification() {

        //TODO send push notification when chat request is sent.

        Log.d(TAG, "sendNotification: request notification method called");

        dbTokensNodeRef.child(contactID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Token deviceToken = dataSnapshot.getValue(Token.class);
                    Log.d(TAG, "onDataChange PUSH_NOTIFICATION_TO_SERVER: token retrieved from firebase: " + deviceToken.getToken());

                    String title = "Chat Request";
                    String message = "You have a new chat request";

                    RequestNotification notificationBody = new RequestNotification(title , message);


                    ChatRequestNotification notification =
                            new ChatRequestNotification( notificationBody , deviceToken.getToken());

                    apiService.requestNotification(notification).enqueue(new Callback<ResponseFCM>() {
                        @Override
                        public void onResponse(Call<ResponseFCM> call, Response<ResponseFCM> response) {
                            if (response.code() == 200) {
                                Log.d(TAG, "onResponse: RETROFIT notification  sent ");
                            } else {
                                Log.e(TAG, "onResponse: code=  " + response.code());
                                Log.e(TAG, "onResponse: error=  " + response.errorBody());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseFCM> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void eraseChatRoom(final String user1, final String user2) {


        dbChatListRef.child(user1).child(user2).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    dbChatListRef.child(user2).child(user1).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            deleteChat(user1, user2);
                            Intent i = new Intent(OtherUserProfileActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();

                        }
                    });

                }


            }
        });


    }

    /**
     * method in charge of deleting chats in fragment chats (called in remove contact)
     */
    private void deleteChat(final String currentUserID, final String contactID) {

        Log.d(TAG, "deleteChat: LLAMADOOOOOOOO");

        removeListener =
        dbChatsNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Messages message = ds.getValue(Messages.class);

                    try {

                        if (message.getSenderID().equals(currentUserID) && message.getReceiverID().equals(contactID) ||
                                message.getSenderID().equals(contactID) && message.getReceiverID().equals(currentUserID)) {

                            ds.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dbChatsNodeRef.removeEventListener(removeListener);
                                    /*Intent i = new Intent(OtherUserProfileActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();*/
                                }
                            });

                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: error = " + e.getMessage());
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void imageProfilePressed() {

        otherUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToImageBigRoom();
            }
        });

    }

    private void goToImageBigRoom() {

        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("messageContent", imageProfile);
        startActivity(intent);
    }


}


