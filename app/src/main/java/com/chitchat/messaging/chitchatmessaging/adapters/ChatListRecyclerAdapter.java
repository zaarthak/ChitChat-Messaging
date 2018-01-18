package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.RecyclerViewItemClickListener;

import java.util.ArrayList;

/**
 * ChatList RecyclerView adapter
 *
 * @author Sarthak Grover
 */

public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListViewHolder> {

    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayList<Message> messageList = new ArrayList<>();
    private ArrayList<String> userKeyList = new ArrayList<>();

    private ArrayList<Integer> unreadMessageList = new ArrayList<>();

    private RecyclerViewItemClickListener onRecyclerViewItemClickListener;

    private Context mContext;

    public ChatListRecyclerAdapter(Context context, ArrayList<User> usersList, ArrayList<String> userKeyList, ArrayList<Message> messageList, ArrayList<Integer> unreadMessageList) {

        this.mContext = context;
        this.usersList = usersList;
        this.userKeyList = userKeyList;
        this.messageList = messageList;
        this.unreadMessageList = unreadMessageList;
    }

    public void setOnRecyclerViewItemClickListener(RecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    @Override
    public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_contact, parent, false);

        return new ChatListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ChatListViewHolder holder, int position) {

        User user = usersList.get(holder.getAdapterPosition());
        Message message = messageList.get(holder.getAdapterPosition());

        int readStatus = 0;//unreadMessageList.get(holder.getAdapterPosition());

        holder.bindData(mContext, user, message, readStatus);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onRecyclerViewItemClickListener.onClick(view, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
}
