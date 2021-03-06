package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.adapters.UserRecyclerAdapter;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.chitchat.messaging.chitchatmessaging.utils.RecyclerViewItemClickListener;
import com.chitchat.messaging.chitchatmessaging.utils.SimpleDividerItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity implements RecyclerViewItemClickListener {

    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayList<String> userKeyList = new ArrayList<>();

    UserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // fetch all users from firebase database
        fetchUserFromDatabase();

        RecyclerView mUserList = findViewById(R.id.user_list);
        adapter = new UserRecyclerAdapter(UserActivity.this, usersList);
        adapter.setOnRecyclerViewItemClickListener(this);

        mUserList.setLayoutManager(new LinearLayoutManager(this));
        mUserList.addItemDecoration(new SimpleDividerItemDecoration(this));
        mUserList.setAdapter(adapter);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //----------------------------------------------------------------------------------------------
    // recyclerView onClick listener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view, int position) {

        // launch chat activity of corresponding user
        Intent chatIntent = new Intent(UserActivity.this, ChatActivity.class);
        chatIntent.putExtra(Constants.INTENT_USER_NAME_KEY, usersList.get(position).username);
        chatIntent.putExtra(Constants.INTENT_USER_ID_KEY, userKeyList.get(position));
        startActivity(chatIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Fetch all user details from firebase database
     */
    private void fetchUserFromDatabase() {

        // set up firebase database reference
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        // enable firebase persistence
        mDatabase.keepSynced(true);

        mDatabase.child(Constants.USERS_REFERENCE).orderByChild(Constants.USERNAME_REFERENCE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                usersList.clear();
                userKeyList.clear();

                for (final DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child !=  null) {

                        if (!child.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            // update arraylist with user details
                            usersList.add(child.getValue(User.class));
                            userKeyList.add(child.getKey());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
