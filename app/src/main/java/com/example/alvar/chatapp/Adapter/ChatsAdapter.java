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
import com.example.alvar.chatapp.Model.User;
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

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {

    private static final String TAG = "ChatsAdapter";

     Context context;
     List<User> userList;



    public ChatsAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }



    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chats_individual_layout, parent, false);



        return new ChatsAdapter.ChatsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position) {

        final User user = userList.get(position);

        holder.username.setText(user.getName());

        //GLIDE
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(user.getImageThumbnail())
                .into(holder.chatImageContact);

        holder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatRoom(user.getUserID(), user.getName(), user.getImageThumbnail(), holder);
            }
        });

        typingState(user.getUserID(), holder);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void typingState(final String contactID, final ChatsViewHolder holder) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbUsersNodeRef = database.getReference().child("Users");

        dbUsersNodeRef.child(contactID)
                                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String typingState = dataSnapshot.child("userState").child("typing").getValue().toString();

                typingStatus(typingState, contactID , holder );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void typingStatus(String typingState, String contactID, ChatsViewHolder holder) {

        if (typingState.equals("yes")) {
            try {
                holder.smallIcon.setVisibility(View.GONE);
                holder.lastMessage.setText(R.string.typing);
                holder.lastMessage.setTextColor(context.getResources().getColor(R.color.color_green));
            }
            catch (Exception e){
                Log.i(TAG, "onDataChange: error: " + e.getMessage() );
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
        TextView username, lastMessage, lastMessageDateField;
        ImageButton smallIcon;


        public ChatsViewHolder(@NonNull View layout) {
            super(layout);
            chatLayout = layout.findViewById(R.id.cardViewAllUsers);
            chatImageContact = layout.findViewById(R.id.imageChat);
            username = layout.findViewById(R.id.usernameChat);
            lastMessage = layout.findViewById(R.id.lastMessage);
            lastMessageDateField = layout.findViewById(R.id.lastMessageDate);
            onlineIcon = layout.findViewById(R.id.onlineIcon);
            smallIcon = layout.findViewById(R.id.smallIcon);
        }

    }


}
