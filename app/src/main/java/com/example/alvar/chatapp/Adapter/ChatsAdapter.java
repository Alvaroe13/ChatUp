package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends ListAdapter<User,ChatsAdapter.ChatsViewHolder> {

     private static final String TAG = "ChatsAdapter";
     //firebase
     private  DatabaseReference dbUsersNodeRef, dbChatsNodeRef;
     //ui
     private Context context; 
     private String currentUserID;
     private OnClickListener clickListener;
     private OnLongClick onLongClickListener;

    public ChatsAdapter(Context context, OnClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.clickListener = clickListener;
    } 

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chats_individual_layout, parent, false);

        initFirebase();

        return new ChatsAdapter.ChatsViewHolder(view, clickListener, onLongClickListener);
    }


    @Override
    public void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position) {

        final User user = getItem(position);

        setUI(user, holder);
        fetchInfoDB(user.getUserID(), holder);
    }


    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            Log.d(TAG, "areItemsTheSame: called");
            return oldItem.getUserID() == newItem.getUserID();
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUserID().equals(newItem.getUserID()) && oldItem.getImage().equals(newItem.getImage()) &&
                    oldItem.getImageThumbnail().equals(newItem.getImageThumbnail());
        }
    };

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
                if (dataSnapshot.exists()){
                    final String typingState = dataSnapshot.child("userState").child("typing").getValue().toString();

                    typingState(typingState, contactID , holder );
                    otherUserState(dataSnapshot, holder);
                }else{
                    Log.d(TAG, "This is null now!!! ");
                }



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
                Log.i(TAG, "typingState: onDataChange: error: " + e.getMessage() );
            }
        }   else {
            //this method show last message in the fragment list with conversations started
            try {
                showLastMessage(currentUserID, contactID, holder);

                //holder.lastMessage, holder.lastMessageDateField, holder.smallIcon

                holder.lastMessageField.setTextColor(context.getResources().getColor(R.color.color_grey));
            }
            catch (Exception ex){
                Log.i(TAG, "typingState1: onDataChange: error: " + ex.getMessage());
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

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

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
                            e.printStackTrace();
                            Log.d(TAG, "onDataChange: lastMessage called ERROR");
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

    public void onLongClickHandler( OnLongClick listener){
        onLongClickListener = listener;
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        CardView chatLayout;
        CircleImageView chatImageContact, onlineIcon;
        TextView username, lastMessageField, lastMessageDateField;
        ImageButton smallIcon;
        OnClickListener clickListener;
        OnLongClick onLongClickListener;


        public ChatsViewHolder(@NonNull View layout,  final OnClickListener clickListener, final OnLongClick onLongClickListener) {
            super(layout);
            chatLayout = layout.findViewById(R.id.cardViewAllUsers);
            chatImageContact = layout.findViewById(R.id.imageChat);
            username = layout.findViewById(R.id.usernameChat);
            lastMessageField = layout.findViewById(R.id.lastMessage);
            lastMessageDateField = layout.findViewById(R.id.lastMessageDate);
            onlineIcon = layout.findViewById(R.id.onlineIcon);
            smallIcon = layout.findViewById(R.id.smallIcon);

            this.clickListener = clickListener;
            this.onLongClickListener = onLongClickListener;

            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        }

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

        @Override
        public boolean onLongClick(View v) {
            if (onLongClickListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    onLongClickListener.onLongItemClick(position, v); //onItemClick is coming from within the interface
                    Log.d(TAG, "onLongClick: called");
                }
            }
            return false;
        }
    }


    public interface OnClickListener{
        void onItemClick(int position);
    }

    public interface OnLongClick{
        void onLongItemClick(int position, View view);
    }



}
