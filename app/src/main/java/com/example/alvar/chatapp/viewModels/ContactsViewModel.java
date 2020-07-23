package com.example.alvar.chatapp.viewModels;

import android.util.Log;

import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.repositories.ContactsRepo;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ContactsViewModel extends ViewModel {

    private static final String TAG = "ContactsViewModel";
    private ContactsRepo repo;
 

    public void init(){
        repo = ContactsRepo.getInstance();
        Log.d(TAG, "init: called");
    }


    public LiveData<List<Contacts>> getContacts(){
        return  repo.getContacts();
    }


    public void sendUserID(String userID) {
        repo.fetchContacts(userID);
    }
}
