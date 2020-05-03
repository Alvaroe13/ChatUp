package com.example.alvar.chatapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Activities.ChatActivity;
import com.example.alvar.chatapp.Activities.ContactsActivity;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragmentPage";

    //ui elements
    private FloatingActionButton fabContacts;
    private View viewContacts;
    private RecyclerView chatRecyclerView;
    //firebase
    private DatabaseReference dbChatsNodeRef, dbUsersNodeRef;
    //vars
    private String currentUserID, lastMessage, lastMessageDate;
    private FirebaseRecyclerOptions<Contacts> options;
    private FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //bind view with controller
        viewContacts = inflater.inflate(R.layout.fragment_chats, container, false);

        initFirebase();
        bind();
        initRecyclerView();
        fabButtonPressed();
        initFirebaseAdapter();

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
        dbChatsNodeRef = database.getReference().child("Chats").child("Messages");
        dbUsersNodeRef = database.getReference().child("Users");
    }

    private void bind() {
        fabContacts = viewContacts.findViewById(R.id.fabContacts);

    }

    private void initRecyclerView() {
        chatRecyclerView = viewContacts.findViewById(R.id.chatRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setHasFixedSize(true);

    }

    /**
     * this method handles event when fab button is pressed
     */
    private void fabButtonPressed() {
        fabContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToContacts();
            }
        });
    }

    /**
     * we take the user to the contacts activity.
     */
    private void goToContacts() {
        Intent intentContacts = new Intent(getContext(), ContactsActivity.class);
        startActivity(intentContacts);
    }

    /**
     * Contains the logic behind the firebase Adapter library.
     */
    private void initFirebaseAdapter() {


        options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbChatsNodeRef.child(currentUserID), Contacts.class)
                .build();


        adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                final String otherUserID = getRef(position).getKey();
                Log.i(TAG, "onBindViewHolder: other user id : " + otherUserID);

                //here we fetch information from other user
                dbUsersNodeRef.child(otherUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()) {

                            //here we fetch info from db
                            final String name = dataSnapshot.child("name").getValue().toString();
                            final String image = dataSnapshot.child("imageThumbnail").getValue().toString();
                            final String typingState = dataSnapshot.child("userState").child("typing").getValue().toString();

                            Log.i(TAG, "onDataChange: name: " + name);
                            Log.i(TAG, "onDataChange: image: " + image);
                            Log.i(TAG, "onDataChange: typingState: " + typingState);

                            //fetch info from db and set it into the UI
                            setInfoIntoLayout(name, image, holder);
                            // update user's typing state in real time
                            typingStatus(typingState, otherUserID, holder );
                            // shows if other user is online/offline
                            otherUserState(dataSnapshot, holder);
                            //if user clicks on the CardView
                            holder.chatLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToChatRoom(otherUserID, name, image);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View chatView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.chats_individual_layout, viewGroup, false);

                return new ChatsViewHolder(chatView);
            }
        };
        adapter.startListening();
        chatRecyclerView.setAdapter(adapter);
    }

    /**
     * updates in real time if user is typing
     * @param typingState
     * @param otherUserID
     * @param holder
     */
    private void typingStatus(String typingState, String otherUserID, ChatsViewHolder holder) {

        if (typingState.equals("yes")) {
            try {
                holder.smallIcon.setVisibility(View.GONE);
                holder.lastMessage.setText(R.string.typing);
                holder.lastMessage.setTextColor(getActivity().getResources().getColor(R.color.color_green));
            }
            catch (Exception e){
                Log.i(TAG, "onDataChange: error: " + e.getMessage() );
            }
        }
        else {
            //this method show last message in the fragment list with conversations started
            try {
                showLastMessage(currentUserID, otherUserID, holder.lastMessage,
                        holder.lastMessageDateField, holder.smallIcon);
                holder.lastMessage.setTextColor(getActivity().getResources().getColor(R.color.color_grey));
            }
            catch (Exception ex){
                Log.i(TAG, "onDataChange: error: " + ex.getMessage());
            }
        }
    }

    /**
     * fetch info from db and display it into the fragment chat
     *
     * @param name
     * @param image
     * @param holder
     */
    private void setInfoIntoLayout(String name, String image, ChatsViewHolder holder) {

        //here we set info from db to the UI
        holder.username.setText(name);
        if (image.equals("imgThumbnail")) {
            holder.chatImageContact.setImageResource(R.drawable.profile_image);
        }
        else {
            try {
                Glide.with(getActivity()).load(image).into(holder.chatImageContact);
            }
            catch (NullPointerException e) {
                Log.i(TAG, "onDataChange: exception: " + e.getMessage());
            }
        }
    }

    /**
     * this method is the one in charge of making visible the green dot when other user in online or not
     *
     * @param dataSnapshot
     * @param holder
     */
    private void otherUserState(DataSnapshot dataSnapshot, ChatsViewHolder holder) {

        //here we show the current state of the other user
        if (dataSnapshot.child("userState").hasChild("state")) {

            //here we get the other user's current state and we store it in each var
            String saveSate = dataSnapshot.child("userState").child("state").getValue().toString();

            //if other user's state is "offline"
            if (saveSate.equals("Offline")) {
                //we show last message
                holder.onlineIcon.setVisibility(View.GONE);
            } else if (saveSate.equals("Online")) {
                holder.onlineIcon.setVisibility(View.VISIBLE);

            }

        }

    }

    /**
     * method in charge of taking the user to the chat room sending the info specified
     *
     * @param otherUserID
     * @param name
     * @param image
     */
    private void goToChatRoom(String otherUserID, String name, String image) {
        Intent intentChatRoom = new Intent(getContext(), ChatActivity.class);
        intentChatRoom.putExtra("contactID", otherUserID);
        intentChatRoom.putExtra("contactName", name);
        intentChatRoom.putExtra("contactImage", image);
        startActivity(intentChatRoom);
    }

    /**
     * This method is the one in charge of showing the last message in a conversation
     *
     * @param currentUserID
     * @param otherUserID
     * @param lastMessageField
     */
    private void showLastMessage(final String currentUserID, final String otherUserID,
                                 final TextView lastMessageField, final TextView lastMessageDateField, final ImageButton smallIcon) {

        //retrieve info from the db node "Chats" / "Messages"
        dbChatsNodeRef.child(currentUserID).child(otherUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (dataSnapshot.exists()) {

                        //we bind the Message node in firebase database with the JAVA model "Messages"
                        Messages message = snapshot.getValue(Messages.class);

                        Log.i(TAG, "onDataChange: sender: " + message.getSenderID());
                        Log.i(TAG, "onDataChange: receiver: " + message.getReceiverID());

                        if (message.getSenderID().equals(currentUserID) && message.getReceiverID().equals(otherUserID) ||
                                message.getSenderID().equals(otherUserID) && message.getReceiverID().equals(currentUserID)) {

                            smallIcon.setVisibility(View.GONE);

                            switch (message.getType()) {
                                case "image":
                                    smallIcon.setVisibility(View.VISIBLE);
                                    lastMessageField.setText(R.string.photo);
                                    break;
                                case "pdf":
                                    smallIcon.setVisibility(View.VISIBLE);
                                    smallIcon.setBackgroundResource(R.drawable.file_icon);
                                    lastMessageField.setText(R.string.PDF);
                                    break;
                                case "docx":
                                    smallIcon.setVisibility(View.VISIBLE);
                                    smallIcon.setBackgroundResource(R.drawable.file_icon);
                                    lastMessageField.setText(R.string.Word_Document);
                                    break;
                                default: //"text" is the one by default
                                    lastMessage = message.getMessage();
                                    lastMessageField.setText(lastMessage);

                                    lastMessageDate = message.getMessageDate();
                                    lastMessageDateField.setText(lastMessageDate);
                            }


                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        if (adapter != null) {
            adapter.startListening();
        }

    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout chatLayout;
        CircleImageView chatImageContact, onlineIcon;
        TextView username, lastMessage, lastMessageDateField;
        ImageButton smallIcon;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            chatLayout = itemView.findViewById(R.id.chatLayout);
            chatImageContact = itemView.findViewById(R.id.imageChat);
            username = itemView.findViewById(R.id.usernameChat);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageDateField = itemView.findViewById(R.id.lastMessageDate);
            onlineIcon = itemView.findViewById(R.id.onlineIcon);
            smallIcon = itemView.findViewById(R.id.smallIcon);
        }


    }

}
