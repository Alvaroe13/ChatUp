package com.example.alvar.chatapp.viewModels;

import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.repositories.ChatsRepository;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ChatListViewModel extends ViewModel {

    private ChatsRepository chatsRepository;

    public void init(){
        chatsRepository = ChatsRepository.getRepository();
    }


    public void connectionWithRepo(String userID){
         chatsRepository.setConnectionToUsersNode(userID);
    }

    public LiveData<List<User>> getChats(){
        return  chatsRepository.getChatList();
    }

}
