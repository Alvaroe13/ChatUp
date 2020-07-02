package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtherUserFragment extends Fragment {

    private static final String TAG = "OtherUserFragment";

    public OtherUserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        incomingBundle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_other_user, container, false);
        // Inflate the layout for this fragment

        return view;
    }

    private void incomingBundle() {

        Bundle bundle = this.getArguments();
        if (bundle != null){
            Log.d(TAG, "incomingBundle, contactID received: " + bundle.getString("contactID"));
        }


    }
}
