package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Activities.ChatActivity;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {

    private static final String TAG = "ChatsAdapter";
    //firebase

    private  DatabaseReference dbUsersNodeRef, dbChatsNodeRef;

     Context context;
     List<User> userList;
     private String currentUserID;



    public ChatsAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }



    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chats_individual_layout, parent, false);

        initFirebase();

        return new ChatsAdapter.ChatsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position) {

        final User user = userList.get(position);

        setUI(user, holder);
        fetchInfoDB(user.getUserID(), holder);

        holder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatRoom(user.getUserID(), user.getName(), user.getImageThumbnail(), holder);
            }
        });



    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * we init firebase services.
     */
    private void initFirebase() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (currentUser != null){
            currentUserID = currentUser.getUid();
        }
        //nodes
        dbChatsNodeRef = database.getReference().child("Chats").child("Messages");
        dbUsersNodeRef = database.getReference().child("Users");
    }

    /**
     * set info from db into layout
     * @param user
     * @param holder
     */
    private void setUI(final User user, final ChatsViewHolder holder) {

        holder.username.setText(user.getName());
        //GLIDE
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(user.getImageThumbnail())
                .into(holder.chatImageContact);
    }

    /**
     * fetch info from db
     * @param contactID
     * @param holder
     */
    private void fetchInfoDB(final String contactID, final ChatsViewHolder holder) {


        dbUsersNodeRef.child(contactID)
                                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String typingState = dataSnapshot.child("userState").child("typing").getValue().toString();

                typingState(typingState, contactID , holder );
                otherUserState(dataSnapshot, holder);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * update in real time contact's typing state
     * @param contactID
     * @param holder
     */
    private void typingState(String typingState, String contactID, ChatsViewHolder holder) {

        if (typingState.equals("yes")) {
            try {
                holder.smallIcon.setVisibility(View.GONE);
                holder.lastMessageField.setText(R.string.typing);
                holder.lastMessageField.setTextColor(context.getResources().getColor(R.color.color_green));
            }
            catch (Exception e){
                Log.i(TAG, "onDataChange: error: " + e.getMessage() );
            }
        }   else {
            //this method show last message in the fragment list with conversations started
            try {
                showLastMessage(currentUserID, contactID, holder);

                //holder.lastMessage, holder.lastMessageDateField, holder.smallIcon

                holder.lastMessageField.setTextColor(context.getResources().getColor(R.color.color_grey));
            }
            catch (Exception ex){
                Log.i(TAG, "onDataChange: error: " + ex.getMessage());
            }
        }


    }

    /**
     * This method is the one in charge of showing the last message in a conversation
     *
     * @param currentUserID
     * @param otherUserID
     */
    private void showLastMessage(final String currentUserID, final String otherUserID, final  ChatsViewHolder holder) {

        //retrieve info from the db node "Chats" / "Messages"
        dbChatsNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (dataSnapshot.exists()) {

                        //we bind the Message node in firebase database with the JAVA model "Messages"
                        Messages message = snapshot.getValue(Messages.class);


                        try{
                            if (message.getSenderID().equals(currentUserID) && message.getReceiverID().equals(otherUserID) ||
                                    message.getSenderID().equals(otherUserID) && message.getReceiverID().equals(currentUserID)) {

                                holder.smallIcon.setVisibility(View.GONE);

                                switch (message.getType()) {
                                    case "image":
                                        holder.smallIcon.setVisibility(View.VISIBLE);
                                        holder.lastMessageField.setText(R.string.photo);
                                        break;
                                    case "pdf":
                                        holder.smallIcon.setVisibility(View.VISIBLE);
                                        holder.smallIcon.setBackgroundResource(R.drawable.file_icon);
                                        holder.lastMessageField.setText(R.string.PDF);
                                        break;
                                    case "docx":
                                        holder.smallIcon.setVisibility(View.VISIBLE);
                                        holder.smallIcon.setBackgroundResource(R.drawable.file_icon);
                                        holder.lastMessageField.setText(R.string.Word_Document);
                                        break;
                                    default: //"text" is the one by default
                                        String lastMessage = message.getMessage();
                                        holder.lastMessageField.setText(lastMessage);

                                        String lastMessageDate = message.getMessageDate();
                                        holder.lastMessageDateField.setText(lastMessageDate);
                                }


                            }
                        }catch (Exception e){
                            Log.e(TAG, "onDataChange: error" + e.getMessage());
                        }



                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    /**
     * method in charge of showing green dot if user is online
     * @param dataSnapshot
     * @param holder
     */
    private void otherUserState(DataSnapshot dataSnapshot, ChatsViewHolder holder) {
        //here we show the current state of the other user
        if (dataSnapshot.child("userState").hasChild("state")) {

            //here we get the other user's current state and we store it in each var
            String saveSate = dataSnapshot.child("userState").child("state").getValue().toString();

            //if other user's state is "offline"
            if (saveSate.equals("Offline")) {
                //we show last message
                holder.onlineIcon.setVisibility(View.GONE);
            } else if (saveSate.equals("Online")) {
                holder.onlineIcon.setVisibility(View.VISIBLE);

            }

        }
    }


    /**
     * method in charge of taking the user to the chat room sending the info specified
     *
     * @param otherUserID
     * @param name
     * @param image
     */
    private void goToChatRoom(String otherUserID, String name, String image,ChatsViewHolder holder ) {
        Intent intentChatRoom = new Intent(context, ChatActivity.class);
        intentChatRoom.putExtra("contactID", otherUserID);
        intentChatRoom.putExtra("contactName", name);
        intentChatRoom.putExtra("contactImage", image);
        holder.itemView.getContext().startActivity(intentChatRoom);
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder{

        CardView chatLayout;
        CircleImageView chatImageContact, onlineIcon;
        TextView username, lastMessageField, lastMessageDateField;
        ImageButton smallIcon;


        public ChatsViewHolder(@NonNull View layout) {
            super(layout);
            chatLayout = layout.findViewById(R.id.cardViewAllUsers);
            chatImageContact = layout.findViewById(R.id.imageChat);
            username = layout.findViewById(R.id.usernameChat);
            lastMessageField = layout.findViewById(R.id.lastMessage);
            lastMessageDateField = layout.findViewById(R.id.lastMessageDate);
            onlineIcon = layout.findViewById(R.id.onlineIcon);
            smallIcon = layout.findViewById(R.id.smallIcon);
        }

    }


}
