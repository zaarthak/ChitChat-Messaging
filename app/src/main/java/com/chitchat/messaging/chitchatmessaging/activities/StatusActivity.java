package com.chitchat.messaging.chitchatmessaging.activities;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSaveButton;
    private TextInputLayout mStatus;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mStatusDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // initialise all view components
        setUpView();

        // get current user ID to access details from firebase database
        // since details are stored in firebase database with key as UID
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUID = mCurrentUser.getUid();

        // access current user details from firebase database
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_REFERENCE).child(currentUID);
        mStatusDatabase.keepSynced(true);

        // get 'Status' of current user
        String statusValue = getIntent().getStringExtra(Constants.INTENT_STATUS_KEY);

        // set current user 'status'
        mStatus.getEditText().setText(statusValue);
        mStatus.getEditText().setSelection(mStatus.getEditText().length());

        // handle button onClick listener
        mSaveButton.setOnClickListener(this);
    }

    // button onClick listener
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.status_save_btn :

                String status = mStatus.getEditText().getText().toString();

                // set up progress dialog
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle(getString(R.string.change_status_progress_dialog_title));
                mProgressDialog.setMessage(getString(R.string.change_status_progress_dialog_message));
                mProgressDialog.show();

                // update 'status' field in firebase database
                mStatusDatabase.child(Constants.INTENT_STATUS_KEY).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.change_status_success, Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(getApplicationContext(), R.string.change_status_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
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

        Toolbar mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.status_activity_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mStatus = findViewById(R.id.status_input);
        mSaveButton = findViewById(R.id.status_save_btn);
    }
}
