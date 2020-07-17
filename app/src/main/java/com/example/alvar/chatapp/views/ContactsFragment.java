package com.example.alvar.chatapp.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Adapter.ContactsAdapter;
import com.example.alvar.chatapp.Model.Chatroom;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.DrawerLocker;
import com.example.alvar.chatapp.Utils.DrawerStateHelper;
import com.example.alvar.chatapp.Utils.NavHelper;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.alvar.chatapp.Utils.Constant.CHATROOM_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Utils.Constant.DOCUMENT_ID;
import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithStack;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnClickListener {

    private static final String TAG = "ContactsFragment";

    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbContactsNodeRef;
    private RecyclerView recyclerViewContacts;
    //fireStore
    private FirebaseFirestore mDb;
    private DocumentReference chatroomRef;
    //vars
    private String currentUserID;
    private List<Contacts> contactsList = new ArrayList<>();
    private ContactsAdapter adapter;
    private View viewLayout;


    public ContactsFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        initFirebase();
        initFirestore();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called");
        viewLayout = view;
        initRecyclerView(view);
        setToolbar(getString(R.string.contacts),view, false);
        showContacts();
        drawerMode();
    }



    private void initFirebase(){
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
        dbUsersNodeRef.keepSynced(true);
        dbContactsNodeRef = database.getReference().child(getString(R.string.contacts_ref));
        dbContactsNodeRef.keepSynced(true);
    }

    private void initFirestore(){
        mDb = FirebaseFirestore.getInstance();
        chatroomRef = mDb.collection("Chatroom").document();
    }


    private void setToolbar(String title, View layout, Boolean backOption){
         Toolbar toolbarContacts = layout.findViewById(R.id.toolbarContacts);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarContacts);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }


    private void initRecyclerView(View layout){
        recyclerViewContacts = layout.findViewById(R.id.recycleContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
    }



    /**
     * method retrieves all users in user node
     */
    private void showContacts() {

        Log.d(TAG, "showContacts: called!!!"  );

        dbContactsNodeRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //we clear the list first to make sure next time we open this view items are not duplicated
                contactsList.clear();


                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    Contacts contact = ds.getValue(Contacts.class);
                    contactsList.add(contact);

                }

                adapter = new ContactsAdapter(getContext(), contactsList, ContactsFragment.this);
                recyclerViewContacts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * lets take the user to the chat room and create a document in firestore to be used by location
     * fragment
     * @param
     */
    private void goToChatRoom(final String contactID) {

        Chatroom chatroom = new Chatroom();
        chatroom.setMember1ID(currentUserID); //current user
        chatroom.setMember2ID(contactID);   //other user in chatroom


        final String collectionID = chatroomRef.getId();  // random ID provided by Firestore db.
        final String documentID = currentUserID + "_" + contactID;

        //we create chatroom  document when a chatroom is created by the user in the UI;
        chatroomRef = mDb.collection("Chatroom")
                .document(documentID);


        chatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    //here we fetch contact's info from db, we make this query to the users node
                    //since the info we get from the adapter is aiming to the contacts node.
                    dbUsersNodeRef.child(contactID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){

                                try {

                                    String image = dataSnapshot.child("imageThumbnail").getValue().toString();
                                    String name = dataSnapshot.child("name").getValue().toString();

                                    Bundle bundle = new Bundle();
                                    bundle.putString(CONTACT_ID, contactID);
                                    bundle.putString(CONTACT_NAME, name);
                                    bundle.putString(CONTACT_IMAGE, image);
                                    bundle.putString(CHATROOM_ID, collectionID);
                                    bundle.putString(DOCUMENT_ID, documentID);

                                    Log.d(TAG, "onDataChange: called");
                                    navigateWithStack(viewLayout, R.id.chatRoomFragment, bundle);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }



                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
        });

    }

    //here we handle click event
    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: cardview pressed in contacts");
        String contactID = contactsList.get(position).getContactID();
        goToChatRoom(contactID);
    }

    private void drawerMode() {
        DrawerStateHelper.drawerEnabled(getActivity(), false);
    }
}
