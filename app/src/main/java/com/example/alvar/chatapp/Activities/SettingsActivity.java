package com.example.alvar.chatapp.Activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Dialogs.AlertDialogStatus;
import com.example.alvar.chatapp.Dialogs.ImageProfileShow;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.example.alvar.chatapp.Constant.GALLERY_REQUEST_NUMBER;
import static com.example.alvar.chatapp.Constant.IMAGE_OPTION;

public class SettingsActivity extends AppCompatActivity {


    private static final String TAG = "SettingsPage";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersRef;
    private FirebaseUser currentUser;
    private StorageReference storageRef, thumbnailImageRef;
    private UploadTask uploadTask, uploadThumbnailTask;
    //UI elements
    private CircleImageView imageProfile;
    private TextView textStatus, textUsername;
    private ProgressBar progressBar;
    private FloatingActionButton fabImage, fabStatus;
    //Vars
    private String currentUserID;
    private Bitmap thumbnailImage = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bindUI();
        //fab buttons listeners
        fabButtonClicked();
        initFirebase();
        retrieveDataFromDb();
        imageClick();
    }

    /**
     * UI elements
     */
    private void bindUI() {
        imageProfile = findViewById(R.id.settingImgProfile);
        textStatus = findViewById(R.id.settingsUserStatus);
        textUsername = findViewById(R.id.settingsUsername);
        fabImage = findViewById(R.id.fabImage);
        fabStatus = findViewById(R.id.fabStatus);
        progressBar = findViewById(R.id.settingsProgressBar);
    }

    /**
     * Method in charge of event when fab buttons are clicked
     */
    private void fabButtonClicked() {

        //first we set fab background color
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        fabStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeStatusDialog();
            }
        });
    }

    /**
     * this method opens gallery to select the image
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType(IMAGE_OPTION);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), GALLERY_REQUEST_NUMBER);
    }

    /**
     * method in charge of initialize firebase service
     */
    private void initFirebase() {
        //init Firebase auth
        mAuth = FirebaseAuth.getInstance();
        //we get current user logged in
        currentUser = mAuth.getCurrentUser();
        //save unique UID from user logged-in to a var type String named "userID"
        currentUserID = currentUser.getUid();
        //init Firebase database
        database = FirebaseDatabase.getInstance();
        dbUsersRef = database.getReference(getString(R.string.users_ref));
        dbUsersRef.keepSynced(true);

        //init firebase storage
        storageRef = FirebaseStorage.getInstance().getReference();
        //we create a "Thumbnail_Images" folder in firebase storage
        thumbnailImageRef = FirebaseStorage.getInstance().getReference();
    }

    /**
     * method in charge of fetching data from database
     */
    private void retrieveDataFromDb() {

        // Read from the database
        dbUsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: data retrieved :" + dataSnapshot);

                if (dataSnapshot.exists()) {
                    infoFetched(dataSnapshot);
                } else {
                    Toast.makeText(SettingsActivity.this,
                            "Unable to retrieve info from database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    /**
     * this method is the one in charge of fetching info from the db and set it to the UI
     *
     * @param dataSnapshot
     */
    private void infoFetched(DataSnapshot dataSnapshot) {

        //save info retrieved from DB into String vars
        String name = dataSnapshot.child("name").getValue().toString();
        String email = dataSnapshot.child("email").getValue().toString();
        String status = dataSnapshot.child("status").getValue().toString();
        String image = dataSnapshot.child("image").getValue().toString();
        String imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();
        //set values to display
        textUsername.setText(name);
        textStatus.setText(status);
        //if there is no pic uploaded to database we set default img
        if (imageThumbnail.equals("imgThumbnail")) {
            imageProfile.setImageResource(R.drawable.profile_image);
        } else {
            //here we set image from database into imageView
            Glide.with(getApplicationContext())
                    .load(imageThumbnail)
                    .into(imageProfile);

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_NUMBER && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //if everything goes well we change image

                if (result != null) {

                    changeProfilePic(result);

                } else {
                    Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: error: " + error);
            }
        }
    }

    /**
     * this method has the logic of saving the image from storage into the database
     *
     * @param result
     */
    private void changeProfilePic(CropImage.ActivityResult result) {
        ProgressBarHelper.showProgressBar(progressBar);

        Uri resultUri = result.getUri();

        //here we compress image size
        ////////////////////////////////here began the compression file process
        File filePathUri = new File(resultUri.getPath());

        try {
            //here we compress file and set it's dimension
            thumbnailImage = new Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(50)
                    .compressToBitmap(filePathUri);

        } catch (Exception e) {
            e.printStackTrace();
            String exception = e.getMessage();
            Log.i(TAG, "compressImage: exception: " + exception);
        }

        ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
        thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 50, byteImage);
        final byte[] thumb_byte = byteImage.toByteArray();
        /////////////////////////////////////////////////////here is the end of compressing-file process


        //here we create the "profile_images" folder in firebase storage
        final StorageReference filepath = storageRef.child("profile_images/").child(currentUserID + ".jpg");

        uploadTask = filepath.putFile(resultUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                if (!task.isSuccessful()) {

                    throw task.getException();
                }

                return filepath.getDownloadUrl();   //here we get URL info from storage (in object format)
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    downloadUrl(task);

                    downloadThumbnailUrl(thumb_byte);


                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * this method downloads the info from the image (without resized) to later be save from storage to database
     * @param task
     */
    private void downloadUrl(Task<Uri> task) {


        Uri downloadUri = task.getResult();

        if (downloadUri != null) {

            String imgUri = downloadUri.toString(); //here is the image URL cast into String
            Log.i(TAG, "onComplete: imgUri: " + imgUri);

            //lets save image from storage into database
            HashMap<String, Object> imgMap = new HashMap<>();
            imgMap.put("image", imgUri);
            dbUsersRef.child(currentUserID).updateChildren(imgMap);

            ProgressBarHelper.hideProgressBar(progressBar);

        }
    }


    /**
     * this method is in charge of downloading the thumbnail info and turn it into string to be saved
     * from storage to database
     *
     * @param thumb_byte
     */
    private void downloadThumbnailUrl(byte[] thumb_byte) {

        //here we create the "Thumbnail_Images" folder in firebase storage
        final StorageReference thumbFilePath = thumbnailImageRef.child("Thumbnail_Images").child(currentUserID + ".jpg");

        uploadThumbnailTask = thumbFilePath.putBytes(thumb_byte);
        //this method will add the image compressed into firebase storage
        uploadThumbnailTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                Task var = thumbFilePath.getDownloadUrl();

                Log.i(TAG, "then: URL " + var.toString());

                return var;   //here we get URL info from storage (in object format)
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    Uri downloadThumbnailUri = task.getResult();

                    if (downloadThumbnailUri != null) {
                        String finalThumbnailUri = downloadThumbnailUri.toString();

                        Log.i(TAG, "onComplete: URL : " + finalThumbnailUri);

                        //here we pass the thumbnail from storage to database at the "imageThumbnail" node
                        HashMap<String, Object> hashThumbnail = new HashMap<>();
                        hashThumbnail.put("imageThumbnail", finalThumbnailUri);
                        dbUsersRef.child(currentUserID).updateChildren(hashThumbnail);
                    }

                    ProgressBarHelper.hideProgressBar(progressBar);


                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * method in charge of handling event when image has been clicked
     */
    private void imageClick() {

        imageProfile.setEnabled(true);
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogImage();
            }
        });

    }

    /**
     * method in charge of init "ImageProfileShow" dialog class saved in "Dialogs" folder
     */
    private void showAlertDialogImage() {
        ImageProfileShow imageDialog = new ImageProfileShow();
        imageDialog.show(getSupportFragmentManager(), "showImageProfile");
    }


    /**
     * method in charge of init "ImageProfileShow" dialog class saved in "Dialogs" folder
     */
    private void showChangeStatusDialog() {
        AlertDialogStatus dialog = new AlertDialogStatus();
        dialog.show(getSupportFragmentManager(), "showChangeStatus");
    }

}
