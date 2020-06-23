package com.example.alvar.chatapp.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.alvar.chatapp.Activities.MainActivity;
import com.example.alvar.chatapp.R;

import androidx.annotation.RequiresApi;

public class NotificationHandler extends ContextWrapper {


    private NotificationManager manager;

    public static final String CHANNEL_HIGH_NAME = "1";
    public static final String CHANNEL_LOW_NAME = "2";
    public static final String REQUEST_CHANEL_NAME = "3";
    private final String CHANNEL_HIGH_ID = "HIGH CHANNEL";
    private final String CHANNEL_LOW_ID = "LOW CHANNEL";
    private final String REQUEST_CHANNEL_ID = "REQUEST CHANNEL";
    public final int GROUP_ID = 100;
    private final String GROUP_NAME = "GROUP NAME";


    public NotificationHandler(Context context) {
        super(context);

        createChannels();
    }

    public NotificationManager getNotificationManager(){
        if (manager == null){
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    private void createChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            //lets create high channel
            NotificationChannel channelHigh = new NotificationChannel(CHANNEL_HIGH_ID,
                                                        CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            channelHigh.enableLights(true);
            channelHigh.enableVibration(true);
            channelHigh.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getNotificationManager().createNotificationChannel(channelHigh);


            //----------------------- CHANNEL 2-----------------------------//

            //Lets create low channel
            NotificationChannel lowChannel = new NotificationChannel(CHANNEL_LOW_ID ,
                                                        CHANNEL_LOW_NAME, NotificationManager.IMPORTANCE_LOW);

            lowChannel.enableLights(true);
            lowChannel.enableVibration(true);
            lowChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getNotificationManager().createNotificationChannel(lowChannel);
        }


    }



    public Notification.Builder createNotification(String title, String message, PendingIntent pendingIntent, Boolean isHighImportance ){

        //lets first check what OS version is running in user's device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            if (isHighImportance){
                return this.createNotificationWithChannel(title, message, pendingIntent, CHANNEL_HIGH_ID);
            }
            return  this.createNotificationWithChannel(title, message, pendingIntent, CHANNEL_LOW_ID);
        }
        return this.createNotificationWithOutChannel(title, message, pendingIntent);
    }


    private Notification.Builder createNotificationWithChannel(String title, String message, PendingIntent pendingIntent, String channelID){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            /*    Intent intent = new Intent(this, AnswerRequestActivity.class);
            intent.putExtra("otherUserID", otherUserID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
*/
            return new Notification.Builder(getApplicationContext(), channelID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.icon_message_notification)
                    .setColor(getColor(R.color.color_blue_light))
                    .setGroup(GROUP_NAME)
                    .setAutoCancel(true);
        }

        return  null;

    }

    private Notification.Builder createNotificationWithOutChannel(String title, String message, PendingIntent pendingIntent){

            return new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.icon_message_notification)
                    .setAutoCancel(true);

    }

    public Notification.Builder createRequestNotification(String title, String message){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        //Lets create low channel
        NotificationChannel requestChannel = new NotificationChannel(REQUEST_CHANNEL_ID ,
                REQUEST_CHANEL_NAME, NotificationManager.IMPORTANCE_LOW);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(requestChannel);


            return  new Notification.Builder(getApplicationContext(), REQUEST_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification_person_add)
                    .setColor(getColor(R.color.color_blue_light))
                    .setAutoCancel(true);
        }
       return null;
    }

    public void showGroupNotification(boolean highImportance){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


            String channelId = (highImportance) ? CHANNEL_HIGH_ID : CHANNEL_LOW_ID;
            Notification groupNotification = new Notification.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.drawable.icon_message_notification )
                    .setGroup(GROUP_NAME)
                    .setContentIntent(pendingIntent)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .build();
            getNotificationManager().notify(GROUP_ID, groupNotification);

        }

    }

}
