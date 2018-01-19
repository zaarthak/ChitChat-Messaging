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
import com.chitchat.messaging.chitchatmessaging.loginmanagers.FacebookSignInManager;
import com.chitchat.messaging.chitchatmessaging.loginmanagers.GoogleSignInManager;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.chitchat.messaging.chitchatmessaging.utils.LoginListener;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener
        , FacebookCallback<LoginResult>, LoginListener {

    private EditText mEmailInput, mPassInput;
    private Button mLoginButton, mRegButton, mForgotPassBtn;

    private FancyButton mGoogleSignInBtn;
    private FancyButton mFbLoginBtn;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialise all view components
        setUpView();

        // create firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        // create firebase database instance
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_REFERENCE);

        // configure google sign-in to request the user's ID, email address, and basic profile
        // ID and basic profile are included in DEFAULT_SIGN_IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginActivity.this  /* Activity */ , this  /* OnConnectionFailedListener */ )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // create a callbackManager to handle facebook login responses
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, LoginActivity.this);

        //------------------------------------------------------------------------------------------
        // button onClick listeners
        //------------------------------------------------------------------------------------------
        mRegButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mForgotPassBtn.setOnClickListener(this);

        mGoogleSignInBtn.setOnClickListener(this);
        mFbLoginBtn.setOnClickListener(this);
    }

    //----------------------------------------------------------------------------------------------
    // button onClick listener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.login_register_btn:

                // launch register activity
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                break;

            case R.id.login_login_btn:

                // log in registered user
                loginUser();
                break;

            case R.id.login_forgot_pass_btn:

                // launch password activity
                Intent passwordIntent = new Intent(LoginActivity.this, PasswordActivity.class);
                startActivity(passwordIntent);
                break;

            case R.id.login_google_sign_in_btn:

                // launch google sign-in intent
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
                break;

            case R.id.login_fb_login_btn:

                // create an instance of facebook loginManager to login via facebook
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_friends"));
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    // google and facebook sign-in result
    //----------------------------------------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // google sign-in result
        if (requestCode == Constants.RC_SIGN_IN) {
            // result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // show progress dialog
                mProgressDialog.show();

                GoogleSignInAccount account = result.getSignInAccount();
                new GoogleSignInManager(this, this).firebaseAuthWithGoogle(account);
            } else {

                Toast.makeText(this, R.string.login_user_auth_failed, Toast.LENGTH_SHORT).show();
            }
        }
        // facebook login result
        else {
            // pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //----------------------------------------------------------------------------------------------
    // google connectionFailed listener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, R.string.login_user_auth_failed, Toast.LENGTH_SHORT).show();
    }

    //----------------------------------------------------------------------------------------------
    // facebook callback listeners
    //----------------------------------------------------------------------------------------------
    @Override
    public void onSuccess(LoginResult loginResult) {

        // display progress dialog
        mProgressDialog.show();
        new FacebookSignInManager(this, this).handleFacebookAccessToken(loginResult.getAccessToken());
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    //----------------------------------------------------------------------------------------------
    // login listeners to update UI based on result of login
    //----------------------------------------------------------------------------------------------
    @Override
    public void onLoginSuccess() {

        // dismiss progress dialog
        mProgressDialog.dismiss();
        // launch ChatListActivity when user registration is complete
        Intent mainActivity = new Intent(LoginActivity.this, ChatListActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);

        // finish current activity
        finish();
    }

    @Override
    public void onLoginFailure() {

        // dismiss progress dialog
        mProgressDialog.dismiss();
        // display error message
        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialise all view components.
     */
    private void setUpView() {

        mRegButton = findViewById(R.id.login_register_btn);
        mLoginButton = findViewById(R.id.login_login_btn);
        mForgotPassBtn = findViewById(R.id.login_forgot_pass_btn);

        mEmailInput = findViewById(R.id.login_email);
        mPassInput = findViewById(R.id.login_password);

        mGoogleSignInBtn = findViewById(R.id.login_google_sign_in_btn);
        mFbLoginBtn = findViewById(R.id.login_fb_login_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.login_progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.login_progress_dialog_message));
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Login registered user using firebase authentication.
     */
    private void loginUser() {

        String email = mEmailInput.getText().toString();
        String password = mPassInput.getText().toString();

        if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

            // show progress dialog
            mProgressDialog.show();
            // log in registered user
            loginWithFirebase(email, password);
        } else {

            Toast.makeText(LoginActivity.this, R.string.login_error_message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Logs in registered user with firebase authentication.
     *
     * @param email is the email ID of the user
     * @param password is the password of the user
     */
    public void loginWithFirebase(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDatabase.child(mAuth.getCurrentUser().getUid()).child(Constants.DEVICE_TOKEN_REFERENCE).setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            // dismiss progress dialog
                            mProgressDialog.dismiss();
                            // launch LoginActivity when user registration is complete
                            Intent mainIntent = new Intent(LoginActivity.this, ChatListActivity.class);
                            startActivity(mainIntent);
                            // finish current activity
                            finish();
                        }
                    });
                } else {

                    // display error message
                    mProgressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
