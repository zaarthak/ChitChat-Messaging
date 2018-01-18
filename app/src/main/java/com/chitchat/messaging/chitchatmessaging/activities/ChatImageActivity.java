package com.chitchat.messaging.chitchatmessaging.activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.squareup.picasso.Picasso;

public class ChatImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        ImageView mImageView = findViewById(R.id.chat_image);

        Picasso.with(ChatImageActivity.this)
                .load(Uri.parse(getIntent().getStringExtra(Constants.INTENT_CHAT_IMAGE_KEY)))
                .into(mImageView);
    }
}
