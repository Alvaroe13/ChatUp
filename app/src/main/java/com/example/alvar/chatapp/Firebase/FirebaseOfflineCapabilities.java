package com.example.alvar.chatapp.Firebase;


import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * this class is in charge of showing info in the app when offline
 */
public class FirebaseOfflineCapabilities extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //enable offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
