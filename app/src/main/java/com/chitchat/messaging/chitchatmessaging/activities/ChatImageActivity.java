package com.chitchat.messaging.chitchatmessaging.activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ChatImageActivity extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        mImageView = findViewById(R.id.chat_image);

        final Uri imageUri = Uri.parse(getIntent().getStringExtra(Constants.INTENT_CHAT_IMAGE_KEY));

        // picasso offline capabilities used.
        Picasso.with(ChatImageActivity.this)
                .load(imageUri)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // do nothing
                    }
                    @Override
                    public void onError() {
                        Picasso.with(ChatImageActivity.this)
                                .load(imageUri)
                                .into(mImageView);
                    }
                });
    }
}
