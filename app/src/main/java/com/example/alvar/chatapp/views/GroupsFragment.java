package com.example.alvar.chatapp.views;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private static final String TAG = "GroupsFragment";

    public GroupsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //we set view to fragment
       View groupView  =  inflater.inflate(R.layout.fragment_groups, container, false);

        return  groupView;
    }



}
