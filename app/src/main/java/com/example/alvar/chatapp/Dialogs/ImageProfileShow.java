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
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


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
    private String currentUserID;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateDialog: called");
        initFirebase();
        return showAlertDialog();
    }

    /**
     * method in charge of init firebase services
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersRef = database.getReference().child("Users");
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

        getIncomingBundle();

        return builder.create();
    }

    private void getIncomingBundle() {
        if (getArguments() != null){
            String profilePic = getArguments().getString("image");
            Log.d(TAG, "getIncomingBundle: image : " + profilePic);
            setPhoto(profilePic);
        }

    }

    private void setPhoto(String profilePic) {

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        try {
            //here we set image from database into imageView
            Glide.with(getContext())
                    .setDefaultRequestOptions(options)
                    .load(profilePic)
                    .into(imageProfileDialog);

        } catch (NullPointerException e) {
            Log.e(TAG, "infoFetched: error loading image");
        }

    }


}
