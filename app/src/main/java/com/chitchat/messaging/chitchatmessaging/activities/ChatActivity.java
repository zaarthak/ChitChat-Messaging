package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.adapters.ChatRecyclerAdapter;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<Message> messageList = new ArrayList<>();

    private EditText mInputMessage;
    private ImageButton mSendBtn;

    private Toolbar mToolbar;

    private RecyclerView mMessageList;
    private ChatRecyclerAdapter adapter;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpView();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        readMessages();

        adapter = new ChatRecyclerAdapter(ChatActivity.this, messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        mMessageList.setAdapter(adapter);

        //create recycler adapter

        mSendBtn.setOnClickListener(this);
    }

    private void setUpView() {

        mToolbar = findViewById(R.id.chat_toolbar);

        if (mToolbar != null) {

            setSupportActionBar(mToolbar);

            // set activity title
            // handle NullPointerException
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getIntent().getStringExtra("user_name"));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mToolbar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("user_id", getIntent().getStringExtra("user_id"));
                    startActivity(profileIntent);
                }
            });
        }

        mInputMessage = findViewById(R.id.chat_input_message);
        mSendBtn = findViewById(R.id.chat_send_btn);

        mMessageList = findViewById(R.id.chat_messages_list);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.chat_send_btn :

                sendMessage();
                break;
        }
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

    private void readMessages() {

        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String friendUser = getIntent().getStringExtra("user_id");

        mDatabase.child("Messages").child(currentUser).child(friendUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                messageList.clear();

                if (dataSnapshot.exists()) {

                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        if (!child.getKey().equals("lastMessage")) {

                            messageList.add(child.getValue(Message.class));
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

    private void sendMessage() {

        String inputMessage = mInputMessage.getText().toString().trim();

        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String friendUser = getIntent().getStringExtra("user_id");

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final String timeStamp = String.valueOf(ts.getTime());

        if (!TextUtils.isEmpty(inputMessage))  {

            final Message sentMessage = new Message(inputMessage, "sent", timeStamp, "read");
            final Message receivedMessage = new Message(inputMessage, "received", timeStamp, "unread");

            mDatabase.child("Chats").child(currentUser).child(friendUser).setValue(sentMessage);
            mDatabase.child("Chats").child(friendUser).child(currentUser).setValue(receivedMessage);

            mDatabase.child("Messages").child(currentUser).child(friendUser).child(timeStamp).setValue(sentMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    mDatabase.child("Messages").child(friendUser).child(currentUser).child(timeStamp).setValue(receivedMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //handle event
                        }
                    });
                }
            });

            mInputMessage.setText("");
        }
    }
}
