package com.chitchat.messaging.chitchatmessaging.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.models.User;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    String name, status, image, thumb_image;
    String downloadUrl, thumb_downloadUrl;

    Uri mCropImageUri;

    private CircleImageView mDisplayImage;
    private Button mChangeImage, mChangeStatus;
    private TextView mName, mStatus;

    private ProgressDialog mProgressDialog;

    private StorageReference mImageStorage;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // initialise all view components
        setUpView();

        // initialise firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        // set up firebase database reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_REFERENCE);
        // enable firebase persistence
        mUserDatabase.keepSynced(true);

        // load user data from firebase database and storage and display in view components
        setUpUserCredentials();

        // button onCLick listener
        mChangeStatus.setOnClickListener(this);
        mChangeImage.setOnClickListener(this);
        // imageView onClick listener
        mDisplayImage.setOnClickListener(this);
    }

    //----------------------------------------------------------------------------------------------
    // onClick listeners
    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.settings_change_status :

                // button onClick listener
                // launch status activity
                Intent statusIntent = new Intent(AccountSettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra(Constants.INTENT_STATUS_KEY, status);
                startActivity(statusIntent);
                break;

            case R.id.settings_change_profile_picture :

                // button onClick listener
                // launch pickImage intent
                CropImage.startPickImageActivity(this);
                break;

            case R.id.settings_image :

                // imageView onClick listener
                // launch profileImage activity which displays profile image
                Intent profileImageIntent = new Intent(AccountSettingsActivity.this, ProfileImageActivity.class);
                profileImageIntent.putExtra(Constants.INTENT_IMAGE_URL_KEY, image);
                startActivity(profileImageIntent);
                break;
        }
    }

    /**
     * Activity result for pickImage intent.
     * Activity result for cropImage intent after picking image.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of crop image activity
        // obtain Uri of cropped image and save image in firebase storage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                // show progress dialog
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

                // save image in firebase storage
                saveImageInFirebaseStorage(resultUri, thumb_byte);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {

            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, R.string.permission_error_toast, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initialise all view components.
     */
    private void setUpView() {

        mDisplayImage =  findViewById(R.id.settings_image);
        mName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status);
        mChangeStatus = findViewById(R.id.settings_change_status);
        mChangeImage = findViewById(R.id.settings_change_profile_picture);

        // set up progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.profile_image_progress_dialog_message));
        mProgressDialog.setTitle(getString(R.string.profile_image_progress_dialog_title));
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Access current user details from firebase database and displays in view components.
     */
    private void setUpUserCredentials() {

        mUserDatabase.child(getCurrentUser()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    User user = dataSnapshot.getValue(User.class);

                    name = user.username;
                    status = user.status;
                    image = user.image;
                    thumb_image = user.thumb_image;
                }

                // set current user name
                mName.setText(name);
                // set user status
                mStatus.setText(status);

                // set profile image
                // picasso offline capabilities used.
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Start crop Image activity.
     *
     * @param imageUri is Uri of the image to be cropped
     */
    private void startCropImageActivity(Uri imageUri) {

        CropImage.activity(imageUri)
                .setAspectRatio(1,1)
                .setMinCropWindowSize(700, 700)
                .start(this);
    }

    /**
     * Store image in firebase storage.
     *
     * @param resultUri is the Uri of the image
     * @param thumb_byte is the byte[] for compressed image
     */
    private void saveImageInFirebaseStorage(final Uri resultUri, final byte[] thumb_byte) {

        // create storage path for profile image in firebase storage
        StorageReference filepath = mImageStorage.child(Constants.PROFILE_PICTURE_STORAGE_REFERENCE).child(getCurrentUser() + ".jpg");
        // create storage path for profile thumb image in firebase storage
        final StorageReference thumb_filepath = mImageStorage
                .child(Constants.PROFILE_PICTURE_STORAGE_REFERENCE).child(Constants.THUMB_IMAGE_STORAGE_REFERENCE).child(getCurrentUser() + ".jpg");

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

                            if (Url != null) {
                                thumb_downloadUrl = Url.toString();
                            }

                            if (thumb_task.isSuccessful()) {

                                updateDatabaseWithImage(resultUri);

                            } else {

                                Toast.makeText(AccountSettingsActivity.this, R.string.profile_image_upload_failed, Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }
                        }
                    });

                }
            }
        });
    }

    /**
     * Save Uri of image in firebase database.
     *
     * @param resultUri is the Uri of the image to be updated
     */
    private void updateDatabaseWithImage(final Uri resultUri) {

        // create hashMap of the image and thumb_image
        HashMap<String, Object> updateHashMap = new HashMap<>();

        updateHashMap.put(Constants.IMAGE_REFERENCE, downloadUrl);
        updateHashMap.put(Constants.THUMB_IMAGE_REFERENCE, thumb_downloadUrl);

        // update firebase database with Uri of image
        mUserDatabase.child(getCurrentUser()).updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    // dismiss progress dialog
                    mProgressDialog.dismiss();

                    Toast.makeText(AccountSettingsActivity.this, R.string.profile_image_update_msg, Toast.LENGTH_SHORT).show();

                    Picasso.with(getApplicationContext())
                            .load(resultUri)
                            .resize(480, 480)
                            .into(mDisplayImage);
                }
            }
        });
    }

    /**
     * @return current user
     */
    private String getCurrentUser() {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {

            return "";
        }
    }
}
