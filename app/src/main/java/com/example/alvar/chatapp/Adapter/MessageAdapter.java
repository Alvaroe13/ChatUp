package com.example.alvar.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Activities.ImageActivity;
import com.example.alvar.chatapp.Fragments.LocationFragment;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Model.UserLocation;
import com.example.alvar.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.alvar.chatapp.Constant.LOCATION_CONTACT_LAT;
import static com.example.alvar.chatapp.Constant.LOCATION_CONTACT_LON;
import static com.example.alvar.chatapp.Constant.LOCATION_USER_LAT;
import static com.example.alvar.chatapp.Constant.LOCATION_USER_LON;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapterPage";

    // firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef , dbChatsNodeRef;
    //Firestore
    private FirebaseFirestore mDb;
    // List to contain the messages
    private List<Messages> messagesList;
    private String currentUserID;
    private Context mContext;
    private String contactID;

    public MessageAdapter(Context mContext, List<Messages> messagesList) {
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
        initFirestore();
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
        Log.d(TAG, "onBindViewHolder: currentUserID= " + currentUserID);
        // here we retrieve all messages in chat room and stored into a messageList type of var.
        Messages messages = messagesList.get(position);

        String messageSenderID = messages.getSenderID();
        String messageReceiverID = messages.getReceiverID();
        String messageType = messages.getType();
        String messageInfo = messages.getMessage();
        String messageTime = messages.getMessageTime();

        if ( !messageSenderID.equals(currentUserID)){
            contactID = messageSenderID;
            Log.d(TAG, "onBindViewHolder: contactId= " + contactID);
        } else if (!messageReceiverID.equals(currentUserID)){
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
     * @return
     */
    @Override
    public int getItemCount() {
        //get the size of the List
        return messagesList.size();
    }


    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbChatsNodeRef = database.getReference().child("Chats").child("Messages");
    }


    /**
     * here we fetch info from db and fill the fields with it
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
                    //if user has not uploaded a pic from  device it means within the db it's values is "imgThumbnail" as default
                    if (imageThumbnail.equals("imgThumbnail")) {
                        messageViewHolder.imageContact.setImageResource(R.drawable.profile_image);
                    } else {
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
    private void layoutToShow(String messageType, String messageSenderID, String messageInfo,
                                String messageTime, MessageViewHolder messageViewHolder, int position) {

        // they're all gone by default
        layoutVisibilityGone(messageViewHolder);

        //lets show the message depending on the type of message
        switch (messageType) {
            case "text":
                //we show layout accordingly
                showTextLayout(messageSenderID, messageInfo, messageTime, messageViewHolder, position);
                break;
            case "image":
                //we show layout accordingly
                showImageLayout(messageSenderID, messageInfo, messageViewHolder, messageType, position);
                break;
            case "map":
                showMapLayout(messageSenderID, messageInfo, messageViewHolder, position);
                break;
            default: //if message type is either pdf or docx.
                showDocumentLayout(messageSenderID, messageViewHolder, messageType, position);
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
     * @param messageSenderID
     * @param messageInfo
     * @param messageTime
     * @param messageViewHolder
     */
    private void showTextLayout(String messageSenderID, String messageInfo, String messageTime,
                                MessageViewHolder messageViewHolder, final int position) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the message)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.textRightSide.setVisibility(View.VISIBLE);
            messageViewHolder.textRightSide.setBackgroundResource(R.drawable.right_message_layout);
            messageViewHolder.textRightSide.setText(messageInfo + "  " + messageTime);
            messageViewHolder.textRightSide.setTextSize(15);
            //if long pressed over layout
            messageViewHolder.textRightSide.setLongClickable(true);
            messageViewHolder.textRightSide.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsRightSide(position);
                    Log.i(TAG, "onLongClick: long pressed layout");
                    return true;
                }
            });

        }
        //if the other user is the one sending the message
        else {
            messageViewHolder.textLeftSide.setVisibility(View.VISIBLE);
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.textLeftSide.setBackgroundResource(R.drawable.left_message_layout);
            messageViewHolder.textLeftSide.setText(messageInfo + "  " + messageTime);
            messageViewHolder.textLeftSide.setTextSize(15);
            //if long pressed over layout
            messageViewHolder.textLeftSide.setLongClickable(true);
            messageViewHolder.textLeftSide.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsLeftSide(position);
                    Log.i(TAG, "onLongClick: long pressed left side");
                    return true;
                }
            });
        }

    }

    /**
     * method in charge of showing layout only when it comes to a message "image" type.
     * @param messageSenderID
     * @param messageInfo
     * @param messageViewHolder
     */
    private void showImageLayout(String messageSenderID, String messageInfo,
                                       final MessageViewHolder messageViewHolder,  final String messageType, final int position) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the image)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendImageRight.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(messageInfo).into(messageViewHolder.sendImageRight);

            //if user clicks on the file it opens
            messageViewHolder.sendImageRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile(messageViewHolder, messageType, position);
                    Log.i(TAG, "onClick: short pressed right side");
                }
            });
            //if long pressed over layout
            messageViewHolder.sendImageRight.setLongClickable(true);
            messageViewHolder.sendImageRight.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsRightSide(position);
                    Log.i(TAG, "onLongClick: long pressed right side");
                    return true;
                }
            });

        }
        //if the other user is the one sending the image
        else {
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.sendImageLeft.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(messageInfo).into(messageViewHolder.sendImageLeft);

            //if user clicks on the file it opens
            messageViewHolder.sendImageLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile(messageViewHolder, messageType, position);
                    Log.i(TAG, "onClick: short pressed left side");
                }
            });
            //if long pressed over layout
            messageViewHolder.sendImageLeft.setLongClickable(true);
            messageViewHolder.sendImageLeft.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsLeftSide(position);
                    Log.i(TAG, "onLongClick: long pressed left side");
                    return true;
                }
            });
        }
    }


    /**
     * method in charge of showing file (pdf/docx) when sent by any user
     * @param messageSenderID
     * @param messageViewHolder
     * @param position
     */
    private void showDocumentLayout(String messageSenderID, final MessageViewHolder messageViewHolder,
                                                            final String messageType, final int position) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the file)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendImageRight.setVisibility(View.VISIBLE);
            //let's make sure every time we sent a file Glide retrieves the Doc image template
            // to avoid being one file replaced when other is send after
            Glide.with(mContext.getApplicationContext())
                    .load("https://firebasestorage.googleapis.com/v0/b/chatapp-4adb2.appspot.com/o/file.png?alt=media&token=dc689859-fb7b-4cbf-8c9d-10304329629e")
                    .into(messageViewHolder.sendImageRight);

            //if user clicks on the file it opens
            messageViewHolder.sendImageRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile(messageViewHolder, messageType, position);
                    Log.i(TAG, "onClick: short pressed right side");
                }
            });
            //if long pressed over layout
            messageViewHolder.sendImageRight.setLongClickable(true);
            messageViewHolder.sendImageRight.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsRightSide(position);
                    Log.i(TAG, "onLongClick: long pressed right side");
                    return true;
                }
            });

        }
        //if the other user is the one sending the file
        else {
            messageViewHolder.sendImageLeft.setVisibility(View.VISIBLE);
            //let's make sure every time we sent a file Glide retrieves the Doc image template
            // to avoid being one file replaced when other is send after
            Glide.with(mContext.getApplicationContext())
                    .load("https://firebasestorage.googleapis.com/v0/b/chatapp-4adb2.appspot.com/o/file.png?alt=media&token=dc689859-fb7b-4cbf-8c9d-10304329629e")
                    .into(messageViewHolder.sendImageLeft);
            //if user clicks on the file it opens
            messageViewHolder.sendImageLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile(messageViewHolder, messageType, position);
                    Log.i(TAG, "onClick: short pressed left side");
                }
            });
            //if long pressed over layout
            messageViewHolder.sendImageLeft.setLongClickable(true);
            messageViewHolder.sendImageLeft.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsLeftSide(position);
                    Log.i(TAG, "onLongClick: long pressed left side");
                    return true;
                }
            });


        }
    }

    /**
     *  method shows pop up window with options to delete messages sent by current user
     */
    private void longPressedOptionsRightSide(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.Delete);

        CharSequence deleteOptions[] = new CharSequence[]{ mContext.getString(R.string.Delete_for_me), mContext.getString(R.string.Delete_for_everyone), mContext.getString(R.string.cancel)};

        builder.setItems(deleteOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int options) {

                switch (options) {
                    case 0:
                        deleteRightSideMessage(position);
                        Log.i(TAG, "onClick: delete for me option pressed");
                        break;
                    case 1:
                        deleteMessageForEveryone(position);
                        Log.i(TAG, "onClick: delete for everyone option pressed");
                        break;
                    default:
                        Log.i(TAG, "onClick: cancel option pressed");
                }
            }
        });

        builder.show();
    }

    /**
     *  method shows pop up window with options to delete message sent by the other user
     */
    private void longPressedOptionsLeftSide(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.Delete);

        CharSequence deleteOptions[] = new CharSequence[]{ mContext.getString(R.string.Delete_for_me) , mContext.getString(R.string.cancel)};

        builder.setItems(deleteOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int options) {

                switch (options) {
                    case 0:
                        deleteLeftSideMessage(position);
                        Log.i(TAG, "onClick: delete for me option pressed");
                        break;
                    default:
                        Log.i(TAG, "onClick: cancel option pressed");
                }
            }
        });

        builder.show();
    }

    /**
     * This method deletes message sent by current user
     * @param position
     */
    private void deleteRightSideMessage(int position) {

        String senderID = messagesList.get(position).getSenderID();
        String receiverID = messagesList.get(position).getReceiverID();
        String messageID = messagesList.get(position).getMessageID();

        dbChatsNodeRef.child(senderID).child(receiverID).child(messageID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Log.i(TAG, "onComplete:message deleted right side");
                }
                 else{
                    Log.i(TAG, "onComplete:something failed");
                }
            }
        });
    }

    /**
     * method deletes messages sent by other user
     * @param position
     */
    private void deleteLeftSideMessage(int position) {

        String senderID = messagesList.get(position).getSenderID();
        String receiverID = messagesList.get(position).getReceiverID();
        String messageID = messagesList.get(position).getMessageID();

        dbChatsNodeRef.child(receiverID).child(senderID).child(messageID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Log.i(TAG, "onComplete: message deleted left side ");
                }
                else{
                    Log.i(TAG, "onComplete: Error, something failed");
                }
            }
        });
    }

    /**
     * method in charge of deleting message for both sender and receiver.
     * @param position
     */
    private void deleteMessageForEveryone(int position){

        final String senderID = messagesList.get(position).getSenderID();
        final String receiverID = messagesList.get(position).getReceiverID();
        final String messageID = messagesList.get(position).getMessageID();

        //lets first of all erase from the current user side
        dbChatsNodeRef.child(senderID).child(receiverID).child(messageID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    //lets first of all erase from the other user side
                    dbChatsNodeRef.child(receiverID).child(senderID).child(messageID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Log.i(TAG, "onComplete: message deleted for everyone ");
                            }
                            else{
                                Log.i(TAG, "onComplete: Error, something failed ");
                            }
                        }
                    });

                } else{
                    Log.i(TAG, "onComplete: Error, something failed ");
                }
            }
        });

    }

    /**
     * method in charge of launching file when clicked by user
     * @param messageViewHolder
     * @param position
     */
    private void openFile(final MessageViewHolder messageViewHolder, final String messageType, final int position) {

        //here we store the "file" or "image" info to be fetched later on
        final String message = messagesList.get(position).getMessage();

        if (messageType.equals("image")) {
            showImageRoom(message, messageViewHolder);
        }
        //if it's a "pdf" or "docx" we show option to download file.
        else {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(message));
            messageViewHolder.itemView.getContext().startActivity(i);
        }

    }


    /**
     * methos in charge of taking the user to the Big image room when image message is pressed
     * @param messageContent
     * @param messageViewHolder
     */
    private void showImageRoom(String messageContent, MessageViewHolder messageViewHolder) {
        Intent intentImage = new Intent(mContext, ImageActivity.class);
        intentImage.putExtra("messageContent", messageContent);
        messageViewHolder.itemView.getContext().startActivity(intentImage);

    }


    // -------------------------------------------- maps features -------------------------------

    private void showMapLayout(String messageSenderID, String messageInfo, MessageViewHolder messageViewHolder , final int position) {

        //if the current user ID matches with the user id saved in "senderByID" (it means that we are the one sending the message)
        if (currentUserID.equals(messageSenderID)) {
            messageViewHolder.sendMapRight.setVisibility(View.VISIBLE);
            messageViewHolder.sendMapRight.setClickable(true);
            messageViewHolder.sendMapRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: map right side clicked");
                    retrieveUsersLocationFromDB( v, contactID );

                }
            });
            //if long pressed over layout
            messageViewHolder.sendMapRight.setLongClickable(true);
            messageViewHolder.sendMapRight.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsRightSide(position);
                    Log.i(TAG, "onLongClick: long pressed layout");
                    return true;
                }
            });

        }
        //if the other user is the one sending the message
        else {
            messageViewHolder.sendMapLeft.setVisibility(View.VISIBLE);
            messageViewHolder.imageContact.setVisibility(View.VISIBLE);
            messageViewHolder.sendMapLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: map right side clicked");
                    deployAlertDialog(v);
                }
            });
            //if long pressed over layout
            messageViewHolder.sendMapLeft.setLongClickable(true);
            messageViewHolder.sendMapLeft.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressedOptionsLeftSide(position);
                    Log.i(TAG, "onLongClick: long pressed left side");
                    return true;
                }
            });
        }

    }

    private void deployAlertDialog(final View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getString(R.string.open_location));
            builder.setIcon(R.drawable.ic_location);
            //options to be shown in the Alert Dialog
           builder.setMessage(mContext.getString(R.string.open_location_message));
           builder.setNegativeButton(mContext.getString(R.string.no), null);
           builder.setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   retrieveUsersLocationFromDB( v , contactID );
               }
           });
            builder.show();
    }

    private void initFirestore() {
        //db
        mDb = FirebaseFirestore.getInstance();
    }

    /**
     * method will be the one fetching users location fro the DB
     * @param contactID
     */
    private void retrieveUsersLocationFromDB(final View v,final String contactID) {


        final DocumentReference locationRefUser1 = mDb.collection(mContext.getString(R.string.collection_user_location))
                .document(currentUserID);

        locationRefUser1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()){

                    final  UserLocation locationUser1 = documentSnapshot.toObject(UserLocation.class);

                    double lat1 = locationUser1.getGeo_point().getLatitude();
                    double lon1 = locationUser1.getGeo_point().getLongitude();

                    Log.d(TAG, "onSuccess: location current user1 (user authenticated): " + lat1 + " , " + lon1 );
                    retrieveOtherUserLocation( v, lat1, lon1 , contactID);
                }
                else {
                    Log.d(TAG, "onSuccess: user1 location is not in db");
                }
            }

        });
    }

    /**
     * this methos retrieves contact's location
     */
    private void retrieveOtherUserLocation( final View v, final double lat1, final double lon1 , final String contactID) {

        DocumentReference locationRefUser2 = mDb.collection(mContext.getString(R.string.collection_user_location))
                .document(contactID);

        locationRefUser2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                    final  UserLocation locationUser2 = documentSnapshot.toObject(UserLocation.class);

                    double lat2 = locationUser2.getGeo_point().getLatitude();
                    double lon2 = locationUser2.getGeo_point().getLongitude();
                    Log.d(TAG, "onSuccess: location current user2 (contact user in chat room): " + lat2 + " , " + lon2 );


                    inflateLocationFragment( v, lat1, lon1, lat2, lon2 );

                } else {
                    Log.d(TAG, "onSuccess: user2 location is not in db");
                    Toast.makeText(mContext, " Your contact " +
                            "has not provided current location", Toast.LENGTH_SHORT).show();
                    //chatProgresBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void inflateLocationFragment(View v, double lat1, double lon1 , double lat2, double lon2 ) {
        // double lat1, double lon1 , double lat2, double lon2

        Log.d(TAG, "inflateLocationFragment: called");

        LocationFragment fragment =  LocationFragment.newInstance();
        Bundle data = new Bundle();
        data.putDouble(LOCATION_USER_LAT, lat1);
        data.putDouble(LOCATION_USER_LON, lon1 );
        data.putDouble(LOCATION_CONTACT_LAT, lat2 );
        data.putDouble(LOCATION_CONTACT_LON, lon2 );
        data.putString("contactID", contactID);
        fragment.setArguments(data);

        AppCompatActivity activity = (AppCompatActivity) v.getContext();

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down);
        transaction.replace(R.id.layoutFrameID, fragment);
        transaction.addToBackStack(null);
        transaction.commit();


    }


    /**
     * This is the viewHolder Class. The one in charge of finding the UI elements within each
     * item shown in the recyclerView
     */
    public class MessageViewHolder extends RecyclerView.ViewHolder {

        //UI elements
        TextView textRightSide, textLeftSide;
        CircleImageView imageContact;
        ImageView sendImageLeft, sendImageRight, sendMapLeft, sendMapRight;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            textLeftSide = itemView.findViewById(R.id.textLeft);
            textRightSide = itemView.findViewById(R.id.textRight);
            imageContact = itemView.findViewById(R.id.imageChat);
            sendImageLeft = itemView.findViewById(R.id.imageLeft);
            sendImageRight = itemView.findViewById(R.id.imageRight);
            sendMapLeft = itemView.findViewById(R.id.mapLeft);
            sendMapRight = itemView.findViewById(R.id.mapRight);

        }


    }


}
