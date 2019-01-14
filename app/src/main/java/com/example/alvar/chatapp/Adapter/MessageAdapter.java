package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapterPage";

    // firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbMessagesNodeRef;
    // List to contain the messages
    private List<Messages> messagesList;
    private String currentUserID;
    private Context mContext;

    public MessageAdapter(List<Messages> messagesList, Context mContext) {
        this.messagesList = messagesList;
        this.mContext = mContext;
    }

    private void initFirebase(){
       auth =  FirebaseAuth.getInstance();
       database = FirebaseDatabase.getInstance();
       dbUsersNodeRef = database.getReference().child("Users");
       dbMessagesNodeRef = database.getReference().child("Messages");
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // we bind the layout with this controller and the sub class "MessageViewHolder"

        View viewChat = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_layout, viewGroup, false);
        return new MessageViewHolder(viewChat);
    }

    /**
     * here in this method lies the logic to fill the recyclerView
     * @param messageViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int position) {


        initFirebase();

        //first of all we get current user id
        currentUserID = auth.getCurrentUser().getUid();



        Messages messages = messagesList.get(position);

        String messageSenderID = messages.getSenderByID();
        String messageType = messages.getType();
        Boolean messageSeen = messages.getSeen();

        Log.i(TAG, "onBindViewHolder: sender ID: " + messageSenderID);

        dbUsersNodeRef.child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()){

                    String imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();

                    if (imageThumbnail.equals("imgThumbnail")){
                        messageViewHolder.imageContact.setImageResource(R.drawable.imgdefault);
                    }else{
                        Glide.with(mContext).load(imageThumbnail).into(messageViewHolder.imageContact);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        if (messageType.equals("text")){

            messageViewHolder.imageContact.setVisibility(View.INVISIBLE);
            messageViewHolder.textRightSide.setVisibility(View.INVISIBLE);
            messageViewHolder.textLeftSide.setVisibility(View.INVISIBLE);


            //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the message)
            if (currentUserID.equals(messageSenderID)){

                messageViewHolder.textRightSide.setVisibility(View.VISIBLE);
                messageViewHolder.textRightSide.setBackgroundResource(R.drawable.right_message_layout);
                messageViewHolder.textRightSide.setText(messages.getMessage());
            }
            //if the other user is the one sending the message
            else{

                messageViewHolder.textLeftSide.setVisibility(View.VISIBLE);
                messageViewHolder.imageContact.setVisibility(View.VISIBLE);
                messageViewHolder.textLeftSide.setBackgroundResource(R.drawable.left_message_layout);
                messageViewHolder.textLeftSide.setText(messages.getMessage());

            }


        }




    }

    @Override
    public int getItemCount() {
        //get the size of the List
        return messagesList.size();
    }

    /**
     * view holder class
     */
    public class MessageViewHolder extends RecyclerView.ViewHolder{

        //UI elements
        TextView textRightSide, textLeftSide;
        CircleImageView imageContact;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);


            textLeftSide = itemView.findViewById(R.id.textLeft);
            textRightSide = itemView.findViewById(R.id.textRight);
            imageContact = itemView.findViewById(R.id.imageChat);

        }


    }



}