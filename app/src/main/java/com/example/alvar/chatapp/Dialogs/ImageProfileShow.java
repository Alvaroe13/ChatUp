package com.example.alvar.chatapp.Dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * This class is in charge of displaying the image profile once the toolbar has been clicked
 * @return
 */


public class ImageProfileShow extends DialogFragment {

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersRef;
    //vars
    private String currentUserID;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //init firebase method
        initFirebase();

        //Create dialog buider
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater viewinflater= getActivity().getLayoutInflater();
        View imageProfileView = viewinflater.inflate(R.layout.profile_dialog, null);
        final ImageView imageProfileDialog = imageProfileView.findViewById(R.id.imageProfileDialog);
        builder.setView(imageProfileView);


        //down here we fetch the info (image) from the db to be shown later on the Alert dialog
        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //we store the image from the db into String var
                String image = dataSnapshot.child("image").getValue().toString();

                //set image from firebase databsae to UI
                if ( image.equals("image")){
                    imageProfileDialog.setImageResource(R.drawable.profile_image);
                }else{
                    Glide.with(getActivity()).load(image).into(imageProfileDialog);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return builder.create();


    }

    /**
     * method in charge of init firebase services
     */
    private void initFirebase() {
        //Firebase auth init
        mAuth = FirebaseAuth.getInstance();
        //database init
        database = FirebaseDatabase.getInstance();
        //database ref init
        dbUsersRef = database.getReference().child("Users");
        //we store current user ID
        currentUserID = mAuth.getCurrentUser().getUid();
    }



}
