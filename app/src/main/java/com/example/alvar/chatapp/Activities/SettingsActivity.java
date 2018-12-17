package com.example.alvar.chatapp.Activities;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Utils.ProgressBarHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    //log
    private static final String TAG = "SettingsPage";
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private StorageTask uploadPhotoTask;
    //UI elements
    private CircleImageView imageProfile;
    private TextView textStatus, textUsername;
    private FloatingActionButton fabOptionStatus, fabOptionImage;
    private ProgressBar settingsProgressBar;
    //Vars
    private String userID;
    private String name, status, image, imageThumbnail, email;
    //Const firebase database
    private static final String DATABASE_NODE = "Users";
    private static final String DATABASE_CHILD_NAME = "name";
    private static final String DATABASE_CHILD_EMAIL = "email";
    private static final String DATABASE_CHILD_IMAGE = "image";
    private static final String DATABASE_CHILD_IMAGE_THUMBNAIL = "imageThumbnail";
    private static final String DATABASE_CHILD_STATUS = "status";
    //Cons image gallery pick
    private static final int PICK_IMAGE_REQUEST_NUMBER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bindUI();
        //fab buttons listeners
        fabButtonClicked();
        //init firebase and get current user ID
        initFirebase();
        //init retrieve data from firebase database method
        retrieveDataFromDb();
    }

    /**
     * UI elements
     */
    private void bindUI(){
        imageProfile    = findViewById(R.id.settingImgProfile);
        textStatus      =  findViewById(R.id.settingsUserStatus);
        textUsername    = findViewById(R.id.settingsUsername);
        fabOptionImage  =  findViewById(R.id.fabOption1);
        fabOptionStatus =  findViewById(R.id.fabOption2);
        settingsProgressBar = findViewById(R.id.settingsProgressBar);
    }

    /**
     * Method in charge of event when fab buttons are clicked
     */
    private void fabButtonClicked(){
        fabOptionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        fabOptionStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send current status tu change status page in a intent with bundle
                String currentStatus = textStatus.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, StatusChangeActivity.class);
                intent.putExtra("currentStatus", currentStatus);
                startActivity(intent);
            }
        });
    }
    /**
     method in charge of initialize firebase service
     */
    private void initFirebase(){
        //init Firebase auth
        mAuth = FirebaseAuth.getInstance();
        //we get current user logged in
        currentUser = mAuth.getCurrentUser();
        //save unique UID from user logged-in to a var type String named "userID"
        userID = currentUser.getUid();
        //init Firebase database
        database = FirebaseDatabase.getInstance();
        //init database reference and we point to the users data by passing "userID" as child.
        mRef = database.getReference(DATABASE_NODE).child(userID);
        Log.i(TAG, "initFirebase: userid: " + userID);
        //init firebase Storage and it's point toward the route folder in our firebase console
        storageRef = FirebaseStorage.getInstance().getReference();

    }

    /**
     * method in charge of fetching data from database
     */
    private void retrieveDataFromDb(){

        // Read from the database
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: data retrieved :" + dataSnapshot);
                infoFetched(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i(TAG, "Failed to read value." + error.toException());
            }
        });
    }

    /**
     * method in charge of retrieving info and set it into UI.
     * @param dataSnapshot
     */

    private void infoFetched(DataSnapshot dataSnapshot){
     //save info retrieved from DB into String vars
      name = dataSnapshot.child(DATABASE_CHILD_NAME).getValue().toString();
      email = dataSnapshot.child(DATABASE_CHILD_EMAIL).getValue().toString();
      status = dataSnapshot.child(DATABASE_CHILD_STATUS).getValue().toString();
      image = dataSnapshot.child(DATABASE_CHILD_IMAGE).getValue().toString();
      imageThumbnail = dataSnapshot.child(DATABASE_CHILD_IMAGE_THUMBNAIL).getValue().toString();

      //set values to display
      textUsername.setText(name);
      textStatus.setText(status);
      //set default image when there is no picture uploaded to database
      if (image.equals("image")){
          imageProfile.setImageResource(R.drawable.imgdefault);
      } else {
          //set image from database to imageView (using glide)
          Glide.with(SettingsActivity.this).load(image).into(imageProfile);
      }

      Log.i(TAG, "infoFetched: name: " + name);
      Log.i(TAG, "infoFetched: status: " + status);
      Log.i(TAG, "infoFetched: image: " + image);
      Log.i(TAG, "infoFetched: imgThumbnail: " + imageThumbnail);
      Log.i(TAG, "infoFetched: email: " + email);

    }

    /**
     * method in charge of picking image from phones gallery
     */
    private void chooseImage(){
       Intent intentImage = new Intent();
       intentImage.setType("image/*");
       intentImage.setAction(Intent.ACTION_GET_CONTENT);
       startActivityForResult(Intent.createChooser(intentImage, "SELECT IMAGE"), PICK_IMAGE_REQUEST_NUMBER);
    }


    private void changeImage( CropImage.ActivityResult imageResult){
        //let's show progressbar while we upload image to firebase storage
        ProgressBarHelper.showProgressBar(settingsProgressBar);
        //we get current user unique ID to then pass it as image name in firebase storage
        userID = currentUser.getUid();
        //we get image data from gallery and save it into Uri variable type
        Uri resultUri = imageResult.getUri();
        //get the route of the folder we created in firebase storage and set the name of the image
        final StorageReference filePath = storageRef.child("profile_images/").child( userID + ".jpg");
        //now we upload image from gallery to firebase storage
        uploadPhotoTask = filePath.putFile(resultUri);
        uploadPhotoTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri> >() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    //here we get image uri
                    Uri downloadUri = task.getResult();

                    if (downloadUri != null){
                            String imageUri = downloadUri.toString();   //here we cast image Uri info into String
                            Log.i(TAG, "onComplete: downloadUri: " + imageUri);
                            //pass image from storage to database
                            uploadImageToStorage(imageUri);
                            //hide progressBar
                            ProgressBarHelper.hideProgressBar(settingsProgressBar);
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onComplete: error: " + error);
                    }

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) { //if task is not successful
                //we get message error from firabase
                String uploadingError = exception.getMessage();
                //hide progressBar
                ProgressBarHelper.hideProgressBar(settingsProgressBar);
                //show message to user in case something went wrong
                Toast.makeText(SettingsActivity.this,  uploadingError, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onFailure: exception " + uploadingError);
            }
        });
    }

    /**
     * method in charge of uploading image from firebase storage to firebase database
     * @param imageUri
     */
    private void uploadImageToStorage(String imageUri) {
        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("image", imageUri);
        mRef.updateChildren(imageMap);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if user has selected image from gallery
        if (requestCode == PICK_IMAGE_REQUEST_NUMBER && resultCode == RESULT_OK){
            //we get image information saved into var type Uri
            Uri imgUri = data.getData();
            //We now send user to activity in charge of cropping the image
            CropImage.activity(imgUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        //THIS if statement code was taken from library guide github...  if everything goes well
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult imageResult = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //change image
                changeImage(imageResult);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = imageResult.getError();
            }
        }
    }


}








