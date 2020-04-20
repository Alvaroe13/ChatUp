package com.example.alvar.chatapp.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import com.example.alvar.chatapp.Activities.AnswerRequestActivity;
import com.example.alvar.chatapp.R;

public class NotificationHandler extends ContextWrapper {


    private NotificationManager manager;

    public static final String CHANNEL_HIGH_NAME="1";
    public static final String CHANNEL_LOW_NAME="2";
    private final String CHANNEL_HIGH_ID="HIGH CHANNEL";
    private final String CHANNEL_LOW_ID="LOW CHANNEL";



    public NotificationHandler(Context context) {
        super(context);
        createChannels();
    }

    public NotificationManager getManager(){
        if (manager == null){
            manager = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        }
        return manager;
    }



    private void createChannels() {
        // lets first check if user's device is Android Oreo or a newer version
        if (Build.VERSION.SDK_INT >= 26 ){
            //lets create high channel
            NotificationChannel highChannel =
                        new NotificationChannel(CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_HIGH);

             highChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            //Lets create low channel
            NotificationChannel lowChannel =
                        new NotificationChannel(CHANNEL_LOW_ID , CHANNEL_LOW_NAME, NotificationManager.IMPORTANCE_LOW);

            lowChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);


            getManager().createNotificationChannel(highChannel);
            getManager().createNotificationChannel(lowChannel);
        }
    }

    public Notification.Builder createNotification(String title, String message, Boolean isHighImportance, String otherUserId){

        //lets first check what OS version is running in user's device
        if (Build.VERSION.SDK_INT >= 26 ){
            if (isHighImportance){
                return this.createNotificationWithChannel(title, message, CHANNEL_HIGH_ID, otherUserId);
            }
            return  this.createNotificationWithChannel(title, message, CHANNEL_LOW_ID, otherUserId);
        }
        return this.createNotificationWithOutChannel(title, message, otherUserId);
    }


    private Notification.Builder createNotificationWithChannel(String title, String message, String channelId, String otherUserID){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            Intent intent = new Intent(this, AnswerRequestActivity.class);
            intent.putExtra("otherUserID", otherUserID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);



            return new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(androidx.core.R.drawable.notification_tile_bg)
                    .setColor(getColor(R.color.color_red))
                    .setShowWhen(true)
                    .setAutoCancel(true);
        }
        return null;
    }

    private Notification.Builder createNotificationWithOutChannel(String title, String message, String otherUserID){

            return new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(androidx.core.R.drawable.notification_tile_bg)
                    .setAutoCancel(true);

    }
}
