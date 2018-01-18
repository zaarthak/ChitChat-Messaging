package com.chitchat.messaging.chitchatmessaging.adapters.chatadapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.activities.ChatImageActivity;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * View holder class for Chat Recycler adapter.
 *
 * Displays IMAGE message sent/received by the user in each row of RecyclerView.
 */

class ImageViewHolder extends RecyclerView.ViewHolder {

    private ImageView mImageView;
    private TextView mTimestampView;

    ImageViewHolder(View itemView) {
        super(itemView);

        mImageView = itemView.findViewById(R.id.message_image);
        mTimestampView = itemView.findViewById(R.id.timestamp_text_view);
    }

    void bindData(final Context context, final Message message) {

        long timestamp = Long.parseLong(message.timeStamp);
        String time = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);

        mTimestampView.setText(time);

        Picasso.with(context)
                .load(message.content)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // do nothing
                    }

                    @Override
                    public void onError() {

                        Picasso.with(context)
                                .load(message.content)
                                .into(mImageView);
                    }
                });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chatImageIntent = new Intent(context, ChatImageActivity.class);
                chatImageIntent.putExtra("chat_image", message.content);
                context.startActivity(chatImageIntent);
            }
        });
    }
}
