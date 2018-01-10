package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.adapters.ContactRecyclerAdapter;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.RecyclerViewItemClickListener;
import com.chitchat.messaging.chitchatmessaging.utils.SimpleDividerItemDecoration;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ChatListActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewItemClickListener {

    private static final int REQUEST_INVITE = 475;

    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayList<String> userKeyList = new ArrayList<>();
    private ArrayList<Message> lastMessageList = new ArrayList<>();

    private ContactRecyclerAdapter adapter;

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            startActivity(new Intent(ChatListActivity.this, HomeScreenActivity.class));
            finish();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        readContacts();

        RecyclerView mContactList = findViewById(R.id.contact_list);
        mContactList.setLayoutManager(new LinearLayoutManager(this));
        mContactList.addItemDecoration(new SimpleDividerItemDecoration(this));

        adapter = new ContactRecyclerAdapter(ChatListActivity.this, usersList, lastMessageList);
        adapter.setOnRecyclerViewItemClickListener(this);
        mContactList.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fab :

                startActivity(new Intent(ChatListActivity.this, UserActivity.class));
                break;
        }
    }

    @Override
    public void onClick(View view, int position) {

        Intent chatIntent = new Intent(ChatListActivity.this, ChatActivity.class);
        chatIntent.putExtra("user_name", usersList.get(position).username);
        chatIntent.putExtra("user_id", userKeyList.get(position));
        startActivity(chatIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.main_logout_btn :
                // sign out from firebase authentication
                FirebaseAuth.getInstance().signOut();
                // launch StartActivity
                logOut();
                break;

            case R.id.main_settings_btn :
                // launch settings activity
                Intent settingsIntent = new Intent(ChatListActivity.this, AccountSettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.main_invite_btn:

                inviteFriends();
                break;

            case R.id.main_all_users_btn :
                // launch settings activity
                Intent usersIntent = new Intent(ChatListActivity.this, UserActivity.class);
                startActivity(usersIntent);
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("", "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

    private void readContacts() {

        String currentUser = getCurrentUser();

        mDatabase.child("Chats").child(currentUser).orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                usersList.clear();
                userKeyList.clear();
                lastMessageList.clear();

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        lastMessageList.add(0,snapshot.getValue(Message.class));

                        mDatabase.child("Users").child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                usersList.add(0, dataSnapshot.getValue(User.class));
                                userKeyList.add(0, dataSnapshot.getKey());
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void inviteFriends() {

        Intent intent = new AppInviteInvitation.IntentBuilder("Send application invitation")
                .setMessage("Welcome to ChitChat Messaging Application")
                .setDeepLink(Uri.parse("https://arunsharma.me/blog/how-to-use-firebase-dynamic-links-and-invites/"))
                //.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                //.setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    /**
     * Launch StartActivity.
     */
    private void logOut() {

        Intent startIntent = new Intent(ChatListActivity.this, HomeScreenActivity.class);
        startActivity(startIntent);
        finish();
    }

    public String getCurrentUser() {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {

            return "";
        }
    }
}
