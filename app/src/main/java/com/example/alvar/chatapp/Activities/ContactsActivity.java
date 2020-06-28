package com.example.alvar.chatapp.activities;

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
import com.example.alvar.chatapp.Model.Chatroom;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.alvar.chatapp.Utils.Constant.CHATROOM_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Utils.Constant.DOCUMENT_ID;

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

        initFirebase();
        initFirestore();
        setToolbar(getString(R.string.contacts), true);
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
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
        dbUsersNodeRef.keepSynced(true);
        //init db "Contacts" node
        dbContactsNodeRef = database.getReference().child(getString(R.string.contacts_ref));
        dbContactsNodeRef.keepSynced(true);
    }

    private void initFirestore(){
        mDb = FirebaseFirestore.getInstance();
        chatroomRef = mDb.collection(getString(R.string.chatroom_ref)).document();
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

                                DatabaseReference contactStatus = getRef(position).child(getString(R.string.contact_status_db)).getRef();

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
                                                             final String name = dataSnapshot.child(getString(R.string.name_db)).getValue().toString();
                                                             final String status = dataSnapshot.child(getString(R.string.status_db)).getValue().toString();
                                                             final String image = dataSnapshot.child(getString(R.string.imageThumbnail_db)).getValue().toString();
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
    private void goToChatRoom(final String listContactsID, final String name, final String image) {

        Chatroom chatroom = new Chatroom();
        chatroom.setMember1ID(currentUserID); //current user
        chatroom.setMember2ID(listContactsID);   //other user in chatroom

        final String collectionID = chatroomRef.getId();  // random ID provided by Firestore db.
        final String documentID = currentUserID + "_" + listContactsID;

        //we create chatroom  document when a chatroom is created by the user in the UI;
        chatroomRef = mDb.collection(getString(R.string.chatroom_ref))
                         .document(documentID);

        chatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intentChatRoom = new Intent(ContactsActivity.this, ChatActivity.class);
                    intentChatRoom.putExtra(CONTACT_ID, listContactsID);
                    intentChatRoom.putExtra(CONTACT_NAME, name);
                    intentChatRoom.putExtra(CONTACT_IMAGE, image);
                    intentChatRoom.putExtra(CHATROOM_ID, collectionID);
                    intentChatRoom.putExtra(DOCUMENT_ID, documentID);
                    startActivity(intentChatRoom);
                }

            }
        });


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
