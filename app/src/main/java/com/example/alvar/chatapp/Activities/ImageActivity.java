package com.example.alvar.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";

    //ui
    private ImageView image;
    //firebase
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //
    private String messageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        initFirebase();
        binUI();
        fetchInfoIntent();
        setImage();
    }


    private void binUI() {

        image = findViewById(R.id.imageBig);
    }

    private void initFirebase(){
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
    }

    private void fetchInfoIntent(){
        messageContent = getIntent().getStringExtra("messageContent");
        Log.i(TAG, "fetchInfoIntent: message content: " + messageContent);
    }


    /**
     * here we show the image
     */
    private void setImage() {

        Glide.with(this).load(messageContent).into(image);

    }






}
