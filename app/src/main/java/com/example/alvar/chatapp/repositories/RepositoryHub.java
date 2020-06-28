package com.example.alvar.chatapp.repositories;

import android.util.Log;

import com.example.alvar.chatapp.FirebaseConnection;
import com.example.alvar.chatapp.Model.User;

import java.util.List;

import androidx.lifecycle.LiveData;

public class RepositoryHub {



    public static RepositoryHub instance;
    private FirebaseConnection firebaseConnection;
    private LiveData<List<User>> chats;

    public static RepositoryHub getRepository(){
        if (instance == null){
            instance = new RepositoryHub();
        }
        return instance;
    }

    public RepositoryHub() {
        firebaseConnection = FirebaseConnection.getFirebaseConnection();
        chats = firebaseConnection.getChatList();
    }

    public void connectionWithFirebase(String userID){
        firebaseConnection.setConnectionToUsersNode(userID);
    }

    public LiveData<List<User>> getChats(){
        return chats;
    }


}
