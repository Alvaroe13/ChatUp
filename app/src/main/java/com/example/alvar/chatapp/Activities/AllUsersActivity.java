package com.example.alvar.chatapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AllUsersActivity extends AppCompatActivity {

    //log
    private static final String TAG = "AllUsersPage";
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersRef;
    //ui elements
    private Toolbar toolbarAllUsers;
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    //vars
    private String currentUserID;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        initFirebase();
        initRecyclerView();
        setToolbar(getString(R.string.allUsers), true);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getUid();
        //we init firebase database service
        database = FirebaseDatabase.getInstance();
        //here we init db reference and pointed to "Users" node
        dbUsersRef = database.getReference().child(getString(R.string.users_ref));
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.contactRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setToolbar(String title, Boolean backOption) {
        toolbarAllUsers = findViewById(R.id.toolbarAllUsers);
        setSupportActionBar(toolbarAllUsers);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_user, menu);

        inflateSearchIcon(menu);

        return super.onCreateOptionsMenu(menu);
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

                        adapter = new UsersAdapter(AllUsersActivity.this, userList);
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
     * we're going to use this method to make sure the app fetches the info from the db
     * once the activity's been executed
     */
    @Override
    protected void onStart() {
        super.onStart();

        showAllUsers();
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

                    adapter = new UsersAdapter(AllUsersActivity.this, userList);
                    recyclerView.setAdapter(adapter);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
