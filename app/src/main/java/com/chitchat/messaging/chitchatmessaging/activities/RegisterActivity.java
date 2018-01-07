package com.chitchat.messaging.chitchatmessaging.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout mName, mEmail, mPassword;
    private Button mSignUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // initialise all view components
        setUpView();

        mAuth = FirebaseAuth.getInstance();

        // sign up button onClick listener
        mSignUp.setOnClickListener(this);
    }

    //------------------------------------------------------------------------
    // button onClick listener
    //------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.register_sign_up_btn :

                registerUser();
                break;
        }
    }

    /**
     * Initialise all view components
     */
    private void setUpView() {

        mSignUp =findViewById(R.id.register_sign_up_btn);

        mName = findViewById(R.id.register_name);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Register new user to firebase authentication
     */
    private void registerUser() {

        // setup progress dialog
        ProgressDialog mProgressDialog = new ProgressDialog(RegisterActivity.this);

        mProgressDialog.setTitle(getString(R.string.register_progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.register_progress_dialog_message));
        mProgressDialog.setCanceledOnTouchOutside(false);

        String name = mName.getEditText().getText().toString();
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();

        if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

            mProgressDialog.show();

            // register user to firebase authentication
            // store name and email to firebase database
            registerWithFirebase(name, email, password, mProgressDialog);
        }
    }

    /**
     * Registers new user to firebase authentication and adds details to firebase database
     *
     * @param name is the name of the user
     * @param email is the email ID of the user
     * @param password is the password of the user
     * @param mProgressDialog is a progress dialog which is dismissed when the user registration is complete
     */
    public void registerWithFirebase(final String name, final String email, String password, final ProgressDialog mProgressDialog) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {

                    mProgressDialog.hide();
                    Toast.makeText(RegisterActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                } else {

                    // get current user UID
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    String UID = currentUser.getUid();

                    DatabaseReference mDatabase;

                    // create a database reference for the user
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);

                    User user = new User(name, email, getString(R.string.default_status), "default", "default");

                    // store user details to firebase database
                    mDatabase.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                // dismiss progress dialog
                                mProgressDialog.dismiss();

                                // launch HomeScreenActivity when user registration is complete
                                Intent mainActivity = new Intent(RegisterActivity.this, ChatListActivity.class);
                                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainActivity);

                                // finish current activity
                                finish();
                            } else {

                                // display error message
                                mProgressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
