package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * View holder class for ChatList Recycler adapter.
 *
 * Displays the username and last message in each row of RecyclerView.
 */

class ChatListViewHolder extends RecyclerView.ViewHolder {

    private TextView mName, mContent, mTimestamp;
    private CircleImageView mProfileImage;

    ChatListViewHolder(View itemView) {

        super(itemView);

        mName = itemView.findViewById(R.id.contact_name);
        mContent = itemView.findViewById(R.id.contact_status);
        mProfileImage = itemView.findViewById(R.id.contact_image);
        mTimestamp = itemView.findViewById(R.id.contact_time);
    }

    /**
     * Bind data from arrayLists to respective position in RecyclerView.
     *
     * @param user is the user details list to set in every row
     * @param message is the lastMessage details of the user
     */
    void bindData(final Context context, final User user, Message message) {

        long timestamp = Long.parseLong(message.timeStamp);
        String time = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);

        mName.setText(user.username);

        mTimestamp.setText(time);

        if (message.media.equals("text")) {

            mContent.setCompoundDrawables(null, null, null, null);
            mContent.setText(message.content);
        } else {

            Drawable img = context.getResources().getDrawable(R.drawable.textview_camera);
            img.setAlpha(127);
            img.setBounds(0, 0, img.getIntrinsicWidth() * mContent.getMeasuredHeight() / img.getIntrinsicHeight(), mContent.getMeasuredHeight());
            mContent.setCompoundDrawables(img, null, null, null);
            mContent.setText(" Image");
        }

        // picasso offline capabilities used.
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
