package com.example.alvar.chatapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Adapter.ViewPagerAdapter;
import com.example.alvar.chatapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment  {

    private static final String TAG = "HomeFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, tokenNodeRef;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userDocRef;
    //UI elements
    private Toolbar toolbarMain;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ActionBarDrawerToggle burgerIcon;
    //vars
    private String currentUserID;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called");

        initFirebase();
        if (currentUser != null){
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        initFirestore();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called ");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: called as well!");
        setToolbar("ChatUp", view, true);
        ui(view);
    }

    private void ui(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        initPageAdapter(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * init firebase services
     */
    private void initFirebase() {
        //Firebase auth init
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //database init
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        tokenNodeRef= database.getReference().child("Tokens");
    }

    /**
     * init firestore services
     */
    private void initFirestore() {
        mDb = FirebaseFirestore.getInstance();
        userDocRef = mDb.collection("Users").document(currentUserID);
    }

    /**
     * this method sets toolbar and it's details
     *
     * @param title
     */
    private void setToolbar(String title, View view, Boolean backOption) {
        //create toolbar
        toolbarMain = view.findViewById(R.id.toolbarMain);
        //we set the toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarMain);
        //we pass the title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }



    /**
     * this method is in charge of creating the tabs and setting it's title
     * @param viewPager
     */
    private void initPageAdapter(ViewPager viewPager) {

        ViewPagerAdapter Adapter = new ViewPagerAdapter(getChildFragmentManager());
        Adapter.addFragment(new GroupsFragment(), getString(R.string.groups));
        Adapter.addFragment(new ChatsFragment(), getString(R.string.chat));
        Adapter.addFragment(new RequestsFragment(), getString(R.string.requests));
        viewPager.setAdapter(Adapter);
        //this line sets the second fragment as default when app is launched.
        viewPager.setCurrentItem(1);
    }


}
