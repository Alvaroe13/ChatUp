package com.example.alvar.chatapp.Activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ObjectsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private static final String TAG = "ChatActivityPage";
    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbMessagesNodeRef;
    //UI elements
    private Toolbar toolbarChat;
    private RecyclerView recyclerViewChat;
    private EditText chatEditText;
    private ImageButton buttonSend;
    private CircleImageView imageProfile;
    private TextView usernameToolbarChat, lastSeenToolbarChat;
    //vars
    private String contactID, currentUserID;
    private String contactName, contactImage;
    private String messageText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fetchInfoIntent();
        initFirebase();
        setToolbar("",true);
        UIElements();
        initRecycleView();
        sendButtonPressed();

        Log.i(TAG, "onCreate: current user: " + currentUserID);
        Log.i(TAG, "onCreate: other user: " + contactID);

    }


    private void UIElements(){
        chatEditText = findViewById(R.id.chatEditText);
        buttonSend = findViewById(R.id.buttonChat);
    }

    /**
     * this method receives de bundles from "ContactsActivity"
     */
    private void fetchInfoIntent(){
        contactID = getIntent().getStringExtra("contactID");
        contactName = getIntent().getStringExtra("contactName");
        contactImage = getIntent().getStringExtra("contactImage");
    }

    private void initFirebase(){

        auth = FirebaseAuth.getInstance();
        //we get current user ID
        currentUserID = auth.getCurrentUser().getUid();

        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbMessagesNodeRef = database.getReference().child("Message");
    }

    /**
     Create toolbar and inflate the custom bar chat bar layout
     */
    private void setToolbar(String title, Boolean backOption){

        toolbarChat = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbarChat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(backOption);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewCustomBar = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(viewCustomBar);

        //UI elements from custom toolbar
        imageProfile = findViewById(R.id.imageToolbarChat);
        usernameToolbarChat = findViewById(R.id.usernameToolbarChat);
        lastSeenToolbarChat = findViewById(R.id.lastSeenChatToolbar);

        //here we set info from bundles into the ui elements in custom toolbar
        usernameToolbarChat.setText(contactName);
        Glide.with(this).load(contactImage).into(imageProfile);

    }

    /**
     * init recyclerView
     */
    private void initRecycleView(){
        Log.i(TAG, "initRecycler: recycler init successful");
        recyclerViewChat = findViewById(R.id.recyclerChat);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
    }

    /**
     * this method handle the click event when send button is pressed
     */
    private void sendButtonPressed() {
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //we get message written by the user
                messageText = chatEditText.getText().toString();

                //if field is empty
                if (messageText.equals("")){
                    //show toast to the user
                    Toast.makeText(ChatActivity.this, "Please write something", Toast.LENGTH_SHORT).show();
                }else{
                    //otherwise we send the message
                    sendMessage();
                }

            }
        });
    }

    /**
     * this method contains the logic of saving the messages in the database
     */
    private void sendMessage() {

        String messageSenderRef = "Message-" + currentUserID + "/" + contactID;
        String messageReceiverRef = "Message-" + contactID + "/" + currentUserID;

        dbMessagesNodeRef.child(currentUserID).child(contactID).push();

        DatabaseReference messageKeyRef = dbMessagesNodeRef;
        String messagePushID = messageKeyRef.getKey();

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("message", messageText);
        messageDetails.put("type", "text");
        messageDetails.put("sender", contactID);

        Map<String, Object> chatUsersInfo = new HashMap<>();
        chatUsersInfo.put(messageSenderRef + "/" + messagePushID , messageDetails);
        chatUsersInfo.put(messageReceiverRef + "/" + messagePushID , messageDetails);

        dbMessagesNodeRef.updateChildren(chatUsersInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //we remove any text enter by the user once it's been sent
        chatEditText.setText("");





    }


}
