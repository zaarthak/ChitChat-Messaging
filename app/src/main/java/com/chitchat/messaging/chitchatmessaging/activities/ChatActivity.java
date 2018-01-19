package com.chitchat.messaging.chitchatmessaging.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.adapters.chatadapter.ChatRecyclerAdapter;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    Uri mCropImageUri;

    private ArrayList<Message> messageList = new ArrayList<>();

    private EditText mInputMessageEt;
    private ImageButton mAddMediaBtn, mSendBtn;

    private RecyclerView mMessageList;
    private ChatRecyclerAdapter adapter;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // set up firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // enable firebase persistence
        mDatabase.keepSynced(true);

        // read messages from firebase database
        readMessages();

        // set up view components
        setUpView();

        adapter = new ChatRecyclerAdapter(ChatActivity.this, messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        mMessageList.setAdapter(adapter);

        // button onCLick listener
        mSendBtn.setOnClickListener(this);
        mAddMediaBtn.setOnClickListener(this);
    }

    //----------------------------------------------------------------------------------------------
    // button onCLick listeners
    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.chat_send_btn :

                // save message details in firebase database
                sendMessage();
                break;

            case R.id.chat_add_media_btn:

                // launch pickImage intent
                CropImage.startPickImageActivity(this);
                break;

            case R.id.chat_toolbar:

                // launch profile activity
                Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                profileIntent.putExtra(Constants.INTENT_USER_ID_KEY, getIntent().getStringExtra(Constants.INTENT_USER_ID_KEY));
                startActivity(profileIntent);
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

    /**
     * Activity result for pickImage intent.
     * Activity result for cropImage intent after picking image.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of crop image activity
        // obtain Uri of cropped image and pass to sendImage activity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                // get profile image Uri
                final Uri resultUri = result.getUri();
                Intent sendImageIntent = new Intent(ChatActivity.this, SendImageActivity.class);
                sendImageIntent.putExtra(Constants.INTENT_IMAGE_URL_KEY, resultUri.toString());
                sendImageIntent.putExtra(Constants.INTENT_USER_ID_KEY, getIntent().getStringExtra(Constants.INTENT_USER_ID_KEY));
                sendImageIntent.putExtra(Constants.INTENT_USER_NAME_KEY, getIntent().getStringExtra(Constants.INTENT_USER_NAME_KEY));
                startActivity(sendImageIntent);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {

            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, R.string.permission_error_toast, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initialise all view components.
     */
    private void setUpView() {

        Toolbar mToolbar = findViewById(R.id.chat_toolbar);
        mToolbar.setOnClickListener(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {

            getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.INTENT_USER_NAME_KEY));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mInputMessageEt = findViewById(R.id.chat_input_message);
        mSendBtn = findViewById(R.id.chat_send_btn);
        mAddMediaBtn = findViewById(R.id.chat_add_media_btn);
        mMessageList = findViewById(R.id.chat_messages_list);
    }

    /**
     * Read user messages from firebase database.
     */
    private void readMessages() {

        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String friendUser = getIntent().getStringExtra(Constants.INTENT_USER_ID_KEY);

        mDatabase.child(Constants.MESSAGES_REFERENCE).child(currentUser).child(friendUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                messageList.clear();

                if (dataSnapshot.exists()) {

                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        // add 'Message' to arrayList
                        messageList.add(0, child.getValue(Message.class));
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child(Constants.NOTIFICATIONS_REFERENCE).child(currentUser).child(friendUser).removeValue();
    }

    /**
     * Save message details in firebase database.
     */
    private void sendMessage() {

        String inputMessage = mInputMessageEt.getText().toString().trim();

        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String friendUser = getIntent().getStringExtra(Constants.INTENT_USER_ID_KEY);

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final String timeStamp = String.valueOf(ts.getTime());

        if (!TextUtils.isEmpty(inputMessage))  {

            final Message sentMessage = new Message(inputMessage, "text", "sent", timeStamp, "read");
            final Message receivedMessage = new Message(inputMessage, "text", "received", timeStamp, "unread");

            mDatabase.child(Constants.CHATS_REFERENCE).child(currentUser).child(friendUser).setValue(sentMessage);
            mDatabase.child(Constants.CHATS_REFERENCE).child(friendUser).child(currentUser).setValue(receivedMessage);

            mDatabase.child(Constants.MESSAGES_REFERENCE).child(currentUser).child(friendUser).child(timeStamp).setValue(sentMessage);
            mDatabase.child(Constants.MESSAGES_REFERENCE).child(friendUser).child(currentUser).child(timeStamp).setValue(receivedMessage);

            mDatabase.child(Constants.NOTIFICATIONS_REFERENCE).child(friendUser).child(currentUser).child(timeStamp).setValue(receivedMessage);

            mInputMessageEt.setText("");
        }
    }

    /**
     * Launch crop activity.
     *
     * @param imageUri is the Uri of the image to be cropped
     */
    private void startCropImageActivity(Uri imageUri) {

        CropImage.activity(imageUri).start(this);
    }
}
