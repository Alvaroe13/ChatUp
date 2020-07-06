package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private static final String TAG = "ContactsAdapter";

    //vars
    private Context context;
    private List<Contacts> contactsList;
    private String name, image, status;
    private OnClickListener clickListener;

    //We get currentUserId as param to be later sent to the chatActivity when cardView is pressed
    public ContactsAdapter(Context context, List<Contacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }


    @NonNull
    @Override
    public ContactsAdapter.ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.contacts_individual_layout, parent, false);
        return new ContactsAdapter.ContactsViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsAdapter.ContactsViewHolder holder, int position) {

        final Contacts contact = contactsList.get(position);
        Log.i(TAG, "onBindViewHolder: contactID = " + contact.getContactID());
        fetchInfo(holder, contact.getContactID());

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }



    private void fetchInfo(final ContactsAdapter.ContactsViewHolder holder , String contactID) {

        FirebaseDatabase database;
        DatabaseReference dbUsersNodeRef;

        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users").child(contactID);

        dbUsersNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("imageThumbnail").getValue().toString();

                setUI(holder, name, status, image );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void setUI(ContactsAdapter.ContactsViewHolder holder , String name, String status, String image) {

        holder.username.setText(name);
        holder.userStatus.setText(status);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        try{
            //here we set image from database into imageView
            Glide.with(context)
                    .setDefaultRequestOptions(options)
                    .load(image)
                    .into(holder.userPhoto);
        }catch (Exception e){
            Log.e(TAG, "setUI: error: " + e.getMessage() );
        }


    }

    /**
     * this method will handle the click event in this adapter (Android best practices)
     * @param listener
     */
    public void clickHandler(OnClickListener listener){
        clickListener = listener;
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder {


        public CardView cardView;
        public CircleImageView userPhoto;
        public TextView username, userStatus;


        public ContactsViewHolder(@NonNull View itemView, final OnClickListener clickListener) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            clickListener.onItemClick(position); //onItemClick is coming from within the interface
                            Log.d(TAG, "onClick: contactID " );
                        }
                    }

                }
            });

            cardView = itemView.findViewById(R.id.cardViewContact);
            userPhoto = itemView.findViewById(R.id.imageContactUsers);
            username = itemView.findViewById(R.id.usernameContactUsers);
            userStatus = itemView.findViewById(R.id.statusContactUsers);
        }



    }

    public interface OnClickListener{
        void onItemClick(int position);
    }




}
