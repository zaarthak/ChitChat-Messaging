package com.chitchat.messaging.chitchatmessaging.loginmanagers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.LoginListener;
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
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Handles Facebook login
 */

public class FacebookSignInManager {

    private Context mContext;

    private FirebaseAuth mAuth;

    private LoginListener mLoginListener;

    public FacebookSignInManager(Context context, LoginListener loginListener) {

        this.mContext = context;
        this.mLoginListener = loginListener;
        mAuth = FirebaseAuth.getInstance();
    }

    public void handleFacebookAccessToken(final AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

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

    private void getImageUrl(FirebaseUser mUser, DatabaseReference mDatabase) {

        String photoUrl;

        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        for (UserInfo profile : mAuth.getCurrentUser().getProviderData()) {

            System.out.println(profile.getProviderId());
            // check if the provider id matches "facebook.com"
            if (profile.getProviderId().equals("facebook.com")) {

                String facebookUserId = profile.getUid();

                // construct the URL to the profile picture, with a custom height
                // alternatively, use '?type=small|medium|large' instead of ?height=
                photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";

                User user = new User(mUser.getDisplayName(), mUser.getEmail(), Long.valueOf(mUser.getPhoneNumber()), mContext.getString(R.string.default_status), photoUrl, mUser.getPhotoUrl().toString(), deviceToken);

                // store user details to firebase database
                mDatabase.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mLoginListener.onLoginSuccess();
                        } else {

                            mLoginListener.onLoginFailure();
                        }
                    }
                });
            }
        }
    }
}
