package com.example.alvar.chatapp.Notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

/**
 * This class is meant to run in a background thread in order to process image with Glide
 * and push notification afterwards
 */
public class NotificationThread  implements Runnable {

    private static final String TAG = "NotificationThread";

    private String photo;
    private String username;
    private String message;
    private PendingIntent pendingIntent;
    private Context context;
    private Bitmap imagePic = null;

    public NotificationThread(String username, String message, PendingIntent pendingIntent, String photo, Context context ){
        this.photo = photo;
        this.username = username;
        this.message = message;
        this.pendingIntent = pendingIntent;
        this.context = context;
    }

    @Override
    public void run() {

        //retrieve photo info
        FutureTarget<Bitmap> foto = Glide.with(context)
                .asBitmap()
                .load(photo)
                .submit();

        try{
            //save the info into a bitmap var type
            imagePic = foto.get();
            Log.d(TAG, "run: imagePic info: " + imagePic);
        }catch (Exception e){
            e.printStackTrace();
        }

        //by passing Looper.getMailLooper() as param in constructor we can create an instance
        // of the MainThread directly
        Handler uiThread = new Handler(Looper.getMainLooper());
        uiThread.post(new Runnable() {
            @Override
            public void run() {

                //create notification and pass the bitmap processed by Glide
                buildNotification(username, message, pendingIntent, imagePic, true);
            }
        });

    }

    /**
     * here we launch push notification since we have all the info collected and processed
     * @param senderUsername
     * @param message
     * @param pendingIntent
     * @param profilePic
     * @param highImportance
     */
    private void buildNotification(String senderUsername, String message, PendingIntent pendingIntent, Bitmap profilePic, boolean highImportance) {

        Log.d(TAG, "BuildNotification: entered here");

        NotificationHandler notificationHandler = new NotificationHandler(context);
        Notification.Builder builder = notificationHandler.createNotification(senderUsername, message, pendingIntent, profilePic, highImportance );

        notificationHandler.getNotificationManager().notify(1, builder.build());
        //notificationHandler.showGroupNotification(true);
    }
}
