package com.example.alvar.chatapp.repositories;

import android.util.Log;

import com.example.alvar.chatapp.Model.ChatList;
import com.example.alvar.chatapp.Model.User;
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

public class ChatsRepository {

    private static final String TAG = "FirebaseDatabaseNodes";

    private List<ChatList> chatListList;
    private List<User> userList;
    private MutableLiveData<List<User>> chats;

    public static ChatsRepository instance;

    //firebase
    private DatabaseReference dbUsersNodeRef, dbChatListRef ;

    public static ChatsRepository getRepository(){
        if (instance == null){
            instance = new ChatsRepository();
        }
        return instance;
    }

    /**
     * this pass the chatList
     * @return
     */
    public MutableLiveData<List<User>> getChatList(){
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

        initFirebase();
        chats = new MutableLiveData<>();

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

        Query query = dbUsersNodeRef;
        query.keepSynced(true);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (ChatList chatList : chatListList){
                        try{
                            if (user.getUserID() != null && user.getUserID().equals(chatList.getId())){
                                userList.add(user);
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }

                    chats.postValue(userList);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
