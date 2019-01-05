package com.example.alvar.chatapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Activities.ContactsActivity;
import com.example.alvar.chatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private FloatingActionButton fabContacts;
    private View viewContacts;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //bind view with controller
        viewContacts = inflater.inflate(R.layout.fragment_chats, container, false);

        bind();
        fabButtonPressed();

        return viewContacts ;
    }

    private void bind() {

        fabContacts = viewContacts.findViewById(R.id.fabContacts);
    }

    /**
     * this method handles event when fab button in pressed
     */
    private void fabButtonPressed(){
        fabContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToContacts();
            }
        });
    }

    private void goToContacts() {
        Intent intentContacts = new Intent(getContext(), ContactsActivity.class);
        startActivity(intentContacts);
    }

}
