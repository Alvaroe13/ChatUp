package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Activities.ChatActivity;
import com.example.alvar.chatapp.Model.Chatroom;
import com.example.alvar.chatapp.Model.Contacts;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.alvar.chatapp.Utils.Constant.CHATROOM_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Utils.Constant.DOCUMENT_ID;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private static final String TAG = "ContactsAdapter";

    //fireStore
    private FirebaseFirestore mDb;
    private DocumentReference chatroomRef;
    //vars
    private Contacts contact;
    private Context context;
    private List<Contacts> contactsList;
    private String currentUserID;
    private String name, image, status;

    //We get currentUserId as param to be later sent to the chatActivity when cardView is pressed
    public ContactsAdapter(Context context, List<Contacts> contactsList, String currentUserID) {
        this.context = context;
        this.contactsList = contactsList;
        this.currentUserID = currentUserID;
    }


    @NonNull
    @Override
    public ContactsAdapter.ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.contacts_individual_layout, parent, false);
        initFirestore();
        return new ContactsAdapter.ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsAdapter.ContactsViewHolder holder, int position) {

        contact = contactsList.get(position);
        Log.i(TAG, "onBindViewHolder: contactID = " + contact.getContactID());

        fetchInfo(holder, contact.getContactID());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatRoom(contact.getContactID(), holder);

            }
        });
        


    }

    private void initFirestore(){
        mDb = FirebaseFirestore.getInstance();
        chatroomRef = mDb.collection("Chatroom").document();
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


    @Override
    public int getItemCount() {
        return contactsList.size();
    }



    private void setUI(ContactsAdapter.ContactsViewHolder holder , String name, String status, String image) {

        holder.username.setText(name);
        holder.userStatus.setText(status);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        //here we set image from database into imageView
        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(image)
                .into(holder.userPhoto);
    }

    /**
     * lets take the user to the chat room and create a document in firestore to be user by location
     * fragment
     * @param contactID
     */
    private void goToChatRoom(final String contactID, final ContactsAdapter.ContactsViewHolder holder) {

        Log.d(TAG, "goToChatRoom: contactID: " + contactID);
        Log.d(TAG, "goToChatRoom: username: " + name);
        Log.d(TAG, "goToChatRoom: image: " + image);

        Chatroom chatroom = new Chatroom();
        chatroom.setMember1ID(currentUserID); //current user
        chatroom.setMember2ID(contactID);   //other user in chatroom

        final String collectionID = chatroomRef.getId();  // random ID provided by Firestore db.
        final String documentID = currentUserID + "_" + contactID;

        //we create chatroom  document when a chatroom is created by the user in the UI;
        chatroomRef = mDb.collection("Chatroom")
                .document(documentID);

        chatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intentChatRoom = new Intent(context, ChatActivity.class);
                    intentChatRoom.putExtra(CONTACT_ID, contactID);
                    intentChatRoom.putExtra(CONTACT_NAME, name);
                    intentChatRoom.putExtra(CONTACT_IMAGE, image);
                    intentChatRoom.putExtra(CHATROOM_ID, collectionID);
                    intentChatRoom.putExtra(DOCUMENT_ID, documentID);
                    holder.itemView.getContext().startActivity(intentChatRoom);
                }

            }
        });


    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {


        public CardView cardView;
        public CircleImageView userPhoto;
        public TextView username, userStatus;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewContact);
            userPhoto = itemView.findViewById(R.id.imageContactUsers);
            username = itemView.findViewById(R.id.usernameContactUsers);
            userStatus = itemView.findViewById(R.id.statusContactUsers);
        }



    }



}
