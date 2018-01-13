package com.chitchat.messaging.chitchatmessaging.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.activities.ChatListActivity;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FacebookSignInManager {

    private Context mContext;

    private FirebaseAuth mAuth;

    public FacebookSignInManager(Context context) {

        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public void handleFacebookAccessToken(final AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        System.out.println("actre" + credential);

        mAuth.signInWithCredential(credential).addOnCompleteListener(((Activity) mContext), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser mUser = mAuth.getCurrentUser();

                            DatabaseReference mDatabase;
                            // create an instance of firebase database for the user
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());

                            getImageUrl(mUser, mDatabase);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(mContext, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getImageUrl(FirebaseUser mUser, DatabaseReference mDatabase) {

        String photoUrl;

        for (UserInfo profile : mAuth.getCurrentUser().getProviderData()) {

            System.out.println(profile.getProviderId());
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals("facebook.com")) {

                String facebookUserId = profile.getUid();

                // construct the URL to the profile picture, with a custom height
                // alternatively, use '?type=small|medium|large' instead of ?height=
                photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";

                User user = new User(mUser.getDisplayName(), mUser.getEmail(), mContext.getString(R.string.default_status), photoUrl, mUser.getPhotoUrl().toString());

                // store user details to firebase database
                mDatabase.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            // launch LoginActivity when user registration is complete
                            Intent mainActivity = new Intent(mContext, ChatListActivity.class);
                            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            mContext.startActivity(mainActivity);

                            // finish current activity
                            ((Activity) mContext).finish();
                        } else {

                            // display error message
                            Toast.makeText(mContext, "Oops.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
