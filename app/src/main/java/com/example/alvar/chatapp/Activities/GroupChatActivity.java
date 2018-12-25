package com.example.alvar.chatapp.Activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    //firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersRef, dbCurrentGroupRef, GroupMessageKeyRef;
    //ui elements
    private Toolbar toolbarGroupChat;
    private ScrollView scrollViewGroupChat;
    private TextView messageGroupText;
    private EditText writeMessageEditText;
    private ImageButton buttonSend;
    //vars
    private String groupName, currentUserID, messageDate, messageTime, message, messageKey, username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        bindUI();
        //here we receive the name of the group
        getGroupName();
        setToolbar(groupName);
        initFirebase();
        getUserInfo();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save message in db
                saveMessageInDatabase();
                //erase text from the text field
                writeMessageEditText.setText("");

                //focus scrollView to the last message sent
                scrollViewGroupChat.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }



    private void bindUI() {
        scrollViewGroupChat = findViewById(R.id.groupChatScrollView);
        messageGroupText = findViewById(R.id.chatText);
        writeMessageEditText = findViewById(R.id.editTextWriteMessage);
        buttonSend = findViewById(R.id.buttonSend);
    }

    private void setToolbar(String title){
        //create toolbar
        toolbarGroupChat = findViewById(R.id.toolbarGroupChat);
        //we set the toolbar
        setSupportActionBar(toolbarGroupChat);
        //we pass the title
        getSupportActionBar().setTitle(title);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * This method received in this activity the name of the group to be shown in the toolbar
     * @return
     */
    private String getGroupName(){
        groupName = getIntent().getExtras().get("Group Name").toString();
        return groupName;
    }

    /**
     * we init firebase services here
     */
    private void initFirebase(){

        auth = FirebaseAuth.getInstance();
        //getting user unique ID
        currentUser = auth.getCurrentUser();
        //database
        database = FirebaseDatabase.getInstance();
        //ref init and point to "Users" node from firebase database
        dbUsersRef = database.getReference().child("Users");
        //ref to "Groups" node from firebase database
        dbCurrentGroupRef = database.getReference().child("Groups").child(getGroupName());

    }


    private void getUserInfo() {

        //here we get the unique user ID given by Firebase in the database
        currentUserID = currentUser.getUid();

        dbUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){

                        username = dataSnapshot.child(currentUserID).child("name").getValue().toString();
                        Log.i(TAG, "onDataChange: email : " + username);
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.i(TAG, "onCancelled: error : " + databaseError.getMessage());
            }
        });


    }

    private void saveMessageInDatabase() {

        //we save message written by the user
        message = writeMessageEditText.getText().toString();
        //we get unique id from firebase
        messageKey = dbCurrentGroupRef.push().getKey();

        if (message.equals("")){
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
        } else {

            addMessage();
        }


    }

    /**
     * this method contains the logic to save the message information into the database
     */
    private void addMessage(){

        //this code is to set the date format
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat messageDateFormat = new SimpleDateFormat("MMM/dd/YYYY");
        messageDate = messageDateFormat.format(calendarDate.getTime());

        //this code is to set the time format
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat messageTimeFormat = new SimpleDateFormat("hh:mm a");
        messageTime = messageTimeFormat.format(calendarTime.getTime());

        //we add the info using the hash map
        HashMap<String, Object> hashMapMessage = new HashMap<>();
        dbCurrentGroupRef.updateChildren(hashMapMessage);


        //we set the unique key inside of the "Groups" node
        GroupMessageKeyRef = dbCurrentGroupRef.child(messageKey);
        //we include the rest of the info in the db
        HashMap<String, Object> messageDataMap = new HashMap<>();
            messageDataMap.put("name", username);
            messageDataMap.put("message", message);
            messageDataMap.put("time", messageTime);
            messageDataMap.put("date", messageDate);
        GroupMessageKeyRef.updateChildren(messageDataMap);

    }

    /**
     * method call whenever the activity is execute
     */
    @Override
    protected void onStart() {
        super.onStart();

        dbCurrentGroupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()){
                    showMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                  showMessage(dataSnapshot) ;

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method in charge of showing the message in the chat room
     * @param dataSnapshot
     */
    private void showMessage( DataSnapshot dataSnapshot) {

        String chatDate;
        String chatMessage;
        String chatName ;
        String chatTime;

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while ( iterator.hasNext()){

            chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            messageGroupText.append(
                    chatName + " : \n" +
                    chatMessage + " \n" +
                    chatDate + " \n" +
                    chatTime +"." + "\n\n"
            );

            //focus scrollView to the last message sent
            scrollViewGroupChat.fullScroll(ScrollView.FOCUS_DOWN);
        }


    }
}
