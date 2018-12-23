package com.example.alvar.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    //ui elements
    private Toolbar toolbarGroupChat;
    private ScrollView scrollView;
    private TextView messageGroupText;
    private EditText writeMessageEditText;
    private ImageButton buttonSend;
    //vars
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        bindUI();
        //here we receive the name of the group
        groupName = getIntent().getExtras().get("Group Name").toString();

        setToolbar(groupName);



    }

    /**
     * method in charge of initializing ui elements
     */
    private void bindUI() {
        scrollView = findViewById(R.id.scrollView);
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
}
