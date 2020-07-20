package com.example.alvar.chatapp.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatRequestFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "ChatRequestFragment";

    //firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef , dbContactsNodeRef, dbRequestsNodeRef;
    //ui elements
    private ImageView imageView;
    private TextView otherUserName;
    private Button acceptButton, declineButton;
    //vars
    private String currentUserID, otherUserID;

    public ChatRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");

        initFirebase();
        //we get user id from "Request Fragment"
        if (getArguments()!= null){
            otherUserID = getArguments().getString("otherUserID") ;
        }

        if (currentUser != null){
            currentUserID = auth.getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called as well");
        bindUI(view);
        fetchInfo();
        acceptButton.setOnClickListener(this);
        declineButton.setOnClickListener(this);

    }

    private void bindUI(View view){

        imageView = view.findViewById(R.id.requestImage);
        otherUserName = view.findViewById(R.id.requestName);
        acceptButton = view.findViewById(R.id.requestButtonAccept);
        declineButton = view.findViewById(R.id.requestButtonDecline);

    }

    private void initFirebase(){

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbContactsNodeRef = database.getReference().child("Contacts");
        dbRequestsNodeRef = database.getReference().child("Chat_Requests");

    }

    /**
     * this method is in charge of fetching info from "Users" node and set up the
     * name and image
     */
    private void fetchInfo(){

        dbUsersNodeRef.child(otherUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    otherUserName.setText(name);

                    try {
                        //GLIDE
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .error(R.drawable.profile_image);

                        Glide.with(getContext())
                                .setDefaultRequestOptions(options)
                                .load(image)
                                .into(imageView);

                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    /**
     * method in charge of accepting chat request
     */
    private void acceptChatRequest() {

            /*at this point since we have accepted the chat request
              we add the new contact in the "Contacts" node
             */

        final Map<String, Object> hash1 = new HashMap<>();
        hash1.put("contactID" , otherUserID);
        hash1.put("contact_status", "saved");

        dbContactsNodeRef.child(currentUserID).child(otherUserID)
                .setValue(hash1)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            final Map<String, Object> hash2 = new HashMap<>();
                            hash2.put("contactID" , currentUserID);
                            hash2.put("contact_status", "saved");

                            dbContactsNodeRef.child(otherUserID).child(currentUserID)
                                    .setValue(hash2)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                    /*now from this point onward we remove request from request tab
                                                      by deleting such request from the "Chat_Requests" node
                                                     */

                                                dbRequestsNodeRef.child(currentUserID).child(otherUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    dbRequestsNodeRef.child(otherUserID).child(currentUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Log.d(TAG, "onComplete: friend added!");
                                                                                        try {
                                                                                            getActivity().onBackPressed();
                                                                                        }catch (Exception e){
                                                                                            e.printStackTrace();
                                                                                        }


                                                                                    }
                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });

                                            }
                                        }
                                    });


                        }

                    }
                });
    }


    /**
     * method in charge of declining chat request
     */
    private void declineChatRequest() {

        dbRequestsNodeRef.child(currentUserID).child(otherUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            dbRequestsNodeRef.child(otherUserID).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                Log.d(TAG, "onComplete: friendship request declined");
                                                try {
                                                    Toast.makeText(getActivity(), getString(R.string.request_decline), Toast.LENGTH_SHORT).show();
                                                    getActivity().onBackPressed();
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                        }

                    }
                });



    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.requestButtonAccept:
                Log.d(TAG, "onClick: accept chat request pressed");
                acceptChatRequest();
                break;
            case R.id.requestButtonDecline:
                Log.d(TAG, "onClick: decline request pressed");
                declineChatRequest();
                break;
        }

    }
}
