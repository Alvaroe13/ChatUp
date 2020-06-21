package com.example.alvar.chatapp.Notifications;

import com.example.alvar.chatapp.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.alvar.chatapp.Constant.BASE_URL;

public class RetrofitClient {

    private static Retrofit instance;

    public static Retrofit getRetrofit(){
            if (instance == null){
                instance =    new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
        return instance;
    }

  //  private static Retrofit retrofit = builder.build();

 /*   private static NotificationAPI api  = retrofit.create(NotificationAPI.class);

    //better to pass api complete when is needed to avoid extra code in activity.
    public static NotificationAPI getAPI(){
        return api;
    }*/


}
