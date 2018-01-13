package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;

import java.util.ArrayList;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;

    private ArrayList<Message> messageList = new ArrayList<>();

    private static final int IMAGE_RIGHT_MSG = 0;
    private static final int IMAGE_LEFT_MSG = 1;
    private static final int TEXT_RIGHT_MSG = 2;
    private static final int TEXT_LEFT_MSG = 3;

    private Context mContext;

    public ChatRecyclerAdapter(Context context, ArrayList<Message> list) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.messageList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        View view;

        switch (viewType) {

            case IMAGE_LEFT_MSG:

                view = inflater.inflate(R.layout.cardview_left_image_message, parent, false);
                viewHolder = new ImageViewHolder(view);
                break;

            case IMAGE_RIGHT_MSG:

                view = inflater.inflate(R.layout.cardview_right_image_message, parent, false);
                viewHolder = new ImageViewHolder(view);
                break;

            case TEXT_LEFT_MSG:

                view = inflater.inflate(R.layout.cardview_left_text_message,parent,false);
                viewHolder = new TextViewHolder(view);
                break;

            case TEXT_RIGHT_MSG:

                view = inflater.inflate(R.layout.cardview_right_text_message,parent,false);
                viewHolder = new TextViewHolder(view);
                break;
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList.get(position).media.equals("text")) {

            if (messageList.get(position).type.equals("sent")) {

                return TEXT_RIGHT_MSG;
            } else {

                return TEXT_LEFT_MSG;
            }
        } else {

            if (messageList.get(position).type.equals("sent")) {

                return IMAGE_RIGHT_MSG;
            } else {

                return IMAGE_LEFT_MSG;
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Message message = messageList.get(holder.getAdapterPosition());
        ImageViewHolder imageViewHolder;
        TextViewHolder textViewHolder;

        switch (holder.getItemViewType()) {

            case IMAGE_LEFT_MSG:

                imageViewHolder = (ImageViewHolder) holder;
                imageViewHolder.bindData(mContext, message);
                break;

            case IMAGE_RIGHT_MSG:

                imageViewHolder = (ImageViewHolder) holder;
                imageViewHolder.bindData(mContext, message);
                break;

            case TEXT_LEFT_MSG:

                textViewHolder = (TextViewHolder) holder;
                textViewHolder.bindData(mContext, message);
                break;

            case TEXT_RIGHT_MSG:

                textViewHolder = (TextViewHolder) holder;
                textViewHolder.bindData(mContext, message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
