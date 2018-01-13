package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;

public class TextViewHolder extends RecyclerView.ViewHolder {

    private TextView mMessageView, mTimestampView;

    public TextViewHolder(View itemView) {

        super(itemView);

        mMessageView = itemView.findViewById(R.id.msg_text_view);
        mTimestampView = itemView.findViewById(R.id.timestamp_text_view);
    }

    void bindData(Context context, Message message) {

        long timestamp = Long.parseLong(message.timeStamp);
        String time = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);

        mMessageView.setText(message.content);
        mTimestampView.setText(time);
    }
}
