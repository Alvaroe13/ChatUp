package com.example.alvar.chatapp.views;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
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
    private View viewLayout;
    //vars
    private String currentUserID;
    private List<User> userList = new ArrayList<>();
    private MainActivityInterface clickListener;


    public AllUsersFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        initFirebase();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called!");
        return inflater.inflate(R.layout.fragment_all_users, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called");

        viewLayout = view;

        initRecyclerView(view);
        setToolbar(getString(R.string.allUsers), view, false);
        showAllUsers();
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

                    }

                    adapter = new UsersAdapter(getContext(), userList);
                    recyclerView.setAdapter(adapter);

                    //here we user our customer method to handle click events from the fragment rather
                    // than inside of the adapter class
                    adapter.clickHandler(new UsersAdapter.OnClickListener() {
                        @Override
                        public void onItemClick(int position) {

                            //Here we retrieve the ID of the user shown in the cardView
                            String contactID = userList.get(position).getUserID();
                            Log.d(TAG, "onItemClick: user pressed ID:  " + contactID );

                            goToOtherUserProfile(contactID);

                        }
                    });



                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToOtherUserProfile(String contactID) {

        Bundle bundle =new Bundle();
        bundle.putString("contactID", contactID);
        navigateWithStack(viewLayout, R.id.otherUserFragment, bundle);

    }


    /**
     * navigate adding to the back stack
     * @param layout
     */
    private void navigateWithStack(View view , int layout, Bundle bundle){
        Navigation.findNavController(view).navigate(layout, bundle);
    }

    /**
     * navigate cleaning the stack
     * @param layout
     */
    private void navigateWithOutStack(View view, int layout){

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();

        Navigation.findNavController(view).navigate(layout, null, navOptions);

    }


/*
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //here we se connection between fragment and Activity.
        clickListener = (MainActivityInterface) context;
    }*/


}
