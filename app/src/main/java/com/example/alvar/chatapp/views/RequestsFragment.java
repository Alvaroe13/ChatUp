package com.example.alvar.chatapp.views;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithStack;


public class RequestsFragment extends Fragment {

    //log
    private static final String TAG = "RequestsFragmentPage";
    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private DatabaseReference dbRequestsNodeRef, dbUsersNode, requestTypeRef;
    //ui elements
    private RecyclerView requestsRecycler;
    private LinearLayoutManager linearLayoutManager;
    private View viewLayout;
    //vars
    private String currentUserID;


    public RequestsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: view init correctly with its methods");
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewLayout = view;
        initRecycler(view);

    }

    /**
     * we init recyclerView
     *
     * @param view
     */
    private void initRecycler(View view) {
        requestsRecycler = view.findViewById(R.id.requestsRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        requestsRecycler.setLayoutManager(linearLayoutManager);
    }

    private void initFirebase() {
        //init firebase auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }
        database = FirebaseDatabase.getInstance();
        dbRequestsNodeRef = database.getReference().child("Chat_Requests");
        dbUsersNode = database.getReference().child("Users");
        dbUsersNode.keepSynced(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser != null) {
            initFirebaseAdapter();
        }


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

                        //here we get the user id of every request made in the "Chat_Requests" node
                        // and save it into a constant.
                        final String list_user_id = getRef(position).getKey();

                        Log.i(TAG, "onBindViewHolder: user id: " + list_user_id);

                        requestTypeRef = getRef(position).child("request_type").getRef();

                        requestTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    String requestTypeFetched = dataSnapshot.getValue().toString();
                                    Log.i(TAG, "onDataChange: other user ID: " + list_user_id);

                                    //we show every request with state "received"
                                    if (requestTypeFetched.equals("received")) {

                                        //we point to "Users" node to retrieve image and username
                                        dbUsersNode.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                //we fetch info from the "Users" node and set it into the UI
                                                if (dataSnapshot.exists()) {

                                                    String name = dataSnapshot.child("name").getValue().toString();
                                                    String image = dataSnapshot.child("imageThumbnail").getValue().toString();

                                                    holder.userName.setText(name);

                                                    RequestOptions options = new RequestOptions()
                                                            .centerCrop()
                                                            .error(R.drawable.profile_image);

                                                    try {
                                                        Glide.with(getContext())
                                                                .setDefaultRequestOptions(options)
                                                                .load(image)
                                                                .into(holder.imageRequest);
                                                    } catch (NullPointerException e) {
                                                        Log.e(TAG, "onDataChange: exception: " + e.getMessage());
                                                    }


                                                    //here we take the user from request fragment to "ChatRequestFragment"
                                                    holder.cardViewRequest.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {

                                                            requestAnswer(list_user_id);

                                                        }
                                                    });

                                                }


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    } else if (requestTypeFetched.equals("sent")) {
                                             /* in case we have sent a request message we make sure not to
                                                show any request in our request fragment, we need to hide only the CardView
                                                as it is the container of the rest of the element*/

                                        holder.itemView.findViewById(R.id.cardViewRequest).setVisibility(View.GONE);
                                        holder.itemView.findViewById(R.id.cardViewRequest).setLayoutParams(new RecyclerView.LayoutParams(0, 0));


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
                        View requestIndividualView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_individual_layout, viewGroup, false);

                        return new RequestsViewHolder(requestIndividualView);
                    }
                };

        requestsRecycler.setAdapter(adapterFirebase);
        adapterFirebase.startListening();

    }

    /**
     * method takes the user to ChatRequestFragment
     * @param contactID
     */
    private void requestAnswer(String contactID) {
        Bundle bundle = new Bundle();
        bundle.putString("otherUserID", contactID);

        navigateWithStack(viewLayout, R.id.chatRequestFragment, bundle);
    }


    //here we bind and init elements from the UI in the individual request layout
    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageRequest;
        TextView userName;
        CardView cardViewRequest;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            imageRequest = itemView.findViewById(R.id.imageRequestsUser);
            userName = itemView.findViewById(R.id.usernameRequestIndividual);
            cardViewRequest = itemView.findViewById(R.id.cardViewRequest);

        }

    }


}


