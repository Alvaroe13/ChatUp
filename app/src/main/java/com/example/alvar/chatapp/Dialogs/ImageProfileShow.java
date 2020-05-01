package com.example.alvar.chatapp.Dialogs;


import android.app.Dialog;
import android.content.Intent;
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
import com.example.alvar.chatapp.Activities.ImageActivity;
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
    //ui
    private View imageProfileView;
    private LayoutInflater viewInflater;
    private AlertDialog.Builder builder;
    private ImageView imageProfileDialog;
    //vars
    private String currentUserID, image;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //init firebase method
        initFirebase();

        return showAlertDialog();
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

    /**
     * this method inflates the Alert Dialog layout
     * @return
     */
    private  AlertDialog showAlertDialog() {

        //Create dialog builder
        builder = new AlertDialog.Builder(getActivity());
        viewInflater= getActivity().getLayoutInflater();
        imageProfileView = viewInflater.inflate(R.layout.profile_dialog, null);
        imageProfileDialog = imageProfileView.findViewById(R.id.imageProfileDialog);
        builder.setView(imageProfileView);
        //lets fetch info from db
        fetchInfoFromDB(imageProfileDialog);

        return builder.create();
    }

    /**
     * method is in charge of sending the photo information to the Image room if photo pressed
     */
    private void imagePressed() {
        imageProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //note that we use "messageContent" as id like in the rest of the app
                Intent i = new Intent(getActivity(), ImageActivity.class);
                i.putExtra("messageContent", image);
                startActivity(i);
            }
        });
    }

    /**
     * this one fetches the info from db
     * @param imageProfileDialog
     */
    private void fetchInfoFromDB(final ImageView imageProfileDialog) {

        //down here we fetch the info (image) from the db to be shown later on the Alert dialog
        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    //we store the image from the db into String var
                    image = dataSnapshot.child("image").getValue().toString();

                    if (getActivity() == null) {
                        Log.i(TAG, "onDataChange: error with context: ");
                        return ;
                    }

                    //set image from firebase database to UI
                    if ( image.equals("image")){
                        imageProfileDialog.setImageResource(R.drawable.profile_image);
                    }
                    else{
                        Glide.with(getActivity()).load(image).into(imageProfileDialog);
                    }

                    imagePressed();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
