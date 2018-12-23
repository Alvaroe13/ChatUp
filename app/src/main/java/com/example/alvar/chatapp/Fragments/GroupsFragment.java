package com.example.alvar.chatapp.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.alvar.chatapp.Activities.MainActivity;
import com.example.alvar.chatapp.GroupChatActivity;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private static final String TAG = "GroupsFragment";

    //firebase
    private FirebaseDatabase groupDatabase;
    private DatabaseReference groupDbRef;

    //UI elements
    private ListView listView;
    private FloatingActionButton fabGroups;
    //vars
    private ArrayAdapter<String> adapter;
    private ArrayList<String> groupList;
    private View groupView;



    public GroupsFragment(){

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //we set view to fragment
        groupView  =  inflater.inflate(R.layout.fragment_groups, container, false);
        Log.i(TAG, "onCreateView: fragment initialized");

        initFirebaseDb();
        UI();
        initAdapter();
        retrieveGroupsInfo();
        addNewGroup();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //we get the name of the group we have clicked from the listView
                String groupName = parent.getItemAtPosition(position).toString();

                Intent goToGroupChat = new Intent(getContext(), GroupChatActivity.class);
                //send the name of the group through this intent
                goToGroupChat.putExtra("Group Name", groupName);
                startActivity(goToGroupChat);
            }
        });


        return  groupView;
    }

    /**
     * init ui elements
     */
    private void UI() {
        listView = groupView.findViewById(R.id.listView);
        fabGroups = groupView.findViewById(R.id.fabGroup);
    }

    /**
     * method in charge of initializing firebase database
     */
    private void initFirebaseDb() {
        groupDatabase = FirebaseDatabase.getInstance();
        groupDbRef = groupDatabase.getReference().child("Groups");
    }

    /**
     * method in charge of initializing adapter to bind info from database with listView
     */
    private void initAdapter() {
        //set adapter to fragment
        groupList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, groupList);
        listView.setAdapter(adapter);

    }

    /**
     * this method is in charge of retrieving the info of the "Groups" node from the database
     */
    private void retrieveGroupsInfo() {

        groupDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator =  dataSnapshot.getChildren().iterator();

                //iterates through the "Groups" node from firebase database
                while (iterator.hasNext()){

                    set.add(((DataSnapshot)iterator.next()).getKey());

                }

                groupList.clear();
                groupList.addAll(set);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method called when fab button's pressed
     */
    private void addNewGroup() {
        fabGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroupRequest();
            }
        });

    }

    /**
     * Pop up message in charge of setting group name
     */
    private void createGroupRequest() {
        AlertDialog.Builder requestPopUp = new AlertDialog.Builder(getActivity());
        requestPopUp.setTitle(getString(R.string.createGroup));

        final EditText groupNameField = new EditText(getContext());
        groupNameField.setHint(getString(R.string.exampleHint));
        requestPopUp.setView(groupNameField);

        //positive button
        requestPopUp.setPositiveButton( getString(R.string.create), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName =  groupNameField.getText().toString();

                //if title field is empty
                if (groupName.equals("")){
                    //show message to the user
                    Toast.makeText(getContext(), getString(R.string.enterGroupName), Toast.LENGTH_LONG).show();
                //if name is enter
                } else {
                    //create group
                    createGroupChat(groupName);
                }



            }
        });     //negative button
        requestPopUp.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        requestPopUp.show();
    }


    /**
     * this method create new database node in Firebase
     * @param groupName
     */
    private void createGroupChat(String groupName) {

        //we create group inside "Groups" node
        groupDbRef.child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(getContext(), getString(R.string.groupCreated), Toast.LENGTH_SHORT).show();
                } else{
                    //if something goes wrong when creating new group on firebase database we show
                    String error = task.getException().getMessage();
                    Toast.makeText(getContext(), error , Toast.LENGTH_LONG).show();
                }

            }
        });
    }


}
