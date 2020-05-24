package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsPage";
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbContactsNodeRef;
    private RecyclerView recyclerViewContacts;
    //firestore
    private FirebaseFirestore mDb;
    private DocumentReference chatroomRef;
    //vars
    private String currentUserID;

    //UI elements
    private Toolbar toolbarContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Log.i(TAG, "onCreate: init correctly the activity");

        mDb = FirebaseFirestore.getInstance();
        chatroomRef = mDb.collection("Chatroom").document();

        initFirebase();
        setToolbar("Contacts", true);
        initRecyclerView();

    }

    private void initFirebase(){
        //init firebase auth
        auth = FirebaseAuth.getInstance();
        //get current user ID
        currentUserID = auth.getCurrentUser().getUid();
        //init database
        database = FirebaseDatabase.getInstance();
        //init db "Users" node
        dbUsersNodeRef = database.getReference().child("Users");
        dbUsersNodeRef.keepSynced(true);
        //init db "Contacts" node
        dbContactsNodeRef = database.getReference().child("Contacts");
        dbContactsNodeRef.keepSynced(true);
    }

    private void setToolbar(String title, Boolean backOption){
        toolbarContacts = findViewById(R.id.toolbarContacts);
        setSupportActionBar(toolbarContacts);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    private void initRecyclerView(){
        recyclerViewContacts = findViewById(R.id.recycleContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        initFirebaseUI();
    }

    /**
     * method in charge of populating the recyclerView with the info saved in the db
     */
    private void initFirebaseUI() {

        //this code is the one i charge of the query to the firebase db to "Contacts" node
        FirebaseRecyclerOptions<Contacts> requests =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(dbContactsNodeRef.child(currentUserID), Contacts.class)
                        .build();


                FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapterFirebase =
                        new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(requests) {
                            @Override
                            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull final Contacts model) {

                                //here we get the user id of every contact saved in "Contacts" node
                                final String listContactsID = getRef(position).getKey();

                                Log.i(TAG, "onBindViewHolder: contacts : " + listContactsID);

                                DatabaseReference contactStatus = getRef(position).child("contact_status").getRef();

                                 contactStatus.addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                         if (dataSnapshot.exists()){

                                             String contactStatus = dataSnapshot.getValue().toString();

                                             if (contactStatus.equals("saved")){

                                                 dbUsersNodeRef.child(listContactsID)
                                                                            .addValueEventListener(new ValueEventListener() {
                                                     @Override
                                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                         if (dataSnapshot.exists()) {

                                                             //here we fetch info from db
                                                             final String name = dataSnapshot.child("name").getValue().toString();
                                                             final String status = dataSnapshot.child("status").getValue().toString();
                                                             final String image = dataSnapshot.child("imageThumbnail").getValue().toString();
                                                             //set info into the cardViews
                                                             setInfoIntoUI(name, status, image, holder);
                                                             //if cardView is pressed
                                                             holder.cardViewContact.setOnClickListener(new View.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(View v) {
                                                                     goToChatRoom(listContactsID, name, image);
                                                                 }
                                                             });
                                                         }

                                                     }

                                                     @Override
                                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                                     }
                                                 });

                                             }

                                         }

                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                     }
                                 });

                            }

                            @NonNull
                            @Override
                            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                                View contactsView = LayoutInflater.from(viewGroup.getContext())
                                                        .inflate(R.layout.contacts_individual_layout, viewGroup, false);

                                ContactsViewHolder viewHolder = new ContactsViewHolder(contactsView);
                                return viewHolder;
                            }
                        };

        recyclerViewContacts.setAdapter(adapterFirebase);
        adapterFirebase.startListening();

    }

    /**
     * fetch info from the db contacts node and set it into the card views
     * @param name
     * @param status
     * @param image
     * @param holder
     */
    private void setInfoIntoUI(String name, String status, String image, ContactsViewHolder holder) {
            //here we set info from db to the UI
            holder.contactName.setText(name);
            holder.contactStatus.setText(status);
            if ( image.equals("imgThumbnail")){
                holder.contactImage.setImageResource(R.drawable.profile_image);
            } else{
                Glide.with(getApplicationContext())
                        .load(image).into(holder.contactImage);
            }
    }

    /**
     * lets take the user to the chat room
     * @param listContactsID
     * @param name
     * @param image
     */
    private void goToChatRoom(String listContactsID, String name, String image) {

        String collectionID = chatroomRef.getId();  // random ID provided by Firestore db.

        //we create chatroom  document when a chatroom is created by the user in the UI;
        chatroomRef = mDb.collection("Chatroom")
                          .document();

        Intent intentChatRoom = new Intent(ContactsActivity.this, ChatActivity.class);
        intentChatRoom.putExtra("contactID", listContactsID);
        intentChatRoom.putExtra("contactName", name);
        intentChatRoom.putExtra("contactImage", image);
        intentChatRoom.putExtra("chatroomID", collectionID);
        startActivity(intentChatRoom);
    }

    /**
     * this class contains the UI elements from the individual contact list
     */
    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView contactImage;
        TextView contactName, contactStatus;
        CardView cardViewContact;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            contactImage = itemView.findViewById(R.id.imageContactUsers);
            contactName = itemView.findViewById(R.id.usernameContactUsers);
            contactStatus = itemView.findViewById(R.id.statusContactUsers);
            cardViewContact = itemView.findViewById(R.id.cardViewContact);
        }

    }
}
