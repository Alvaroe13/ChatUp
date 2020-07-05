package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageLargeFragment extends Fragment {

    private static final String TAG = "ImageLargeFragment";

    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //ui
    private ImageView image;
    // vars
    private String messageContent, currentUserID;


    public ImageLargeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");
        initFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_image_large, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called");
        binUI(view);
        setImage();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
    }

    /**
     * ui elements
     */
    private void binUI(View view) {
        image = view.findViewById(R.id.imageBig);
    }

    /**
     * here we show the image
     */
    private void setImage() {

        //in this variable we store the info of any image coming from any activity.
        if (getArguments() != null) {
            messageContent = getArguments().getString("messageContent");
            Log.i(TAG, "fetchInfoIntent: message content: " + messageContent);
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        try {

            Glide.with(getContext())
                    .setDefaultRequestOptions(options)
                    .load(messageContent)
                    .into(image);

        } catch (Exception e) {
            Log.e(TAG, "setImage: error " + e.getMessage());
        }


    }



}
