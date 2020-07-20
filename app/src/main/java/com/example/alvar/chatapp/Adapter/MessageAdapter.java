package com.example.alvar.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapterPage";

    // firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef, dbChatsNodeRef;
    // List to contain the messages
    private List<Messages> messagesList;
    private String currentUserID;
    private Context mContext;
    private String contactID;
    private String contactName;
    private OnClickListener clickListener;
    private OnLongClickListener longClickListener;

    public MessageAdapter(Context mContext, List<Messages> messagesList, OnClickListener clickListener,
                          String contactName, OnLongClickListener longClickListener) {
        this.messagesList = messagesList;
        this.mContext = mContext;
        this.clickListener = clickListener;
        this.contactName = contactName;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // we bind the layout with this controller and the sub class "MessageViewHolder"
        View viewChat = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_layout, viewGroup, false);
        //init firebase services as soon as we inflate the view
        initFirebase();
        return new MessageViewHolder(viewChat, clickListener, longClickListener);
    }

    /**
     * here in this method lies the logic to fill the recyclerView
     *
     * @param messageViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int position) {

        //first of all we get current user id
        currentUserID = auth.getCurrentUser().getUid();
        Log.d(TAG, "onBindViewHolder: currentUserID= " + currentUserID);
        // here we retrieve all messages in chat room and stored into a messageList type of var.
        Messages messages = messagesList.get(position);

        String messageSenderID = messages.getSenderID();
        String messageReceiverID = messages.getReceiverID();
        String messageType = messages.getType();
        String messageInfo = messages.getMessage();
        String messageTime = messages.getMessageTime();

        if (!messageSenderID.equals(currentUserID)) {
            contactID = messageSenderID;
            Log.d(TAG, "onBindViewHolder: contactId= " + contactID);
        } else if (!messageReceiverID.equals(currentUserID)) {
            contactID = messageReceiverID;
            Log.d(TAG, "onBindViewHolder: contactId= " + contactID);
        }

        //we fetch info from db Users node
        infoFetchedFromDb(messageSenderID, messageViewHolder);
        //here's where fun with the layouts starts
        layoutToShow(messageType, messageSenderID, messageInfo, messageTime, messageViewHolder, position);


    }


    /**
     * this method is the one in charge of establishing the number of items to be shown in the recyclerView
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (messagesList != null) {
            //get the size of the List is is not null
            return messagesList.size();
        }
        return -1;
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbChatsNodeRef = database.getReference().child("Chats").child("Messages");
    }

    /**
     * here we fetch info from db and fill the fields with it
     *
     * @param messageSenderID
     * @param messageViewHolder
     */
    private void infoFetchedFromDb(String messageSenderID, final MessageViewHolder messageViewHolder) {

        //here we fetch image from db
        dbUsersNodeRef.child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    //fetch image info from db
                    String imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();

                    //GLIDE
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .error(R.drawable.profile_image);

                    //if user has uploaded a pic from device into ChatUp profile settings we retrieve it and show it here
                    Glide.with(mContext.getApplicationContext())
                            .setDefaultRequestOptions(options)
                            .load(imageThumbnail)
                            .into(messageViewHolder.imageContact);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    /**
     * this method is in charge of showing the correct layout in chat room according to the situation
     *
     * @param messageType
     * @param messageSenderID
     * @param messageInfo
     * @param messageTime
     * @param messageViewHolder
     */
    private void layoutToShow(String messageType, String messageSenderID, String messageInfo,
                              String messageTime, MessageViewHolder messageViewHolder, int position) {

        // they're all gone by default
        layoutVisibilityGone(messageViewHolder);

        //lets show the message depending on the type of message
        switch (messageType) {
            case "text":
                //we show layout accordingly
                showTextLayout(messageSenderID, messageInfo, messageTime, messageViewHolder );
                break;
            case "image":
                //we show layout accordingly
                showImageLayout(messageSenderID, messageInfo, messageViewHolder );
                break;
            case "map":
                showMapLayout(messageSenderID,  messageViewHolder );
                break;
            default: //if message type is either pdf or docx.
                showDocumentLayout(messageSenderID, messageViewHolder );
        }

    }

    /**
     * method in charge of setting every view in chat layout as GONE and we make them visible accordingly
     * @param messageViewHolder
     */
    private void layoutVisibilityGone(MessageViewHolder messageViewHolder) {
        messageViewHolder.imageContact.setVisibility(View.GONE);
        messageViewHolder.textRightSide.setVisibility(View.GONE);
        messageViewHolder.textLeftSide.setVisibility(View.GONE);
        messageViewHolder.sendImageLeft.setVisibility(View.GONE);
        messageViewHolder.sendImageRight.setVisibility(View.GONE);
        messageViewHolder.sendMapLeft.setVisibility(View.GONE);
        messageViewHolder.sendMapRight.setVisibility(View.GONE);
    }

    /**
     * method in charge of showing layout only when it comes to a message "text" type.
     *
     * @param messageSenderID
     * @param messageInfo
     * @param messageTime
     * @param messageViewHolder
     */
    private void showTextLayout(String messageSenderID, String messageInfo, String messageTime,
                                MessageViewHolder messageViewHolder ) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the message)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.textRightSide.setVisibility(View.VISIBLE);
            messageViewHolder.textRightSide.setBackgroundResource(R.drawable.right_message_layout);
            messageViewHolder.textRightSide.setText(messageInfo + "  " + messageTime);
            messageViewHolder.textRightSide.setTextSize(15);
        }
        //if the other user is the one sending the message
        else {
            messageViewHolder.textLeftSide.setVisibility(View.VISIBLE);
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.textLeftSide.setBackgroundResource(R.drawable.left_message_layout);
            messageViewHolder.textLeftSide.setText(messageInfo + "  " + messageTime);
            messageViewHolder.textLeftSide.setTextSize(15);
        }

    }

    /**
     * method in charge of showing layout only when it comes to a message "image" type.
     *
     * @param messageSenderID
     * @param messageInfo
     * @param messageViewHolder
     */
    private void showImageLayout(String messageSenderID, String messageInfo,
                                 final MessageViewHolder messageViewHolder  ) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the image)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendImageRight.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(messageInfo).into(messageViewHolder.sendImageRight);

        }
        //if the other user is the one sending the image
        else {
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.sendImageLeft.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(messageInfo).into(messageViewHolder.sendImageLeft);

        }
    }


    /**
     * method in charge of showing file (pdf/docx) when sent by any user
     *
     * @param messageSenderID
     * @param messageViewHolder
     */
    private void showDocumentLayout(String messageSenderID, final MessageViewHolder messageViewHolder ) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the file)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendImageRight.setVisibility(View.VISIBLE);
            //let's make sure every time we sent a file Glide retrieves the Doc image template
            // to avoid being one file replaced when other is send after

            //image template
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .error(R.drawable.file);

            Glide.with(mContext.getApplicationContext())
                    .setDefaultRequestOptions(options)
                    .load("")
                    .into(messageViewHolder.sendImageRight);

        }
        //if the other user is the one sending the file
        else {
            messageViewHolder.sendImageLeft.setVisibility(View.VISIBLE);
            //let's make sure every time we sent a file Glide retrieves the Doc image template
            // to avoid being one file replaced when other is send after

            //image template
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .error(R.drawable.file);

            Glide.with(mContext.getApplicationContext())
                    .setDefaultRequestOptions(options)
                    .load("")
                    .into(messageViewHolder.sendImageLeft);


        }
    }

    // -------------------------------------------- maps related -------------------------------

    private void showMapLayout(String messageSenderID , final MessageViewHolder messageViewHolder ) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the message)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendMapRight.setVisibility(View.VISIBLE);
            messageViewHolder.sendMapRight.setClickable(true);

        }
        //if the other user is the one sending the message
        else {
            messageViewHolder.sendMapLeft.setVisibility(View.VISIBLE);
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
        }

    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        //UI elements
        TextView textRightSide, textLeftSide;
        CircleImageView imageContact;
        ImageView sendImageLeft, sendImageRight, sendMapLeft, sendMapRight;
        OnClickListener clickListener;
        OnLongClickListener longClickListener;

        public MessageViewHolder(@NonNull View itemView, OnClickListener clickListener, OnLongClickListener longClickListener) {
            super(itemView);
            this.clickListener = clickListener;
            this.longClickListener = longClickListener;

            textLeftSide = itemView.findViewById(R.id.textLeft);
            textRightSide = itemView.findViewById(R.id.textRight);
            imageContact = itemView.findViewById(R.id.imageChat);
            sendImageLeft = itemView.findViewById(R.id.imageLeft);
            sendImageRight = itemView.findViewById(R.id.imageRight);
            sendMapLeft = itemView.findViewById(R.id.mapLeft);
            sendMapRight = itemView.findViewById(R.id.mapRight);
            //single click listeners
            sendMapLeft.setOnClickListener(this);
            sendMapRight.setOnClickListener(this);
            sendImageLeft.setOnClickListener(this);
            sendImageRight.setOnClickListener(this);
            //long click listeners
            //text
            textLeftSide.setLongClickable(true);
            textRightSide.setLongClickable(true);
            textLeftSide.setOnLongClickListener(this);
            textRightSide.setOnLongClickListener(this);
            //map type
            sendMapLeft.setLongClickable(true);
            sendMapRight.setLongClickable(true);
            sendMapLeft.setOnLongClickListener(this);
            sendMapRight.setOnLongClickListener(this);
            //image/doc type
            sendImageLeft.setLongClickable(true);
            sendImageRight.setLongClickable(true);
            sendImageLeft.setOnLongClickListener(this);
            sendImageRight.setOnLongClickListener(this);
        }


        /**
         * we'll pass as param position and view Id in order to be able to handle all event click listener
         * in ChatroomFragment
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                int position = getAdapterPosition();
                int viewID = v.getId();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(position, viewID); //here we pass both params
                    Log.d(TAG, "onClick: contactID ");
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (longClickListener != null) {
                int position = getAdapterPosition();
                int viewID = v.getId();
                if (position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(position, viewID);
                    return true;
                }
            }
            return false;
        }
    }


    //this interface will make possible to handle single click event from ChatRoomFragment
    public interface OnClickListener {
        void onItemClick(int position, int viewID);
    }

    //with this one we can control long click events in ChatRoomFragment
    public interface OnLongClickListener {
        void onItemLongClick(int position, int viewID);
    }


}



