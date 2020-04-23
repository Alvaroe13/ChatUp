package com.example.alvar.chatapp.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.MessageAdapter;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivityPage";

    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbMessagesNodeRef, messagePushID, dbUsersNodeRef;
    //UI elements
    private Toolbar toolbarChat;
    private RecyclerView recyclerViewChat;
    private EditText chatEditText;
    private ImageButton buttonSend, buttonAttachFile;
    private CircleImageView imageProfile, onlineIcon;
    private TextView usernameToolbarChat, lastSeenToolbarChat;
    private LinearLayoutManager linearLayoutManager;
    //vars
    private String contactID, currentUserID;
    private String contactName, contactImage;
    private String messageText;
    private MessageAdapter adapter;
    private List<Messages> messagesList;
    //gallery const
    private static final int GALLERY_REQUEST_NUMBER = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fetchInfoIntent();
        initFirebase();
        setToolbar("",false);
        UIElements();
        initRecycleView();
        sendButtonPressed();
        editTextStatus();
        otherUserState();
        toolbarPressed();
        
        attachFileButtonPressed();


    }

    private void UIElements(){
        chatEditText = findViewById(R.id.chatEditText);
        buttonSend = findViewById(R.id.buttonChat);
        buttonAttachFile = findViewById(R.id.buttonAttachFile);
    }

    /**
     * this method receives de bundles from "ContactsActivity"
     */
    private void fetchInfoIntent(){
        contactID = getIntent().getStringExtra("contactID");
        contactName = getIntent().getStringExtra("contactName");
        contactImage = getIntent().getStringExtra("contactImage");
    }

    /**
     * init firebase services
     */
    private void initFirebase(){

        auth = FirebaseAuth.getInstance();
        //we get current user ID
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbMessagesNodeRef = database.getReference().child("Chats").child("Messages");
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
        onlineIcon = findViewById(R.id.onlineIcon);

        //here we set info from bundles into the ui elements in custom toolbar
        usernameToolbarChat.setText(contactName);
        if (contactImage.equals("imgThumbnail")) {
            imageProfile.setImageResource(R.drawable.profile_image);
        } else {
            Glide.with(getApplicationContext()).load(contactImage).into(imageProfile);
        }


    }



    /**
     * Here in this method we read the current state of the other user in real time to show it
     * in the toolbar.
     */
    private void otherUserState(){

        dbUsersNodeRef.child(contactID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    //here we get the other user's current state and we store it in each var
                    String saveLastSeenDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    String saveLastSeenTime = dataSnapshot.child("userState").child("time").getValue().toString();
                    String saveSate = dataSnapshot.child("userState").child("state").getValue().toString();
                    //retrieving other user's typing state
                    String typingState = dataSnapshot.child("userState").child("typing").getValue().toString();

                    //if typing state in db is yes we should in toolbar that other user is typing
                    if (typingState.equals("yes")) {

                        lastSeenToolbarChat.setText(R.string.typing);

                    } else {
                            //if user is online but not typing we show online on the toolbar
                        if (saveSate.equals("Online")){
                            lastSeenToolbarChat.setText(R.string.activeNow);
                            onlineIcon.setVisibility(View.VISIBLE);

                            //if user is not typing nor "online" we show "offline" on the toolbar.
                        } else if(saveSate.equals("Offline")){
                            lastSeenToolbarChat.setText(getString(R.string.lastSeen) + " " +  saveLastSeenDate + " " + saveLastSeenTime);
                            onlineIcon.setVisibility(View.INVISIBLE);
                        }

                    }


                } else{

                    Toast.makeText(ChatActivity.this, "Error with the network", Toast.LENGTH_SHORT).show();
                }

 
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * init recyclerView
     */
    private void initRecycleView(){

        //instance of arrayList of messages
        messagesList = new ArrayList<>();

        recyclerViewChat = findViewById(R.id.recyclerChat);
        recyclerViewChat.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);

        adapter = new MessageAdapter(ChatActivity.this, messagesList);
        recyclerViewChat.setAdapter(adapter);

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
                if (messageText.equals("")) {
                    //show toast to the user
                    Toast.makeText(ChatActivity.this,
                            getString(R.string.noEmptyFieldAllowed), Toast.LENGTH_SHORT).show();
                } else {
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

        //first we create a ref for sender and receiver to be later saved in the db
        String messageSenderRef =  currentUserID + "/" + contactID;
        String messageReceiverRef = contactID + "/" + currentUserID;

        String lastMessageTime, lastMessageDate;

        Calendar calendar =  Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy");
        lastMessageDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        lastMessageTime = time.format(calendar.getTime());


        messagePushID = dbMessagesNodeRef.child(currentUserID).child(contactID).push();

        String messagePushKey = messagePushID.getKey();

        //this map is for saving the details of the messages
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("message", messageText);
        messageDetails.put("type", "text");
        messageDetails.put("senderID", currentUserID);
        messageDetails.put("receiverID", contactID);
        messageDetails.put("messageDate", lastMessageDate);
        messageDetails.put("messageTime", lastMessageTime);
        messageDetails.put("messageID", messagePushID);
        messageDetails.put("seen", false);

        //this map is for the info shown in the "Messages" node
        Map<String, Object> chatUsersInfo = new HashMap<>();
        chatUsersInfo.put(messageSenderRef + "/" + messagePushKey , messageDetails);
        chatUsersInfo.put(messageReceiverRef + "/" + messagePushKey , messageDetails);

        dbMessagesNodeRef.updateChildren(chatUsersInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: Message sent successfully ");
                } else {
                    Log.i(TAG, "onComplete: something went wrong");
                }
            }
        });

        //we remove any text enter by the user once it's been sent
        chatEditText.setText("");

    }

    /**
     * we use onStart to update and display all the message every time a user send messages
     */
    @Override
    protected void onStart() {
        super.onStart();


        updateDateTime("Online");

        dbMessagesNodeRef.child(currentUserID).child(contactID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        messagesList.clear();

                        for (DataSnapshot info : dataSnapshot.getChildren()){

                                Messages messages = info.getValue(Messages.class);

                                messagesList.add(messages);

                                adapter.notifyDataSetChanged();

                                recyclerViewChat.smoothScrollToPosition(recyclerViewChat.getAdapter().getItemCount());

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    protected void onPause() {
        super.onPause();
        //in case the other close the chat activity the state changes to "offline"
        updateDateTime("Offline");
        //in the the other user close the chat activity the typing state changes to "no"
        typingState("no");
    }

    /**
     * method in charge of getting the user's current state, time and Date to update in db
     */
    private void updateDateTime(String state){

        String currentTime, currentDate;

        Calendar calendar =  Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MMM/yyyy");
        currentDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm aaa");
        currentTime = time.format(calendar.getTime());

        //lets save all this info in a map to uploaded to the Firebase database.
        //NOTE: we use HashMap instead of an Object because the database doesn't accept a Java Object
        // when the database will be updated when using "updateChildren" whereas when using setValue you can use a Java Object.
        HashMap<String , Object> userState = new HashMap<>();
        userState.put("state", state);
        userState.put("date", currentDate);
        userState.put("time", currentTime);

        dbUsersNodeRef.child(currentUserID).child("userState").updateChildren(userState);

    }

    /**
     * method in charge of checking if edit txt is empty or not
     */
    private void editTextStatus() {

        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = s.toString();

                //in edit text is empty we set typing state as "no"
                if (text.isEmpty()){
                    typingState("no");
                }
                    //if edit text is not empty we set typing state as "yes"
                    else {
                    typingState("yes");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * method oni charge of taking the user to other user's profile when toolbar pressed in the chat room
     */
    private void toolbarPressed() {

        toolbarChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentOtherUserProf = new Intent(ChatActivity.this, OtherUserProfileActivity.class);
                //we send user id through an intent
                intentOtherUserProf.putExtra("otherUserId" , contactID);
                startActivity(intentOtherUserProf);
            }
        });
    }

    /**
     * method in charge of updating the other user's typing state in the db in real time
     * @param typingState
     */
    private void typingState(String typingState){

        HashMap<String, Object> typingStateMap = new HashMap<>();
        typingStateMap.put("typing" , typingState);

        dbUsersNodeRef.child(currentUserID).child("userState").updateChildren(typingStateMap);

    }

    /**
     * this method ensures when back button is pressed when in the chat room it takes the user to the
     * main activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(intent);

    }

    /**
     * when attach file button is pressed
     */
    private void attachFileButtonPressed() {
        buttonAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertDialog();
                
            }
        });
    }

    /**
     * here we show the option for the user to choose.
     */
    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Choose file");
        builder.setIcon(R.drawable.send_files);
        builder.setPositiveButton("Document", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ChatActivity.this, "Document option selected", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openGallery();
            }
        });

        builder.show();
    }

    /**
     * this method opens gallery to select the image
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), GALLERY_REQUEST_NUMBER);
    }



}
