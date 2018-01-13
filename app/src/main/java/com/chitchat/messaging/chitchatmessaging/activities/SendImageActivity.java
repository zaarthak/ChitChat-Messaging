package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.HashMap;

public class SendImageActivity extends AppCompatActivity implements View.OnClickListener {

    private String downloadUrl = "default";

    private Uri imageUri;

    private ImageView mImageView;

    private ImageButton mSendBtn;

    private StorageReference mImageStorage;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        setUpView();

        imageUri = Uri.parse(getIntent().getStringExtra("image_uri"));

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Picasso.with(this)
                .load(imageUri)
                .into(mImageView);

        mSendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.image_send_btn:

                sendImage();
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

    private void setUpView() {

        Toolbar mToolbar = findViewById(R.id.send_image_toolbar);

        if (mToolbar != null) {

            setSupportActionBar(mToolbar);

            // set activity title
            // handle NullPointerException
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mImageView = findViewById(R.id.send_image_view);
        mSendBtn = findViewById(R.id.image_send_btn);
    }

    private void sendImage() {

        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String friendUser = getIntent().getStringExtra("user_id");

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final String timeStamp = String.valueOf(ts.getTime());

        // create storage path for profile image in firebase storage
        StorageReference filepath = mImageStorage.child("messages").child(currentUser).child(friendUser).child(timeStamp);
        // add image to firebase storage
        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    @SuppressWarnings("VisibleForTests") Uri Url = task.getResult().getDownloadUrl();
                    if (Url != null) {
                        downloadUrl = Url.toString();
                    }

                    final Message sentMessage = new Message(downloadUrl, "image", "sent", timeStamp, "read");
                    final Message receivedMessage = new Message(downloadUrl, "image", "received", timeStamp, "unread");

                    mDatabase.child("Chats").child(currentUser).child(friendUser).setValue(sentMessage);
                    mDatabase.child("Chats").child(friendUser).child(currentUser).setValue(receivedMessage);

                    mDatabase.child("Messages").child(currentUser).child(friendUser).child(timeStamp).setValue(sentMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mDatabase.child("Messages").child(friendUser).child(currentUser).child(timeStamp).setValue(receivedMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    finish();
                                }
                            });
                        }
                    });

                    mDatabase.child("Notifications").child(friendUser).child(currentUser).child(timeStamp).setValue(receivedMessage);
                }
            }
        });
    }
}
