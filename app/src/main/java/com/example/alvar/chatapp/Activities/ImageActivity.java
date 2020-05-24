package com.example.alvar.chatapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";

    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //ui
    private ImageView image;
    // vars
    private String messageContent, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initFirebase();
        binUI();
        setImage();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
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

    /**
     * we use onStart to update user online state in real time
     */
    @Override
    protected void onStart() {
        super.onStart();
        updateDateTime("Online");
    }

    /**
     * if user leaves the room we change online state tu offline
     */
    @Override
    protected void onPause() {
        super.onPause();
        //in case the other close the chat activity the state changes to "offline"
        updateDateTime("Offline");
    }

    /**
     * method in charge of getting the user's current state, time and Date to update in db
     */
    private void updateDateTime(String state){

        String currentTime, currentDate;

        Calendar calendar =  Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MMM/yyyy");
        currentDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        currentTime = time.format(calendar.getTime());

        //lets save all this info in a map to uploaded to the Firebase database.
        //NOTE: we use HashMap instead of an Object because the database doesn't accept a Java Object
        // when the database will be updated when using "updateChildren" whereas when using setValue you can use a Java Object.
        HashMap<String , Object> userState = new HashMap<>();
        userState.put(getString(R.string.state_db), state);
        userState.put(getString(R.string.date_db), currentDate);
        userState.put(getString(R.string.time_db), currentTime);

        dbUsersNodeRef.child(currentUserID).child(getString(R.string.user_state_db)).updateChildren(userState);
    }

}



