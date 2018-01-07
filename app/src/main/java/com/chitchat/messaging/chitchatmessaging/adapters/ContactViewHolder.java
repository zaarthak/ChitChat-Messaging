package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.activities.AccountSettingsActivity;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactViewHolder extends RecyclerView.ViewHolder {

    public TextView mName, mContent;
    public CircleImageView mProfileImage;

    public ContactViewHolder(View itemView) {

        super(itemView);

        mName = itemView.findViewById(R.id.users_name);
        mContent = itemView.findViewById(R.id.users_status);
        mProfileImage = itemView.findViewById(R.id.user_image);
    }

    /**
     * Bind data from String to respective position in RecyclerView
     *
     * @param user is the user details list to set in every row
     */
    void bindData(final Context context, final User user, Message message) {

        mName.setText(user.username);
        mContent.setText(message.content);

        if (!user.thumb_image.equals("default")) {

            Picasso.with(context)
                    .load(user.thumb_image)
                    .placeholder(R.drawable.default_profile_picture)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            // do nothing
                        }

                        @Override
                        public void onError() {

                            Picasso.with(context)
                                    .load(user.thumb_image)
                                    .placeholder(R.drawable.default_profile_picture)
                                    .into(mProfileImage);
                        }
                    });
        }
    }
}
