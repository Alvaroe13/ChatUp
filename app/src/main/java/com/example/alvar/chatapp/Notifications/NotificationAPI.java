package com.example.alvar.chatapp.Notifications;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationAPI {


    @Headers({ "Content-Type:application/json" ,
            "Authorization:key=AAAAhNQdtcA:APA91bHRs49seNR290a9-G5v98yAcXk96uLTb_096KKP4i-Av_YaMTsANcnJ2cP5XMZN4UCDUqq7dF6JxQdXhh_9o6QXQ7_vgHOPBYv-eowNdKwa8SLVlWdAqgOQLxzPA_WY9m42E9ME"
            })
    @POST("fcm/send")
   // Response<ResponseFCM> sendNotification (@Body PushNotification notificationBody);
    Call<ResponseFCM> sendNotification (@Body PushNotification notificationBody);
}
