package com.chitchat.messaging.chitchatmessaging.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    User user;

    private CircleImageView mProfileImageView;
    private TextView mProfileNameTv, mStatusTv;

    DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // initialise all view components
        setUpView();

        // set up firebase database reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_REFERENCE);
        // enable firebase persistence
        mUserDatabase.keepSynced(true);

        // read user details from firebase database
        readUser();
    }

    //----------------------------------------------------------------------------------------------
    // imageView onClick listener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.profile_image:

                // launch profileImage activity
                Intent profileImageIntent = new Intent(ProfileActivity.this, ProfileImageActivity.class);
                profileImageIntent.putExtra(Constants.INTENT_IMAGE_URL_KEY, user.image);
                startActivity(profileImageIntent);
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
     * Initialise all view components
     */
    private void setUpView() {

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setElevation(0);
        }

        mProfileImageView = findViewById(R.id.profile_image);
        mProfileNameTv = findViewById(R.id.profile_name);
        mStatusTv = findViewById(R.id.profile_status);

        mProfileImageView.setOnClickListener(this);
    }

    /**
     * Read user details from firebase database and display in view components
     */
    private void readUser() {

        mUserDatabase.child(getIntent().getStringExtra(Constants.INTENT_USER_ID_KEY)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                user = dataSnapshot.getValue(User.class);

                mProfileNameTv.setText(user.username);
                mStatusTv.setText(user.status);

                Picasso.with(getApplicationContext())
                        .load(user.image)
                        .placeholder(R.drawable.default_profile_picture)
                        .resize(480,480)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                // do nothing
                            }

                            @Override
                            public void onError() {

                                Picasso.with(ProfileActivity.this)
                                        .load(user.image)
                                        .placeholder(R.drawable.default_profile_picture)
                                        .into(mProfileImageView);
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
