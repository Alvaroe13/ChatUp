package com.example.alvar.chatapp.Activities;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    //log
    private static final String TAG = "AllUsersPage";
    //firebase
    private FirebaseDatabase database;
    private DatabaseReference dbUsersRef;
    //ui elements
    private Toolbar toolbarAllUsers;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        initFirebase();
        initRecyclerView();
        setToolbar(getString(R.string.allUsers), true);
    }

    private void initFirebase() {
        //we init firebase database service
        database = FirebaseDatabase.getInstance();
        //here we init db reference and pointed to "Users" node
        dbUsersRef = database.getReference().child(getString(R.string.users_ref));
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.contactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setToolbar(String title, Boolean backOption){
        toolbarAllUsers = findViewById(R.id.toolbarAllUsers);
        setSupportActionBar(toolbarAllUsers);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backOption);
    }

    /**
     * we're going to use this method to make sure the app fetches the info from the db
     * once the activity's been executed
     */
    @Override
    protected void onStart() {
        super.onStart();

        callFirebaseAdapter();
    }

    /**
     * this method contains the logic to fill the recyclerView with the info from the database node "Users".
     */
    private void callFirebaseAdapter(){

        //we create firebaseOptions to pass it to firebaseAdapter
        FirebaseRecyclerOptions<Contacts> firebaseOptions  = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbUsersRef , Contacts.class)
                .build();

        // model and viewHolder class are the parameter
        FirebaseRecyclerAdapter< Contacts , AllUsersViewHolder > firebaseAdapter = new
                FirebaseRecyclerAdapter<Contacts, AllUsersViewHolder>(firebaseOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull AllUsersViewHolder holder, final int position, @NonNull Contacts model) {

                        //set info from db into the Cardviews
                        setInfoIntoUI( holder, model);
                        //onClick when any of the users displayed has been pressed
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //here we get user id given by Firebase
                                String otherUserId = getRef(position).getKey();
                                goToOtherUserLayout(otherUserId);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        //here we bind the user layout to the firebase adapter
                        View allUsersView = LayoutInflater.from(viewGroup.getContext()).
                                inflate(R.layout.users_individual_layout, viewGroup, false);

                        AllUsersViewHolder viewHolder = new AllUsersViewHolder(allUsersView);
                        return viewHolder;

                    }
                };

        recyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();

    }

    private void goToOtherUserLayout(String otherUserId) {
        Intent intentOtherUserProf = new Intent(AllUsersActivity.this, OtherUserProfileActivity.class);
        //we send user id through an intent
        intentOtherUserProf.putExtra("otherUserId" , otherUserId);
        startActivity(intentOtherUserProf);
    }

    private void setInfoIntoUI(AllUsersViewHolder holder, Contacts model ) {

        //  here we fetch info from database and set it to the UI
        holder.username.setText(model.getName());
        holder.currentStatus.setText(model.getStatus());
        //here we set the default image is user has not upload any pic
        if (model.getImageThumbnail().equals("imgThumbnail")){
            holder.imgProfile.setImageResource(R.drawable.profile_image);
        }else{
            //lets upload images from db to ui using glide instead of picasso
            Glide.with(getApplicationContext()).load(model.getImageThumbnail()).into(holder.imgProfile);
        }


    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder{

        //we get ui elements from all users layout
        TextView username, currentStatus;
        CircleImageView imgProfile;

        public AllUsersViewHolder(@NonNull View itemView) {
            super(itemView);

            //we init ui elements
            username = itemView.findViewById(R.id.usernameAllUsers);
            currentStatus = itemView.findViewById(R.id.statusAllUsers);
            imgProfile = itemView.findViewById(R.id.imageAllUsers);
        }
    }

}
