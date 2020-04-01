package com.example.alvar.chatapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Activities.ChatActivity;
import com.example.alvar.chatapp.Activities.ContactsActivity;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragmentPage";
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbChatsNodeRef, dbUsersNodeRef;
    //ui elements
    private FloatingActionButton fabContacts;
    private View viewContacts;
    private RecyclerView chatRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    //vars
    private String currentUserID;
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

        currentUserID = auth.getCurrentUser().getUid();

        bind();
        initRecyclerView();
        fabButtonPressed();
        initFirebaseAdapter();


        return viewContacts;
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbChatsNodeRef = database.getReference().child("Chats").child("Messages");
        dbUsersNodeRef = database.getReference().child("Users");
    }

    private void bind() {
        fabContacts = viewContacts.findViewById(R.id.fabContacts);

    }

    private void initRecyclerView() {
        chatRecyclerView = viewContacts.findViewById(R.id.chatRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
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

    private void goToContacts() {
        Intent intentContacts = new Intent(getContext(), ContactsActivity.class);
        startActivity(intentContacts);
    }


    private void initFirebaseAdapter() {


        options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbChatsNodeRef.child(currentUserID), Contacts.class)
                .build();


        adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                //here lies the issue, it does not iterates through the whole list of messages node

                final String otherUserID = getRef(position).getKey();

                Log.i(TAG, "onBindViewHolder: other user id : " + otherUserID);

                //here we fetch information from other user
                dbUsersNodeRef.child(otherUserID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                if (dataSnapshot.exists()) {


                                    //here we fetch info from db
                                    final String name = dataSnapshot.child("name").getValue().toString();
                                    final String image = dataSnapshot.child("imageThumbnail").getValue().toString();

                                    Log.i(TAG, "onDataChange: name " + name);
                                    Log.i(TAG, "onDataChange: image " + image);

                                    //here we set info from db to the UI
                                    holder.username.setText(name);
                                    if (image.equals("imgThumbnail")) {
                                        holder.chatImageContact.setImageResource(R.drawable.profile_image);
                                    } else {

                                        try {
                                            Glide.with(getActivity())
                                                    .load(image).into(holder.chatImageContact);
                                        } catch (NullPointerException e) {
                                            String exception = e.getMessage();
                                            Log.i(TAG, "onDataChange: exception: " + exception);
                                        }

                                    }

                                    //here we show the last Seen of the other user
                                    if (dataSnapshot.child("userState").hasChild("state")){

                                        //here we get the other user's current state and we store it in each var
                                        String saveLastSeenDate = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String saveLastSeenTime = dataSnapshot.child("userState").child("time").getValue().toString();
                                        String saveSate = dataSnapshot.child("userState").child("state").getValue().toString();

                                                //if other user's state is "offline"
                                            if ( saveSate.equals("Offline")){

                                                //we show last message
                                                holder.onlineIcon.setVisibility(View.INVISIBLE);

                                            } else if(saveSate.equals("Online")){

                                                holder.onlineIcon.setVisibility(View.VISIBLE);

                                            }

                                    }

                                    holder.chatLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intentChatRoom = new Intent(getContext(), ChatActivity.class);
                                            intentChatRoom.putExtra("contactID", otherUserID);
                                            intentChatRoom.putExtra("contactName", name);
                                            intentChatRoom.putExtra("contactImage", image);
                                            startActivity(intentChatRoom);
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


    @Override
    public void onStart() {
        super.onStart();

        if (adapter != null) {
            adapter.startListening();
        }

    }


    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        LinearLayout chatLayout;
        CircleImageView chatImageContact, onlineIcon;
        TextView username, lastSeen;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            chatLayout = itemView.findViewById(R.id.chatLayout);
            chatImageContact = itemView.findViewById(R.id.imageChat);
            username = itemView.findViewById(R.id.usernameChat);
            lastSeen = itemView.findViewById(R.id.userLastSeen);
            onlineIcon = itemView.findViewById(R.id.onlineIcon);
        }


    }

}
