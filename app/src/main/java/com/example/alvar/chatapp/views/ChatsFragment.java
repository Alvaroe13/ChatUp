package com.example.alvar.chatapp.views;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.example.alvar.chatapp.Adapter.ChatsAdapter;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.viewModels.ChatListViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithStack;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements ChatsAdapter.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "ChatsFragmentPage";
    //firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference dbChatListRef, dbChatsNodeRef;
    private ValueEventListener removeListener;
    //ui elements
    private RecyclerView chatRecyclerView;
    private View viewLayout;
    //vars
    private String currentUserID, contactID;
    private ChatsAdapter chatsAdapter;
    private ChatListViewModel viewModel;
    private List<User> userList = new ArrayList<>();


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    /**
     * method call right after the view's been created, is better to init ui elements here
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewLayout = view;

        initRecyclerView(view);
        // viewModel stuff
        initViewModel();
        connectionWithViewModel(currentUserID);
        initObserver();

    }


    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserID = auth.getCurrentUser().getUid();
        }
        dbChatListRef = FirebaseDatabase.getInstance().getReference().child("ChatList");
        dbChatsNodeRef = FirebaseDatabase.getInstance().getReference().child("Chats").child("Messages");
    }


    private void initRecyclerView(View view) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setHasFixedSize(true);
        chatsAdapter = new ChatsAdapter(getContext(), new ArrayList<User>(), ChatsFragment.this);
        chatRecyclerView.setAdapter(chatsAdapter);


    }

    //viewModel area
    private void initViewModel() {
        Log.d(TAG, "initViewModel: called");
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
    }

    private void connectionWithViewModel(String userID) {
        Log.d(TAG, "connectionWithViewModel: called");
        viewModel.connectionWithRepo(userID);
    }

    /**
     * show conversations in the fragment
     */
    private void initObserver() {

        Log.d(TAG, "initObserver: called");
        viewModel.getChats().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(final List<User> users) {
                if (users != null) {
                    userList = users;

                    Log.d(TAG, "initObserver onChanged: called");
                    chatRecyclerView.setVisibility(View.VISIBLE);
                    chatsAdapter.updateChats(users);

                    chatItemCLick(users);

                }

            }
        });
    }




    private void showPopUp(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.setOnMenuItemClickListener(ChatsFragment.this);
        popupMenu.inflate(R.menu.menu_pop_up);
        popupMenu.show();
    }


    /**
     * method in charge of taking the user to the chat room sending the info specified
     */
    private void goToChatRoom(String contactID, String image, String name) {
        Log.d(TAG, "goToChatRoom: called!!");
        Bundle bundle = new Bundle();
        bundle.putString(CONTACT_ID, contactID);
        bundle.putString(CONTACT_NAME, name);
        bundle.putString(CONTACT_IMAGE, image);

        navigateWithStack(viewLayout, R.id.chatRoomFragment, bundle);
    }


    /**
     * delete chatroom to make go away the chat from the chatList
     */
    private void deleteChatRoom(final String currentUserID, final String contactID) {

        Log.d(TAG, "deleteChatRoom: called");

        dbChatListRef.child(currentUserID).child(contactID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            dbChatListRef.child(contactID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        deleteChat(currentUserID, contactID);
                                    }
                                }
                            });
                        }
                    }
                });

    }

    /**
     * method in charge of deleting chats in fragment chats (called in remove contact)
     */
    private void deleteChat(final String currentUserID, final String contactID) {

        Log.d(TAG, "deleteChat: called");

        removeListener =
                dbChatsNodeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            Messages message = ds.getValue(Messages.class);
                            try {
                                if (message.getSenderID().equals(currentUserID) && message.getReceiverID().equals(contactID) ||
                                        message.getSenderID().equals(contactID) && message.getReceiverID().equals(currentUserID)) {

                                    ds.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dbChatsNodeRef.removeEventListener(removeListener);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: error = " + e.getMessage());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

    }

        //--------------------------- click event handling ------- //

    private void chatItemCLick(final List<User> users) {
        chatsAdapter.onLongClickHandler(new ChatsAdapter.OnLongClick() {
            @Override
            public void onLongItemClick(int position, View view) {
                Log.d(TAG, "chatItemCLick: onLongItemClick: long click done again");
                contactID = users.get(position).getUserID();
                showPopUp(view);

            }
        });
    }

    @Override
    public void onItemClick(int position) {

        Log.d(TAG, "onItemClick: called button");

        String contactID = userList.get(position).getUserID();
        String image = userList.get(position).getImage();
        String name = userList.get(position).getName();

        goToChatRoom(contactID, image, name);

    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.deleteChat) {
            Log.d(TAG, "onMenuItemClick: delete chat clicked");
            deleteChatRoom(currentUserID, contactID);
            return true;
        }
        return false;
    }
}
