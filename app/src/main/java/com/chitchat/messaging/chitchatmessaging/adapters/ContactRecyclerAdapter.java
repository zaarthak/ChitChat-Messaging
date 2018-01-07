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

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayList<Message> messageList = new ArrayList<>();

    private RecyclerViewItemClickListener onRecyclerViewItemClickListener;

    private Context mContext;

    public ContactRecyclerAdapter(Context context, ArrayList<User> usersList, ArrayList<Message> messageList) {

        this.mContext = context;
        this.usersList = usersList;
        this.messageList = messageList;
    }

    public void setOnRecyclerViewItemClickListener(RecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_users, parent, false);

        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, final int position) {

        User user = usersList.get(holder.getAdapterPosition());
        Message message = messageList.get(holder.getAdapterPosition());

        holder.bindData(mContext, user, message);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onRecyclerViewItemClickListener.onClick(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
}
