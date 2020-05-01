package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Activities.ImageActivity;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapterPage";

    // firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    // List to contain the messages
    private List<Messages> messagesList;
    private String currentUserID;
    private Context mContext;

    public MessageAdapter(Context mContext , List<Messages> messagesList) {
        this.messagesList = messagesList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // we bind the layout with this controller and the sub class "MessageViewHolder"

        View viewChat = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_layout, viewGroup, false);

        //init firebase services as soon as we inflate the view
        initFirebase();
        return new MessageViewHolder(viewChat);
    }

    /**
     * here in this method lies the logic to fill the recyclerView
     * @param messageViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int position) {

        //first of all we get current user id
        currentUserID = auth.getCurrentUser().getUid();

        Messages messages = messagesList.get(position);

        String messageSenderID = messages.getSenderID();
        String messageType = messages.getType();
        String messageInfo = messages.getMessage();
        String messageTime = messages.getMessageTime();
        //we fetch info from db Users node
        infoFetchedFromDb(messageSenderID, messageViewHolder);
        //here's where fun with the layouts starts
        layoutToShow(messageType, messageSenderID, messageInfo, messageTime, messageViewHolder, position);
    }


    private void initFirebase(){
        auth =  FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
    }

    /**
     * here we fetch info from db and fill the fields with it
     * @param messageSenderID
     * @param messageViewHolder
     */
    private void infoFetchedFromDb(String messageSenderID, final MessageViewHolder messageViewHolder ) {

        //here we fetch image from db
        dbUsersNodeRef.child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    //fetch image info from db
                    String imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();
                    //if user has not uploaded a pic from  device it means within the db it's values is "imgThumbnail" as default
                    if (imageThumbnail.equals("imgThumbnail")){
                        messageViewHolder.imageContact.setImageResource(R.drawable.profile_image);
                    }else{
                        //if user has uploaded a pic from device into ChatUp profile settings we retrieve it and show it here
                        Glide.with(mContext.getApplicationContext()).load(imageThumbnail).into(messageViewHolder.imageContact);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    /**
     * this method is in charge of showing the correct layout in chat room according to the situation
     * @param messageType
     * @param messageSenderID
     * @param messageInfo
     * @param messageTime
     * @param messageViewHolder
     */
    private void layoutToShow(String messageType, String messageSenderID, String messageInfo, String messageTime, MessageViewHolder messageViewHolder, int position) {

        // they're all gone by default
        layoutVisibilityGone(messageViewHolder);

       //lets show the message depending on the type of message
        switch (messageType){
            case "text":
                //we show layout accordingly
                showTextLayout(messageSenderID, messageInfo, messageTime, messageViewHolder);
                break;
            case "image":
                //we show layout accordingly
                showImageLayout(messageSenderID, messageInfo, messageViewHolder, messageType, position);
                break;
            default: //if message type is either pdf or docx.
                showDocument(messageSenderID, messageViewHolder, messageType , position);
        }

    }

    /**
     * method in charge of showing file when user clicks on it
     * @param messageSenderID
     * @param messageViewHolder
     * @param position
     */
    private void showDocument(String messageSenderID, MessageViewHolder messageViewHolder, String messageType, int position) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the file)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendImageRight.setVisibility(View.VISIBLE);
            //let's make sure every time we sent a file Glide retrieves the Doc image template
            // to avoid being one file replaced when other is send after
            Glide.with(mContext.getApplicationContext())
                    .load("https://firebasestorage.googleapis.com/v0/b/chatapp-4adb2.appspot.com/o/file.png?alt=media&token=dc689859-fb7b-4cbf-8c9d-10304329629e")
                    .into(messageViewHolder.sendImageRight);
            //if user clicks on the file it opens
            openFile(messageViewHolder, messageType, position);
        }
        //if the other user is the one sending the file
        else {
            messageViewHolder.sendImageLeft.setVisibility(View.VISIBLE);
            //let's make sure every time we sent a file Glide retrieves the Doc image template
            // to avoid being one file replaced when other is send after
            Glide.with(mContext.getApplicationContext())
                    .load("https://firebasestorage.googleapis.com/v0/b/chatapp-4adb2.appspot.com/o/file.png?alt=media&token=dc689859-fb7b-4cbf-8c9d-10304329629e")
                    .into( messageViewHolder.sendImageLeft);
            //if user clicks on the file it opens
            openFile(messageViewHolder, messageType , position);
        }
    }

    /**
     * method in charge of launching file when clicked by user
     * @param messageViewHolder
     * @param position
     */
    private void openFile(final MessageViewHolder messageViewHolder, final String messageType,  final int position ){

        //here we store the "file" or "image" info to be fetched later on
       final String message = messagesList.get(position).getMessage();

        //when message box is pressed
        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( messageType.equals("image") ){
                    Intent intentImage = new Intent(mContext , ImageActivity.class);
                    intentImage.putExtra("messageContent", message );
                    messageViewHolder.itemView.getContext().startActivity(intentImage);
                    showImageRoom(message, messageViewHolder);
                }
                //if it's a "pdf" or "docx" we show option to download file.
                else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse( message ) );
                    messageViewHolder.itemView.getContext().startActivity(i);
                }

            }
        });

    }

    private void showImageRoom( String messageContent, MessageViewHolder messageViewHolder) {
        Intent intentImage = new Intent(mContext , ImageActivity.class);
        intentImage.putExtra("messageContent", messageContent );
        messageViewHolder.itemView.getContext().startActivity(intentImage);

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
    }

    /**
     * method in charge of showing layout only when it comes to a message "text" type.
     * @param messageSenderID
     * @param messageInfo
     * @param messageTime
     * @param messageViewHolder
     */
    private void showTextLayout( String messageSenderID, String messageInfo,  String messageTime, MessageViewHolder messageViewHolder) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the message)
        if (currentUserID.equals(messageSenderID)){
            messageViewHolder.textRightSide.setVisibility(View.VISIBLE);
            messageViewHolder.textRightSide.setBackgroundResource(R.drawable.right_message_layout);
            messageViewHolder.textRightSide.setText(messageInfo + "  " + messageTime );
            messageViewHolder.textRightSide.setTextSize(15);
        }
        //if the other user is the one sending the message
        else{
            messageViewHolder.textLeftSide.setVisibility(View.VISIBLE);
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.textLeftSide.setBackgroundResource(R.drawable.left_message_layout);
            messageViewHolder.textLeftSide.setText(messageInfo + "  " + messageTime);
            messageViewHolder.textLeftSide.setTextSize(15);
        }

    }

    /**
     * method in charge of showing layout only when it comes to a message "image" type.
     * @param messageSenderID
     * @param messageInfo
     * @param messageViewHolder
     */
    private void showImageLayout(String messageSenderID, String messageInfo,  MessageViewHolder messageViewHolder, String messageType, int position ) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the image)
        if (currentUserID.equals(messageSenderID) ){
            messageViewHolder.sendImageRight.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(messageInfo).into(messageViewHolder.sendImageRight);
            //if user clicks on the file it opens
            openFile(messageViewHolder, messageType , position);
        }
        //if the other user is the one sending the image
        else {
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.sendImageLeft.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(messageInfo).into(messageViewHolder.sendImageLeft);
            //if user clicks on the file it opens
            openFile(messageViewHolder, messageType , position);
        }
    }

    /**
     * this method is the one in charge of establishing the number of items to be shown in the recyclerView
     * @return
     */
    @Override
    public int getItemCount() {
        //get the size of the List
        return messagesList.size();
    }

    /**
     * This is the viewHolder Class. The one in charge of finding the UI elements within each
     * item shown in the recyclerView
     */
    public class MessageViewHolder extends RecyclerView.ViewHolder{

        //UI elements
        TextView textRightSide, textLeftSide ;
        CircleImageView imageContact;
        ImageView sendImageLeft, sendImageRight;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            textLeftSide = itemView.findViewById(R.id.textLeft);
            textRightSide = itemView.findViewById(R.id.textRight);
            imageContact = itemView.findViewById(R.id.imageChat);
            sendImageLeft = itemView.findViewById(R.id.imageLeft);
            sendImageRight = itemView.findViewById(R.id.imageRight);

        }


    }



}
