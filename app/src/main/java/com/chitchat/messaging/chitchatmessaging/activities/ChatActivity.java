package com.chitchat.messaging.chitchatmessaging.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.chitchat.messaging.chitchatmessaging.R;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpView();
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
        }
    }
}
