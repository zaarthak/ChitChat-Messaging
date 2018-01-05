package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.adapters.UserRecyclerAdapter;
import com.chitchat.messaging.chitchatmessaging.utils.RecyclerViewItemClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity implements RecyclerViewItemClickListener {

    private ArrayList<User> usersList = new ArrayList<>();

    private RecyclerView mUserList;
    UserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        fetchUserFromDatabase();

        mUserList = findViewById(R.id.user_list);
        adapter = new UserRecyclerAdapter(UserActivity.this, usersList);
        adapter.setOnRecyclerViewItemClickListener(this);

        mUserList.setLayoutManager(new LinearLayoutManager(this));
        mUserList.setAdapter(adapter);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void fetchUserFromDatabase() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                usersList.clear();

                for (final DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child !=  null) {

                        usersList.add(child.getValue(User.class));
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view, int position) {

        Intent chatIntent = new Intent(UserActivity.this, ChatActivity.class);
        chatIntent.putExtra("user_name", usersList.get(position).username);
        startActivity(chatIntent);
        finish();
    }
}
