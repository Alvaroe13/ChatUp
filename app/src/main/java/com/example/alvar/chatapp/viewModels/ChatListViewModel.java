package com.example.alvar.chatapp.viewModels;

import android.util.Log;

import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.repositories.RepositoryHub;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ChatListViewModel extends ViewModel {

    private RepositoryHub repositoryHub;
    private LiveData<List<User>> chats;

    public ChatListViewModel( ) {
        repositoryHub = RepositoryHub.getRepository();
        chats = repositoryHub.getChats();
    }


    public void connectionWithRepo(String userID){
         repositoryHub.connectionWithFirebase(userID);
    }

    public LiveData<List<User>> getChats(){
        return  chats;
    }

}
