package com.chitchat.messaging.chitchatmessaging.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout mEmailEt;

    private Button mResetLinkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        setUpView();

        mResetLinkBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.send_reset_link_btn:

                sendResetLink();
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

    private void setUpView() {

        Toolbar mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.reset_password);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmailEt = findViewById(R.id.email_input);
        mResetLinkBtn = findViewById(R.id.send_reset_link_btn);
    }

    private void sendResetLink() {

        String emailId = mEmailEt.getEditText().getText().toString();

        if (!TextUtils.isEmpty(emailId)) {

            FirebaseAuth.getInstance().sendPasswordResetEmail(emailId).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(PasswordActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(PasswordActivity.this, "An error occured.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
