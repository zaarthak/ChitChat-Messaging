package com.chitchat.messaging.chitchatmessaging;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class ChitChatMessaging extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // enable firebase disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // enable offline image loading for Picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