/*
 filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){

                    final String downloadedUrl = task.getResult().getStorage().getDownloadUrl().toString();

                    mRef.child("image").setValue(downloadedUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //hide progressbar once the picture it's uploaded to firebase storage
                                ProgressBarHelper.hideProgressBar(settingsProgressBar);
                                Toast.makeText(SettingsActivity.this, "image saved in database", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "error passing image to database", Toast.LENGTH_SHORT).show();
                                //hide progressbar once the picture it's uploaded to firebase storage
                                ProgressBarHelper.hideProgressBar(settingsProgressBar);
                            }

                        }
                    });

                } else {
                    Toast.makeText(SettingsActivity.this, "error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

*/
/*

    filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {   //if task successful

                //we get image URL
                final String downloadedUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                Log.i(TAG, "onSuccess: URL " + downloadedUrl);

                mRef.child("image").setValue(downloadedUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                //hide progressbar once the picture it's uploaded to firebase storage
                ProgressBarHelper.hideProgressBar(settingsProgressBar);
                Toast.makeText(SettingsActivity.this, "image saved in database", Toast.LENGTH_SHORT).show();
                     }
                }
                });

              }
                }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) { //if task is not successful
                //we get message error from firabase
                String uploadingError = exception.getMessage();
                //hide progressBar
                ProgressBarHelper.hideProgressBar(settingsProgressBar);
                //show message to user in case something went wrong
                Toast.makeText(SettingsActivity.this,  uploadingError, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onFailure: exception " + uploadingError);
                }
                });


*/