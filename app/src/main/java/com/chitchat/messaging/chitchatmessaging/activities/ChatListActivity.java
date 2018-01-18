package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.chitchat.messaging.chitchatmessaging.adapters.ChatListRecyclerAdapter;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.chitchat.messaging.chitchatmessaging.utils.RecyclerViewItemClickListener;
import com.chitchat.messaging.chitchatmessaging.utils.SimpleDividerItemDecoration;
import com.facebook.login.LoginManager;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewItemClickListener {

    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayList<String> userKeyList = new ArrayList<>();
    private ArrayList<Message> lastMessageList = new ArrayList<>();

    private ArrayList<Integer> unreadMessageCountList = new ArrayList<>();

    private RecyclerView mContactList;
    private ChatListRecyclerAdapter adapter;

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check user login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            startActivity(new Intent(ChatListActivity.this, LoginActivity.class));
            finish();
        }

        // initialise all view components
        setUpView();

        // set up firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // enable firebase persistence
        mDatabase.keepSynced(true);

        // read contacts with whom conversation has taken place
        readContacts();

        adapter = new ChatListRecyclerAdapter(ChatListActivity.this, usersList, userKeyList, lastMessageList, unreadMessageCountList);
        adapter.setOnRecyclerViewItemClickListener(this);
        mContactList.setLayoutManager(new LinearLayoutManager(this));
        mContactList.addItemDecoration(new SimpleDividerItemDecoration(this));
        mContactList.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fab :

                // start user activity
                startActivity(new Intent(ChatListActivity.this, UserActivity.class));
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    // recyclerView item click listener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view, int position) {

        Intent chatIntent = new Intent(ChatListActivity.this, ChatActivity.class);
        chatIntent.putExtra(Constants.INTENT_USER_NAME_KEY, usersList.get(position).username);
        chatIntent.putExtra(Constants.INTENT_USER_ID_KEY, userKeyList.get(position));
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

                mDatabase.child(Constants.USERS_REFERENCE).child(getCurrentUser()).child(Constants.DEVICE_TOKEN_REFERENCE).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                        logOut();
                    }
                });
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
                // launch user activity
                Intent usersIntent = new Intent(ChatListActivity.this, UserActivity.class);
                startActivity(usersIntent);
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

    /**
     * Initialise all view components
     */
    private void setUpView() {

        mContactList = findViewById(R.id.contact_list);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    /**
     * Read contacts from firebase database with whom user has had a conversation
     */
    private void readContacts() {

        final String currentUser = getCurrentUser();

        mDatabase.child(Constants.CHATS_REFERENCE).child(currentUser).orderByChild(Constants.TIMESTAMP_REFERENCE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot chatDataSnapshot) {

                usersList.clear();
                userKeyList.clear();
                lastMessageList.clear();
                unreadMessageCountList.clear();

                if (chatDataSnapshot.exists()) {

                    for (DataSnapshot dataSnapshot : chatDataSnapshot.getChildren()) {

                        lastMessageList.add(0, dataSnapshot.getValue(Message.class));

                        mDatabase.child(Constants.USERS_REFERENCE).child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot userSnapshot) {

                                usersList.add(0, userSnapshot.getValue(User.class));
                                userKeyList.add(0, userSnapshot.getKey());

                                mDatabase.child(Constants.NOTIFICATIONS_REFERENCE).child(currentUser).child(userSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot notificationDataSnapshot) {

                                        if (userKeyList.contains(userSnapshot.getKey())) {

                                            //unreadMessageCountList.add(userKeyList.indexOf(userSnapshot.getKey()), (int) notificationDataSnapshot.getChildrenCount());
                                        }

                                        //Log.e("zxc1", String.valueOf(unreadMessageCountList.size()) + " " + notificationDataSnapshot.getChildrenCount());
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
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

        Intent inviteIntent = new AppInviteInvitation.IntentBuilder("Send application invitation")
                .setMessage("Welcome to ChitChat Messaging Application")
                .setDeepLink(Uri.parse("https://arunsharma.me/blog/how-to-use-firebase-dynamic-links-and-invites/"))
                //.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                //.setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(inviteIntent, Constants.REQUEST_INVITE);
    }

    /**
     * Launch StartActivity.
     */
    private void logOut() {

        Intent startIntent = new Intent(ChatListActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    /**
     * @return current user ID
     */
    private String getCurrentUser() {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {

            return "";
        }
    }
}
