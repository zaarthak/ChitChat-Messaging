package com.chitchat.messaging.chitchatmessaging.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mUserDatabase;

    private FirebaseUser mCurrentUser;

    private CircleImageView mDisplayImage;
    private Button mChangeImage, mChangeStatus;
    private TextView mName, mStatus;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_REQUEST = 1248;

    private StorageReference mImageStorage;

    String name, status, image, thumb_image;

    String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        setUpView();

        // get current logged in user
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // call firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        // get current user UID
        String currentUID  = mCurrentUser.getUid();
        // access current user details from firebase database
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        // enable firebase persistence
        mUserDatabase.keepSynced(true);

        // load user data from firebase database and storage and display in view components
        setUpUserCredentials();

        // handle button onCLick listeners
        mChangeStatus.setOnClickListener(this);
        mChangeImage.setOnClickListener(this);
        mDisplayImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.settings_change_status :

                // launch status activity
                Intent statusIntent = new Intent(AccountSettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("Status", status);
                startActivity(statusIntent);
                break;

            case R.id.settings_change_profile_picture :

                // launch gallery intent
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST);
                break;

            case R.id.settings_image :

                Intent profileImageIntent = new Intent(AccountSettingsActivity.this, ProfileImageActivity.class);
                profileImageIntent.putExtra("imageUrl", image);
                startActivity(profileImageIntent);
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(700, 700)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                // set up progress dialog
                mProgressDialog.setMessage("Please wait while we change your profile picture...");
                mProgressDialog.setTitle("Changing profile picture");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                // get profile image Uri
                final Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());

                // get compressed image bitmap from Uri
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                // create storage path for profile image in firebase storage
                StorageReference filepath = mImageStorage.child("profile_pictures").child(mCurrentUser.getUid() + ".jpg");
                // create storage path for profile thumb image in firebase storage
                final StorageReference thumb_filepath = mImageStorage
                        .child("profile_pictures").child("thumb").child(mCurrentUser.getUid() + ".jpg");

                // add image to firebase storage
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            @SuppressWarnings("VisibleForTests") Uri Url = task.getResult().getDownloadUrl();
                            if (Url != null) {
                                downloadUrl = Url.toString();
                            }

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    @SuppressWarnings("VisibleForTests") Uri Url = thumb_task.getResult().getDownloadUrl();
                                    String thumb_downloadUrl = null;
                                    if (Url != null) {
                                        thumb_downloadUrl = Url.toString();
                                    }

                                    if (thumb_task.isSuccessful()) {

                                        HashMap<String, Object> updateHashMap = new HashMap<>();

                                        updateHashMap.put("image", downloadUrl);
                                        updateHashMap.put("thumb_image", thumb_downloadUrl);

                                        mUserDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    mProgressDialog.dismiss();

                                                    Toast.makeText(AccountSettingsActivity.this, "Profile Picture Updated.", Toast.LENGTH_SHORT).show();

                                                    Picasso.with(getApplicationContext())
                                                            .load(resultUri)
                                                            .resize(480, 480)
                                                            .into(mDisplayImage);

                                                }
                                            }
                                        });

                                    } else {

                                        Toast.makeText(AccountSettingsActivity.this, "Update failed. Please try again.", Toast.LENGTH_LONG).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });

                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    private void setUpView() {

        mDisplayImage =  findViewById(R.id.settings_image);
        mName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status);
        mChangeStatus = findViewById(R.id.settings_change_status);
        mChangeImage = findViewById(R.id.settings_change_profile_picture);

        mProgressDialog = new ProgressDialog(this);
    }

    /**
     * Access current user details from firebase database and displays in view components
     */
    private void setUpUserCredentials() {

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    name = dataSnapshot.getValue(User.class).username;
                    status = dataSnapshot.getValue(User.class).status;
                    image = dataSnapshot.getValue(User.class).image;
                    thumb_image = dataSnapshot.getValue(User.class).thumb_image;
                }
                // set current user name
                mName.setText(name);
                // set user status
                mStatus.setText(status);

                // set user profile picture
                if (!image.equals("default")) {

                    Picasso.with(getApplicationContext())
                            .load(image)
                            .placeholder(R.drawable.default_profile_picture)
                            .resize(480,480)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mDisplayImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    // do nothing
                                }

                                @Override
                                public void onError() {

                                    Picasso.with(AccountSettingsActivity.this)
                                            .load(image)
                                            .placeholder(R.drawable.default_profile_picture)
                                            .into(mDisplayImage);
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
