package com.chitchat.messaging.chitchatmessaging.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.activities.ChatListActivity;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GoogleSignInManager {

    private Context mContext;

    private FirebaseAuth mAuth;

    public GoogleSignInManager(Context context) {

        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        // sign in with Google credentials
        mAuth.signInWithCredential(credential).addOnCompleteListener(((Activity) mContext), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (!task.isSuccessful()) {

                    // display error message
                    Toast.makeText(mContext, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                } else {

                    // get current user UID
                    String UID = mAuth.getCurrentUser().getUid();

                    DatabaseReference mDatabase;
                    // create an instance of firebase database for the user
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);

                    User user = new User(account.getDisplayName(), account.getEmail(), mContext.getString(R.string.default_status), account.getPhotoUrl().toString(), account.getPhotoUrl().toString());

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
        });
    }
}
