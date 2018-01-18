package com.chitchat.messaging.chitchatmessaging.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.RecyclerViewItemClickListener;

import java.util.ArrayList;

/**
 * User RecyclerView adapter.
 *
 * @author Sarthak Grover
 */

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private ArrayList<User> usersList = new ArrayList<>();

    private RecyclerViewItemClickListener onRecyclerViewItemClickListener;

    private Context mContext;

    public UserRecyclerAdapter(Context context, ArrayList<User> list) {

        this.mContext = context;
        this.usersList = list;
    }

    public void setOnRecyclerViewItemClickListener(RecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_users, parent, false);

        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, final int position) {

        User user = usersList.get(holder.getAdapterPosition());

        holder.bindData(mContext, user);

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
