package com.example.alvar.chatapp.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.alvar.chatapp.R;

import androidx.annotation.RequiresApi;

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
                                                        CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_HIGH);

            channelHigh.enableLights(true);
            channelHigh.enableVibration(true);
            channelHigh.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getNotificationManager().createNotificationChannel(channelHigh);

            //Lets create low channel
            /*NotificationChannel lowChannel = new NotificationChannel(CHANNEL_LOW_ID ,
                                                        CHANNEL_LOW_NAME, NotificationManager.IMPORTANCE_LOW);

            lowChannel.enableLights(true);
            lowChannel.enableVibration(true);
            lowChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            Uri sound2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            lowChannel.setSound(sound2, null);


            getNotificationManager().createNotificationChannel(lowChannel);*/
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
                    .setShowWhen(true)
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
}
