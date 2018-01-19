package com.chitchat.messaging.chitchatmessaging.adapters.chatadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;

/**
 * View holder class for Chat Recycler adapter.
 *
 * Displays TEXT message sent/received by the user in each row of RecyclerView.
 */

class TextViewHolder extends RecyclerView.ViewHolder {

    private TextView mMessageView, mTimestampView;

    TextViewHolder(View itemView) {

        super(itemView);

        mMessageView = itemView.findViewById(R.id.msg_text_view);
        mTimestampView = itemView.findViewById(R.id.timestamp_text_view);
    }

    /**
     * Binds data for 'Message' to respective position in RecyclerView.
     *
     * @param message is the message details stored in firebase database
     */
    void bindData(Context context, Message message) {

        long timestamp = Long.parseLong(message.timeStamp);
        String time = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);

        mMessageView.setText(message.content);
        mTimestampView.setText(time);
    }
}
