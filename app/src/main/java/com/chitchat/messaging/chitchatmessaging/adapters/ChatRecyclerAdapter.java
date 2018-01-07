package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;

import java.util.ArrayList;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private LayoutInflater inflater;

    private ArrayList<Message> messageList = new ArrayList<>();

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;

    private Context mContext;

    public ChatRecyclerAdapter(Context context, ArrayList<Message> list) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.messageList = list;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == RIGHT_MSG){
            view = inflater.inflate(R.layout.cardview_right_message,parent,false);
            return new ChatViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.cardview_left_message,parent,false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList.get(position).type.equals("sent")) {

            return RIGHT_MSG;
        } else {

            return LEFT_MSG;
        }
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {

        Message message = messageList.get(holder.getAdapterPosition());

        holder.bindData(mContext, message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
