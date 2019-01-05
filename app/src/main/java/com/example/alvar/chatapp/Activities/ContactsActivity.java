package com.example.alvar.chatapp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.alvar.chatapp.R;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";

    //UI elements
    private Toolbar toolbarContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        setToolbar("Contacts", true);
    }

    private void setToolbar(String title, Boolean backOption){
        toolbarContacts = findViewById(R.id.toolbarContacts);
        setSupportActionBar(toolbarContacts);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }



}
