package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private static final String TAG = "UsersAdapter";

    private Context context;
    private List<User> userList;
    private OnClickListener clickListener;
    private String contactID;

    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(context).inflate(R.layout.users_individual_layout, parent, false);
        return new UsersAdapter.UsersViewHolder(layout, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersViewHolder holder, int position) {

        final User user = userList.get(position);
        setInfoIntoUI( holder, user);

        //userID authenticated "currentUserID"
        //other userID in recyclerView "user.getUserID()"

        contactID = user.getUserID();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * this method will handle the click event in this adapter (Android best practices)
     * @param listener
     */
    public void clickHandler(OnClickListener listener){
        clickListener = listener;
    }



    /**
     * mehotd retrieves info form db and set it in the UI
     * @param holder
     * @param user
     */
    private void setInfoIntoUI(UsersAdapter.UsersViewHolder holder, User user ) {

        //  here we fetch info from database and set it to the UI
        holder.username.setText(user.getName());
        holder.userStatus.setText(user.getStatus());

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        //lets upload images from db to ui using glide instead of picasso
        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(user
                .getImageThumbnail()).into(holder.userPhoto);

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        public CardView cardView;
        public CircleImageView userPhoto;
        public TextView username, userStatus;



        public UsersViewHolder(@NonNull View itemView, final OnClickListener clickListener) {
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

            cardView = itemView.findViewById(R.id.cardViewAllUsers);
            userPhoto = itemView.findViewById(R.id.imageAllUsers);
            username =  itemView.findViewById(R.id.usernameAllUsers);
            userStatus = itemView.findViewById(R.id.statusAllUsers);

        }
    }

    public interface OnClickListener{
        void onItemClick(int position);
    }


}
