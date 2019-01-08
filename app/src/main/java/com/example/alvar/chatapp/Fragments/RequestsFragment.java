package com.example.alvar.chatapp.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.SnackbarHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    //log
    private static final String TAG = "RequestsFragmentPage";
    //firebase serives
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRequestsNodeRef, dbUsersNode, dbContactsNodeRef;
    //ui elements
    private RecyclerView requestsRecycler;
    private CoordinatorLayout coordinatorLayout;
    //vars
    private String currentUserID;
    private String list_user_id;


    public RequestsFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // bind view with controller
        View requestsView =  inflater.inflate(R.layout.fragment_requests, container, false);
        Log.i(TAG, "onCreateView: view init correctly with its methods");

        UI(requestsView);
        initFirebase();
        initRecycler(requestsView);



        return requestsView;
    }

    private void UI(View view){
        Log.i(TAG, "initRecycler: views init successful");
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
    }


    /**
     * we init recyclerView
     * @param view
     */
    private void initRecycler(View view){
        Log.i(TAG, "initRecycler: recycler init successful");
        requestsRecycler = view.findViewById(R.id.requestsRecyclerView);
        requestsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
    }

    private void initFirebase(){
        //init firebase auth
        auth = FirebaseAuth.getInstance();
        //we get currentUser ID
        currentUserID = auth.getCurrentUser().getUid();
        //init database
        database = FirebaseDatabase.getInstance();
        // we aim to "Contacts" node from db
        dbRequestsNodeRef = database.getReference().child("Chat_Requests");
        // we aim to "Users" node from db
        dbUsersNode = database.getReference().child("Users");
        dbUsersNode.keepSynced(true);
        //init "Contacts" node
        dbContactsNodeRef = database.getReference().child("Contacts");
        Log.i(TAG, "initFirebase: init firebase correctly");
    }

    @Override
    public void onStart() {
        super.onStart();

        initFirebaseAdapter();

    }

    /**
     * this method contains the logic to fill the recyclerView with the info from the database node "Contacts".
     */
    private void initFirebaseAdapter() {

        //this code is the one i charge of the query to the firebase db
        FirebaseRecyclerOptions<Contacts> requests =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(dbRequestsNodeRef.child(currentUserID), Contacts.class)
                        .build();

        /*this is the adapter, the one in charge of populating the recyclerView with the info from the db
           using the query created above*/
        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapterFirebase =
                        new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(requests) {
                            @Override
                            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {

                           //this shows the buttons from the individual layout as we've set the buttons invisible by default
                             holder.itemView.findViewById(R.id.buttonAccept).setVisibility(View.VISIBLE);
                             holder.itemView.findViewById(R.id.buttonDeclineRequest).setVisibility(View.VISIBLE);

                             //here we get the user id of every request made in the "Chat_Requests" node
                             list_user_id = getRef(position).getKey();

                             Log.i(TAG, "onBindViewHolder: user id: " + list_user_id);


                             DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();


                             requestTypeRef.addValueEventListener(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                     if (dataSnapshot.exists()){

                                         String requestTypeFetched = dataSnapshot.getValue().toString();

                                         if (requestTypeFetched.equals("received")){

                                             //we point to "Users" node to retrieve image and username
                                             dbUsersNode.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                 @Override
                                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                     //we fetch info from the "Users" node and set it into the UI

                                                     if (dataSnapshot.exists()){

                                                         String name = dataSnapshot.child("name").getValue().toString();
                                                         String image = dataSnapshot.child("imageThumbnail").getValue().toString();

                                                         holder.userName.setText(name);
                                                         if ( image.equals("imgThumbnail")){
                                                             holder.imageRequest.setImageResource(R.drawable.profile_image);
                                                         } else{
                                                                 Glide.with(getContext()).load(image).into(holder.imageRequest);

                                                         }

                                                     }



                                                 }

                                                 @Override
                                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                                 }
                                             });

                                         }



                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                 }
                             });

                            }

                            @NonNull
                            @Override
                            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                //we bind the "request_individual_layout" to it's controller
                                View requestIndividualView = LayoutInflater.from(viewGroup.getContext())
                                        .inflate(R.layout.request_individual_layout, viewGroup, false);

                                RequestsViewHolder viewHolder = new RequestsViewHolder(requestIndividualView);
                                return viewHolder;
                            }
                        };

        requestsRecycler.setAdapter(adapterFirebase);
        adapterFirebase.startListening();

    }


    //here we bind and init elements from the UI in the individual request layout
    public class RequestsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imageRequest;
        TextView userName ;
        Button acceptButton, declineButton;
        CardView cardViewRequest;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            imageRequest = itemView.findViewById(R.id.imageRequestsUser);
            acceptButton = itemView.findViewById(R.id.buttonAccept);
            declineButton = itemView.findViewById(R.id.buttonDeclineRequest);
            userName = itemView.findViewById(R.id.usernameRequestIndividual);
            cardViewRequest = itemView.findViewById(R.id.cardViewRequest);


            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    acceptChatRequest();
                }
            });

            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    declineChatRequest();

                }
            });



        }

        /**
         * method in charge of accepting chat request
         */
        private void acceptChatRequest() {

            /*at this point since we have accepted the chat request
              we add the new contact in the "Contacts" node
             */
            dbContactsNodeRef.child(currentUserID).child(list_user_id)
                    .child("contact_status")
                    .setValue("saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                dbContactsNodeRef.child(list_user_id).child(currentUserID)
                                        .child("contact_status")
                                        .setValue("saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    /*now from this point onward we remove request from request tab
                                                      by deleting such request from the "Chat_Requests" node
                                                     */

                                                    dbRequestsNodeRef.child(currentUserID).child(list_user_id)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        dbRequestsNodeRef.child(list_user_id).child(currentUserID)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){

                                                                                            //show confirmation message to the user
                                                                                            SnackbarHelper.showSnackBarLong(coordinatorLayout,
                                                                                                                     getString(R.string.friendAdded));
                                                                                        }
                                                                                    }
                                                                                });

                                                                    }

                                                                }
                                                            });

                                                }
                                            }
                                        });


                            }

                        }
                    });
        }

        /**
         * method in charge of declining chat request
         */
        private void declineChatRequest() {

            dbRequestsNodeRef.child(currentUserID).child(list_user_id)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                dbRequestsNodeRef.child(list_user_id).child(currentUserID)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    SnackbarHelper.showSnackBarLongRed(coordinatorLayout,
                                                                            getString(R.string.chatRequestRejected));

                                                }
                                            }
                                        });
                            }

                        }
                    });



        }


    }


    }


