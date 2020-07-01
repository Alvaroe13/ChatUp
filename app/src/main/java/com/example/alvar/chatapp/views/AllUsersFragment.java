package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Adapter.UsersAdapter;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllUsersFragment extends Fragment {

    //log
    private static final String TAG = "AllUsersFragment";
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersRef;
    //ui elements
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    //vars
    private String currentUserID;
    private List<User> userList = new ArrayList<>();


    public AllUsersFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        initFirebase();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_all_users, container,false);

        initRecyclerView(layout);
        setToolbar(getString(R.string.allUsers), layout, false);
        showAllUsers();

        return layout;
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersRef = database.getReference().child(getString(R.string.users_ref));
    }

    private void initRecyclerView(View layout) {
        recyclerView = layout.findViewById(R.id.allUsersRecyclerFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    private void setToolbar(String title, View layout, boolean backOption) {
        Toolbar toolbar = layout.findViewById(R.id.toolbarUsersFragment);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_user, menu);
        inflateSearchIcon(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * method in charge of searching for users
     *
     * @param menu
     */
    private void inflateSearchIcon(Menu menu) {

        MenuItem.OnActionExpandListener expandMenuListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "onMenuItemActionExpand: inflated");
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "onMenuItemActionCollapse: collapse");
                return false;
            }
        };
        menu.findItem(R.id.searchUser).setOnActionExpandListener(expandMenuListener);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchUser).getActionView();
        searchView.setQueryHint(getString(R.string.place_username));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!TextUtils.isEmpty( query.trim() ) ) {
                    searchUser(query);
                } else{
                    showAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                if (!TextUtils.isEmpty( query.trim() ) ) {
                    searchUser(query);
                } else{
                    showAllUsers();
                }
                return false;
            }
        });

    }

    /**
     * method in charge of searching for the user
     * @param query
     */
    private void searchUser(final String query) {

        dbUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userList.clear();

                if (dataSnapshot.exists()) {
                    //loop through all user ref in db
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        User user = snapshot.getValue(User.class);

                        if (user.getUserID() != null) {

                            if (!currentUserID.equals(user.getUserID())) {
                                if (user.getName().toLowerCase().contains(query.toLowerCase())
                                        || user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                                    userList.add(user);
                                }
                            }

                        } else {
                            Log.d(TAG, "onDataChange: userID field empty");
                        }

                        adapter = new UsersAdapter(getContext(), userList);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * method retrieves all users in user node
     */
    private void showAllUsers() {

        dbUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //we clear the list first to make sure next time we open this view items are not duplicated
                userList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    User user = snapshot.getValue(User.class);

                    if (user.getUserID() != null ){

                        //here we fetch every user in userRef in db that except for the user authenticated
                        if ( !currentUserID.equals(user.getUserID()) ){
                            userList.add(user);
                        }

                    } else {
                        Log.d(TAG, "onDataChange: userId field null");
                    }

                    adapter = new UsersAdapter(getContext(), userList);
                    recyclerView.setAdapter(adapter);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




}
