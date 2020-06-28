package com.example.alvar.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.activities.OtherUserProfileActivity;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private Context context;
    private List<User> userList;

    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(context).inflate(R.layout.users_individual_layout, parent, false);
        return new UsersAdapter.UsersViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersViewHolder holder, int position) {

        final User user = userList.get(position);
        setInfoIntoUI( holder, user);

        //userID authenticated "currentUserID"
        //other userID in recyclerView "user.getUserID()"

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToOtherUserLayout(user.getUserID(), holder);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
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
        //here we set the default image is user has not upload any pic
        if (user.getImageThumbnail().equals("imgThumbnail")){
            holder.userPhoto.setImageResource(R.drawable.profile_image);
        }else{
            //lets upload images from db to ui using glide instead of picasso
            Glide.with(context).load(user.getImageThumbnail()).into(holder.userPhoto);
        }
    }


    /**
     * take the user to the chat activity send the other userID with it
     * @param otherUserId
     * @param holder
     */
    private void goToOtherUserLayout(String otherUserId, UsersViewHolder holder) {
        Intent intentOtherUserProf = new Intent(context, OtherUserProfileActivity.class);
        //we send user id through an intent
        intentOtherUserProf.putExtra("otherUserId", otherUserId);
        holder.itemView.getContext().startActivity(intentOtherUserProf);

    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        public CardView cardView;
        public CircleImageView userPhoto;
        public TextView username, userStatus;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardViewAllUsers);
            userPhoto = itemView.findViewById(R.id.imageAllUsers);
            username =  itemView.findViewById(R.id.usernameAllUsers);
            userStatus = itemView.findViewById(R.id.statusAllUsers);

        }
    }
}
