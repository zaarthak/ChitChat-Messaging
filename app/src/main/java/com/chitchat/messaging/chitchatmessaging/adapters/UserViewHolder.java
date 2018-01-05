package com.chitchat.messaging.chitchatmessaging.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public TextView mName, mId;

    public UserViewHolder(View itemView) {
        super(itemView);

        mName = itemView.findViewById(R.id.users_name);
        mId = itemView.findViewById(R.id.users_status);
    }

    /**
     * Bind data from String to respective position in RecyclerView
     *
     * @param user is the user details list to set in every row
     */
    void bindData(User user) {

        mName.setText(user.username);
        mId.setText(user.email);
    }
}
