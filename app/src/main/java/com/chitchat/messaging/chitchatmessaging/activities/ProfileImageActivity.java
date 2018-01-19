package com.chitchat.messaging.chitchatmessaging.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.chitchat.messaging.chitchatmessaging.utils.SquareImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ProfileImageActivity extends AppCompatActivity {

    private SquareImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        // initialise all view components
        setUpView();

        // set image in imageView
        setImage();
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
     * Initialise all view components.
     */
    private void setUpView() {

        Toolbar mToolbar = findViewById(R.id.profile_image_toolbar);

        if (mToolbar != null) {

            setSupportActionBar(mToolbar);

            // set activity title
            // handle NullPointerException
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.profile_image_activity_title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mProfileImage = findViewById(R.id.profile_image_view);
    }

    /**
     * Set image in imageView.
     */
    private void setImage() {

        final String imageUrl = getIntent().getStringExtra(Constants.INTENT_IMAGE_URL_KEY);

        // picasso offline capabilities used.
        Picasso.with(getApplicationContext())
                .load(imageUrl)
                .placeholder(R.drawable.default_profile_picture)
                .resize(480,480)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        // do nothing
                    }

                    @Override
                    public void onError() {

                        Picasso.with(ProfileImageActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.default_profile_picture)
                                .into(mProfileImage);
                    }
                });
    }
}
