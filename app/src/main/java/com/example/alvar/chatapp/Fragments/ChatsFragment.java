package com.example.alvar.chatapp.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Adapter.ChatsAdapter;
import com.example.alvar.chatapp.Model.ChatList;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragmentPage";
    //ui elements
    private View viewContacts;
    private RecyclerView chatRecyclerView;
    //firebase
    private DatabaseReference dbUsersNodeRef, dbChatListRef ;
    //vars
    private String currentUserID;
    private List<ChatList> chatListList;
    private List<User> userList;
    private ChatsAdapter chatsAdapter;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //bind view with controller
        viewContacts = inflater.inflate(R.layout.fragment_chats, container, false);

        initFirebase();
        initRecyclerView();
        fetchChatLists();

        return viewContacts;
    }



    /**
     * we init firebase services.
     */
    private void initFirebase() {
        //firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbChatListRef = database.getReference().child("ChatList");
    }

    private void initRecyclerView() {
        chatRecyclerView = viewContacts.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager( getContext() ));
        chatRecyclerView.setHasFixedSize(true);

    }

    /**
     * fetch list of all conversation in the "ChatList" node from db
     */
    private void fetchChatLists(){

        chatListList = new ArrayList<>();

        dbChatListRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                   final ChatList chatList = snapshot.getValue(ChatList.class);
                    chatListList.add(chatList);
                }



                showChatList();

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

        userList = new ArrayList<>();

        dbUsersNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    for (ChatList chatList : chatListList){
                        if (user.getUserID() != null && user.getUserID().equals(chatList.getId())){
                            userList.add(user);
                            break;
                        }
                    }

                    chatsAdapter = new ChatsAdapter(getContext(), userList);
                    chatRecyclerView.setAdapter(chatsAdapter);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }






}
