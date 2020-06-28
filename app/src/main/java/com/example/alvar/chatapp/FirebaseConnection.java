package com.example.alvar.chatapp;

import android.util.Log;

import com.example.alvar.chatapp.Model.ChatList;
import com.example.alvar.chatapp.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FirebaseConnection {

    private static final String TAG = "FirebaseDatabaseNodes";

    private List<ChatList> chatListList;
    private List<User> userList;
    private MutableLiveData<List<User>> chats;


    private static FirebaseConnection instance;
    //firebase
    private DatabaseReference dbUsersNodeRef, dbChatListRef ;


    public static FirebaseConnection getFirebaseConnection(){
        if (instance == null){
            instance = new FirebaseConnection();
        }
        return instance;
    }

    public FirebaseConnection() {
        initFirebase();
        chats = new MutableLiveData<>();
    }

    /**
     * this pass the chatList
     * @return
     */
    public LiveData<List<User>> getChatList(){
        return chats;
    }


    /**
     * we init firebase services.
     */
    private void initFirebase() {
        //firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbUsersNodeRef.keepSynced(true);
        dbChatListRef = database.getReference().child("ChatList");
        dbChatListRef.keepSynced(true);
    }

    public void setConnectionToUsersNode(String userID){

        Log.d(TAG, "setConnectionToUsersNode:  called");

        chatListList = new ArrayList<>();

        dbChatListRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();
                if (dataSnapshot.exists()){

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        final ChatList chatList = snapshot.getValue(ChatList.class);
                        chatListList.add(chatList);
                    }

                    showChatList();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * show conversations in the fragment
     */
    private void showChatList() {

        Log.d(TAG, "showChatList: called");

        userList = new ArrayList<>();

        dbUsersNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    for (ChatList chatList : chatListList){
                        try{
                            if (user.getUserID() != null && user.getUserID().equals(chatList.getId())){
                                userList.add(user);
                                chats.setValue(userList);
                                break;
                            }
                        }catch (NullPointerException e){
                            Log.e(TAG, "showChatList: onDataChange error: " + e.getMessage() );
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }






}
