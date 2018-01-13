package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ImageViewHolder extends RecyclerView.ViewHolder {

    private ImageView mImageView;
    private TextView mTimestampView;

    public ImageViewHolder(View itemView) {
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
    }
}
