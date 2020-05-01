package com.example.alvar.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";

    //ui
    private ImageView image;
    // vars
    private String messageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        binUI();
        setImage();
    }

    /**
     * ui elements
     */
    private void binUI() {
        image = findViewById(R.id.imageBig);
    }


    /**
     * here we show the image
     */
    private void setImage() {

        //in this variable we store the info of any image coming from any activity.
        messageContent = getIntent().getStringExtra("messageContent");
        Log.i(TAG, "fetchInfoIntent: message content: " + messageContent);

        //in case the value is "image" meaning that the user hasn't uploaded any image as profile pic
        if (messageContent.equals("image")){
            image.setImageResource(R.drawable.profile_image);
        }
        else {
         Glide.with(getApplicationContext()).load(messageContent).into(image);
        }
    }

}



