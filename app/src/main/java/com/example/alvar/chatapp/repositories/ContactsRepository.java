package com.example.alvar.chatapp.repositories;

import android.util.Log;

import com.example.alvar.chatapp.Model.Contacts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class ContactsRepository {

    private static final String TAG = "ContactsRepository";


    public static ContactsRepository instance;
    private MutableLiveData<List<Contacts>> contacts = new MutableLiveData<>();
    private List<Contacts> contactList = new ArrayList<>();


    public static ContactsRepository getInstance(){
        if (instance == null){
            instance = new ContactsRepository();
        }
        return instance;
    }

    public MutableLiveData<List<Contacts>> getContacts(){
        return contacts;
    }

    public void fetchContacts(String userID) {

        if (userID != null){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference.child("Contacts"). child(userID);
            query.keepSynced(true);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    contactList.clear();

                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: called!!!");
                        Contacts contact = ds.getValue(Contacts.class);
                        contactList.add(contact);
                    }
                    contacts.postValue(contactList);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Log.d(TAG, "fetchContacts: null");
        }


    }


}
