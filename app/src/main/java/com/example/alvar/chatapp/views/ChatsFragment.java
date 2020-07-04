package com.example.alvar.chatapp.views;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.Adapter.ChatsAdapter;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.viewModels.ChatListViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragmentPage";
    //firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    //ui elements
    private RecyclerView chatRecyclerView;
    //vars
    private String currentUserID;
    private ChatsAdapter chatsAdapter;
    private ChatListViewModel viewModel;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_chats, container, false);
    }

    /**
     * method call right after the view's been created, is better to init ui elemtns here
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRecyclerView(view);
        // viewModel stuff
        initViewModel();
        connectionWithViewModel(currentUserID);
        initObserver();

    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null){
            currentUserID = auth.getCurrentUser().getUid();
        }
    }


    private void initRecyclerView(View view) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager( getContext() ));
        chatRecyclerView.setHasFixedSize(true);

    }

    //viewModel area
    private void initViewModel(){
        Log.d(TAG, "initViewModel: called");
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
    }

    private void connectionWithViewModel(String userID){
        Log.d(TAG, "connectionWithViewModel: called");
        viewModel.connectionWithRepo(userID);
    }

    /**
     * show conversations in the fragment
     */
    private void initObserver(){

        Log.d(TAG, "initObserver: called");
        viewModel.getChats().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null){
                    Log.d(TAG, "initObserver onChanged: called");

                    chatsAdapter = new ChatsAdapter(getContext(), users);
                    chatRecyclerView.setAdapter(chatsAdapter);
                    chatsAdapter.notifyDataSetChanged();

                }else{
                    Log.d(TAG, "onChanged: null response from db");
                }


            }
        });
    }








}
