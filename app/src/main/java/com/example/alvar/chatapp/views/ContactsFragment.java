package com.example.alvar.chatapp.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Adapter.ContactsAdapter;
import com.example.alvar.chatapp.Model.Contacts;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";

    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbContactsNodeRef;
    private RecyclerView recyclerViewContacts;
    //vars
    private String currentUserID;
    private List<Contacts> contactsList = new ArrayList<>();
    private ContactsAdapter adapter;


    public ContactsFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFirebase();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_contacts, container, false);

        initRecyclerView(layout);
        setToolbar(getString(R.string.contacts),layout, false);
        showContacts();

        return layout;
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

                adapter = new ContactsAdapter(getContext(), contactsList, currentUserID);
                recyclerViewContacts.setAdapter(adapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
