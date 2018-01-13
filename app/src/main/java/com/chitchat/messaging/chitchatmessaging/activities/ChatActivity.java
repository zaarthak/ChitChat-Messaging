package com.chitchat.messaging.chitchatmessaging.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    Uri mCropImageUri;

    private ArrayList<Message> messageList = new ArrayList<>();

    private EditText mInputMessage;
    private ImageButton mAddMediaBtn, mSendBtn;

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
        linearLayoutManager.setReverseLayout(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        mMessageList.setAdapter(adapter);

        //create recycler adapter

        mSendBtn.setOnClickListener(this);
        mAddMediaBtn.setOnClickListener(this);
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
        mAddMediaBtn = findViewById(R.id.chat_add_media_btn);

        mMessageList = findViewById(R.id.chat_messages_list);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.chat_send_btn :

                sendMessage();
                break;

            case R.id.chat_add_media_btn:

                CropImage.startPickImageActivity(this);
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

    @Override
    @SuppressLint("NewApi")
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

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                // get profile image Uri
                final Uri resultUri = result.getUri();
                Intent sendImageIntent = new Intent(ChatActivity.this, SendImageActivity.class);
                sendImageIntent.putExtra("image_uri", resultUri.toString());
                sendImageIntent.putExtra("user_id", getIntent().getStringExtra("user_id"));
                sendImageIntent.putExtra("user_name", getIntent().getStringExtra("user_name"));
                startActivity(sendImageIntent);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
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

                        messageList.add(0, child.getValue(Message.class));
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("Notifications").child(currentUser).child(friendUser).removeValue();
    }

    private void sendMessage() {

        String inputMessage = mInputMessage.getText().toString().trim();

        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String friendUser = getIntent().getStringExtra("user_id");

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final String timeStamp = String.valueOf(ts.getTime());

        if (!TextUtils.isEmpty(inputMessage))  {

            final Message sentMessage = new Message(inputMessage, "text", "sent", timeStamp, "read");
            final Message receivedMessage = new Message(inputMessage, "text", "received", timeStamp, "unread");

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

            mDatabase.child("Notifications").child(friendUser).child(currentUser).child(timeStamp).setValue(receivedMessage);

            mInputMessage.setText("");
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }
}
