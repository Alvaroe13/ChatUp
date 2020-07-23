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
import com.example.alvar.chatapp.viewModels.ContactsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //fireStore
    private FirebaseFirestore mDb;
    private DocumentReference chatroomRef;
    //vars
    private String currentUserID;
    private List<Contacts> contactsList = new ArrayList<>();
    private ContactsAdapter adapter;
    private View viewLayout;
    private ContactsViewModel viewModel;


    public ContactsFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser= auth.getCurrentUser();
        initFirebase();
        if (currentUser != null){
            currentUserID = auth.getCurrentUser().getUid();
            Log.d(TAG, "onCreate: userID: " + currentUserID);
        }
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
        initViewModel();
        sendUserID(currentUserID);
        initRecyclerView(view);
        setToolbar(getString(R.string.contacts),view, false);

        fetchContactList();

    }

    private void initViewModel() {
        Log.d(TAG, "initViewModel: called");
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.init();
    }

    private void initFirebase(){
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
        dbUsersNodeRef.keepSynced(true);
    }


    private void initFirestore(){
        mDb = FirebaseFirestore.getInstance();
        chatroomRef = mDb.collection("Chatroom").document();
    }


    private void setToolbar(String title, View layout, Boolean backOption){
        try {
            Toolbar toolbarContacts = layout.findViewById(R.id.toolbarContacts);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarContacts);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void initRecyclerView(View layout){
        RecyclerView recyclerViewContacts  = layout.findViewById(R.id.recycleContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(getContext(), new ArrayList<Contacts>(), ContactsFragment.this);
        recyclerViewContacts.setAdapter(adapter);
    }


    /**
     * method retrieves all users in user node
     */
    
    private void fetchContactList(){
        viewModel.getContacts().observe(getViewLifecycleOwner(), new Observer<List<Contacts>>() {
            @Override
            public void onChanged(List<Contacts> contacts) {
                if (contacts != null){
                    Log.d(TAG, "fetchContactList: called");
                    adapter.setContacts(contacts);
                }else {
                    Log.d(TAG, "fetchContactList: came null");
                }
            }
        });
    }

    private void sendUserID(String userID){
        viewModel.sendUserID(userID);
    }


    /**
     * lets take the user to the chat room and create a document in firestore to be used by location
     * fragment
     * @param
     */
    private void goToChatRoom(final String currentUserID, final String contactID) {

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
        goToChatRoom(currentUserID, contactID);
    }


}
