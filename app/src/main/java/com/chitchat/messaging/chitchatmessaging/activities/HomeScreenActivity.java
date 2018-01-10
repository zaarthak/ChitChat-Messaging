package com.chitchat.messaging.chitchatmessaging.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailInput, mPassInput;
    private Button mLoginButton, mRegButton, mForgotPassBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // initialise all view components
        setUpView();

        mAuth = FirebaseAuth.getInstance();

        //-------------------------------------------------------------------
        // button onClick listeners
        //-------------------------------------------------------------------
        mRegButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mForgotPassBtn.setOnClickListener(this);
    }

    //---------------------------------------------------------------------------------------
    // button onClick listener
    //---------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.home_register_btn:

                launchRegisterActivity();
                break;

            case R.id.home_login_btn:

                loginUser();
                break;

            case R.id.home_forgot_pass_btn:

                launchPasswordActivity();
                break;
        }
    }

    /**
     * Initialise all view components
     */
    private void setUpView() {

        mRegButton = findViewById(R.id.home_register_btn);
        mLoginButton = findViewById(R.id.home_login_btn);
        mForgotPassBtn = findViewById(R.id.home_forgot_pass_btn);

        mEmailInput = findViewById(R.id.home_email);
        mPassInput = findViewById(R.id.home_password);
    }

    /**
     * Launch register activity
     */
    private void launchRegisterActivity() {

        Intent registerIntent = new Intent(HomeScreenActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * Login registered user using firebase authentication
     */
    private void loginUser() {

        // setup progress dialog
        ProgressDialog mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setTitle(getString(R.string.home_progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.home_progress_dialog_message));
        mProgressDialog.setCanceledOnTouchOutside(false);

        String email = mEmailInput.getText().toString();
        String password = mPassInput.getText().toString();

        if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

            // show progress dialog
            mProgressDialog.show();
            // login registered user
            loginWithFirebase(email, password, mProgressDialog);
        } else {

            Toast.makeText(HomeScreenActivity.this, R.string.home_error_message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Logs in registered user with firebase authentication
     *
     * @param email is the email ID of the user
     * @param password is the password of the user
     * @param mProgressDialog is a progress dialog which is dismissed when the user registration is complete
     */
    public void loginWithFirebase(String email, String password, final ProgressDialog mProgressDialog) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    // dismiss progress dialog
                    mProgressDialog.dismiss();

                    // launch HomeScreenActivity when user registration is complete
                    Intent mainIntent = new Intent(HomeScreenActivity.this, ChatListActivity.class);
                    startActivity(mainIntent);

                    // finish current activity
                    finish();
                } else {

                    // display error message
                    mProgressDialog.dismiss();
                    Toast.makeText(HomeScreenActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchPasswordActivity() {

        Intent passwordIntent = new Intent(HomeScreenActivity.this, PasswordActivity.class);
        startActivity(passwordIntent);
    }
}
