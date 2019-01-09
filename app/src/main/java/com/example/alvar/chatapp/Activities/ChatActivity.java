package com.example.alvar.chatapp.Activities;

import android.content.Context;
import android.support.annotation.NonNull;
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

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private static final String TAG = "ChatActivityPage";
    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //UI elements
    private Toolbar toolbarChat;
    private RecyclerView recyclerViewChat;
    private EditText chatEditText;
    private ImageButton buttonSend;
    private CircleImageView imageProfile;
    private TextView usernameToolbarChat, lastSeenToolbarChat;
    //vars
    private String contactID, currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //we receive the contact ID from the "ContactsActivity"
        contactID = getIntent().getStringExtra("contactID");

        initFirebase();
        setToolbar("",true);
        UI();
        initRecycleView();
        fetchInfo();


    }

    private void UI(){

        chatEditText = findViewById(R.id.chatEditText);
        buttonSend = findViewById(R.id.buttonSend);

    }

    private void initFirebase(){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
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

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewCustomBar = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(viewCustomBar);

        imageProfile = findViewById(R.id.imageToolbarChat);
        usernameToolbarChat = findViewById(R.id.usernameToolbarChat);
        lastSeenToolbarChat = findViewById(R.id.lastSeenChatToolbar);


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
     * fetch info from the db and set it into the chat toolbar
     */
    private void fetchInfo(){


        dbUsersNodeRef.child(contactID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String usernameContact = dataSnapshot.child("name").getValue().toString();
                    String imageContact = dataSnapshot.child("imageThumbnail").getValue().toString();

                    Log.i(TAG, "onDataChange: name: " + usernameContact);
                    Log.i(TAG, "onDataChange: image: " + imageContact);

                    usernameToolbarChat.setText(usernameContact);
                    if (imageContact.equals("imgThumbnail")){
                        imageProfile.setImageResource(R.drawable.imgdefault);
                    }else{
                        Glide.with(ChatActivity.this).load(imageContact).into(imageProfile);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



}
