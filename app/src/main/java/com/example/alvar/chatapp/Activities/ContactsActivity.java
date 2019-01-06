package com.example.alvar.chatapp.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Fragments.RequestsFragment;
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

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsPage";
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbContactsNodeRef;
    private RecyclerView recyclerViewContacts;
    //vars
    private String currentUserID;

    //UI elements
    private Toolbar toolbarContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Log.i(TAG, "onCreate: init correctly the activity");

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
                                final String list_user_contacts_id = getRef(position).getKey();

                                DatabaseReference contactStatus = getRef(position).child("contact_status").getRef();

                                 contactStatus.addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                         if (dataSnapshot.exists()){

                                             String contactStatus = dataSnapshot.getValue().toString();

                                             if (contactStatus.equals("saved")){

                                                 dbUsersNodeRef.child(list_user_contacts_id)
                                                                            .addValueEventListener(new ValueEventListener() {
                                                     @Override
                                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                         if (dataSnapshot.exists()) {

                                                             //here we fetch info from db
                                                             String name = dataSnapshot.child("name").getValue().toString();
                                                             String status = dataSnapshot.child("status").getValue().toString();
                                                             String image = dataSnapshot.child("imageThumbnail").getValue().toString();

                                                             //here we set info from db to the UI
                                                             holder.contactName.setText(name);
                                                             holder.contactStatus.setText(status);
                                                             if ( image.equals("imgThumbnail")){
                                                                 holder.contactImage.setImageResource(R.drawable.profile_image);
                                                             } else{
                                                                 Glide.with(ContactsActivity.this)
                                                                                                .load(image).into(holder.contactImage);
                                                             }
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
     * this class contains the UI elements from the individual contact list
     */
    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView contactImage;
        TextView contactName, contactStatus;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            contactImage = itemView.findViewById(R.id.imageContactUsers);
            contactName = itemView.findViewById(R.id.usernameContactUsers);
            contactStatus = itemView.findViewById(R.id.statusContactUsers);


        }




    }
}
