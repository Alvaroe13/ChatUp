package com.example.alvar.chatapp.Dialogs;


import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
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

    private static final String TAG = "ImageProfileShow";

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

        //Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater viewInflater= getActivity().getLayoutInflater();
        View imageProfileView = viewInflater.inflate(R.layout.profile_dialog, null);
        final ImageView imageProfileDialog = imageProfileView.findViewById(R.id.imageProfileDialog);
        builder.setView(imageProfileView);


        //down here we fetch the info (image) from the db to be shown later on the Alert dialog
        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //we store the image from the db into String var
                String image = dataSnapshot.child("image").getValue().toString();

                if (getActivity() == null) {

                    Log.i(TAG, "onDataChange: error with context: ");
                     return ;
                }

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
