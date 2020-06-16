package com.example.alvar.chatapp.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.example.alvar.chatapp.Activities.ChatActivity;
import com.example.alvar.chatapp.Notifications.NotificationHandler;
import com.example.alvar.chatapp.Notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidx.annotation.NonNull;

import static com.example.alvar.chatapp.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Constant.TOKEN_PREFS;
import static com.example.alvar.chatapp.Constant.USER_ID_PREFS;
import static com.example.alvar.chatapp.Constant.USER_INFO_PREFS;

public class FirebaseNotificationService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseService";

    //firebase
    private FirebaseUser userFirebase;
    private FirebaseDatabase database;
    private DatabaseReference tokensNodeRef;
    //vars
    private String userIDPrefs;
    private  String userID2;

    /**
     * method triggered whenever there's a new instance of this app therefore a new token is generated
     * @param token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "onNewToken: token: " + token);

        userFirebase = FirebaseAuth.getInstance().getCurrentUser();

        if (userFirebase !=null){
            saveTokenOnPreferences(token);
            getUserIDPrefs();
            initFirebase();
            saveNewTokenInRemoteDB(token);
        }

    }

    private void saveTokenOnPreferences( String token) {
        SharedPreferences prefs = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_PREFS, token);
        editor.apply();
    }

    private String getUserIDPrefs(){
        SharedPreferences userID = getSharedPreferences(USER_INFO_PREFS, Context.MODE_PRIVATE);
        userID.getString(USER_ID_PREFS, "no userID saved in prefs");
        userIDPrefs = userID.toString();
        return userIDPrefs;
    }
    private void initFirebase(){
        //firebase db init
        database = FirebaseDatabase.getInstance();
        //get access to "Users" branch of db
        tokensNodeRef = database.getReference().child("Tokens");
    }

    private void saveNewTokenInRemoteDB(String newToken){

        Token token = new Token(newToken);

        tokensNodeRef.child(userIDPrefs).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Log.d(TAG, "onComplete: error saving new token in firebase " + task.getException()); ;
                    return;
                }
                Log.d(TAG, "onComplete: done successfully");

            }
        });
    }


    /**
     * this method is triggered whenever there is an incoming notification from the cloud messaging service
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "message received NOTIFICATION RECEIVED");



        userFirebase = FirebaseAuth.getInstance().getCurrentUser();

        if (userFirebase !=null){
            getUserIDPrefs();
            initFirebase();
            userID2 = userFirebase.getUid();
        }

        //params in .get() must match with the one's in our Data model.
        String senderID = remoteMessage.getData().get("senderID");
        String userID = remoteMessage.getData().get("recipientUserID");


        if (userFirebase != null && !senderID.equals(userID2) ){
            if (!getUserIDPrefs().equals(userID)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendNotification(remoteMessage);
                }else{
                    sendNotification(remoteMessage);

                }
            }
        }


    }

    /**
     * this method is in charge of pushing the pop up notification for an incoming notification ONLY.
     * @param remoteMessage
     */
    private void sendNotification(RemoteMessage remoteMessage) {

        Random random = new Random();
        int notificationID = random.nextInt();

        String senderID = remoteMessage.getData().get("senderID");
        String message = remoteMessage.getData().get("message");
        String title = remoteMessage.getData().get("title");

        Log.d(TAG, "sendNotification: NOTIFICATION RECEIVED senderID: " + senderID);
        Log.d(TAG, "sendNotification: NOTIFICATION RECEIVED message: " + message);
        Log.d(TAG, "sendNotification: NOTIFICATION RECEIVED title: " + title);


        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra( CONTACT_ID, senderID);
        Log.d(TAG, "sendNotification: sender user ID : " + senderID);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationHandler notificationHandler = new NotificationHandler(this);
        Notification.Builder builder = notificationHandler.createNotification(title, message, pendingIntent, true);

        notificationHandler.getNotificationManager().notify(1, builder.build());
    //    notificationHandler.showGroupNotification(true);
    }
        /* intent.putExtra( CONTACT_IMAGE, image);
        intent.putExtra( CONTACT_NAME, contactName);*/
}


