package com.example.alvar.chatapp.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    // firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    // List to contain the messages
    private List<Messages> messagesList;
    private String currentUserID;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    private void initFirebase(){
        auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
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
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int position) {


        initFirebase();

        //first of all we get current user id
        currentUserID = auth.getCurrentUser().getUid();




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
