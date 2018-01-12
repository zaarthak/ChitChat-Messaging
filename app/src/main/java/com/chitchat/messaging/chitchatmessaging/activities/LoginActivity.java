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
import com.chitchat.messaging.chitchatmessaging.managers.FacebookSignInManager;
import com.chitchat.messaging.chitchatmessaging.managers.GoogleSignInManager;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, FacebookCallback<LoginResult> {

    private static final int RC_SIGN_IN = 123;

    private EditText mEmailInput, mPassInput;
    private Button mLoginButton, mRegButton, mForgotPassBtn;

    private SignInButton mGoogleSignInBtn;
    private LoginButton mFbLoginBtn;

    private FirebaseAuth mAuth;

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialise all view components
        setUpView();

        mAuth = FirebaseAuth.getInstance();

        // configure sign-in to request the user's ID, email address, and basic profile
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

        mCallbackManager = CallbackManager.Factory.create();

        //-------------------------------------------------------------------
        // button onClick listeners
        //-------------------------------------------------------------------
        mRegButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mForgotPassBtn.setOnClickListener(this);

        mGoogleSignInBtn.setOnClickListener(this);
        mFbLoginBtn.registerCallback(mCallbackManager, this);
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

            case R.id.home_google_sign_in_btn:

                googleSignIn();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                new GoogleSignInManager(this).firebaseAuthWithGoogle(account);
            } else {

                Toast.makeText(this, "Error occurred.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

        mGoogleSignInBtn = findViewById(R.id.home_google_sign_in_btn);
        mFbLoginBtn = findViewById(R.id.home_fb_login_btn);
        mFbLoginBtn.setReadPermissions("email", "public_profile");
    }

    /**
     * Launch register activity
     */
    private void launchRegisterActivity() {

        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
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

            Toast.makeText(LoginActivity.this, R.string.home_error_message, Toast.LENGTH_LONG).show();
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

                    // launch LoginActivity when user registration is complete
                    Intent mainIntent = new Intent(LoginActivity.this, ChatListActivity.class);
                    startActivity(mainIntent);

                    // finish current activity
                    finish();
                } else {

                    // display error message
                    mProgressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchPasswordActivity() {

        Intent passwordIntent = new Intent(LoginActivity.this, PasswordActivity.class);
        startActivity(passwordIntent);
    }

    private void googleSignIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(LoginResult loginResult) {

        new FacebookSignInManager(this).handleFacebookAccessToken(loginResult.getAccessToken());
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }
}
