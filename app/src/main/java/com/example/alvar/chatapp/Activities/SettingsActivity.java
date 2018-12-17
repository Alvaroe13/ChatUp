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
    private UploadTask uploadTask;
    //UI elements
    private CircleImageView imageProfile;
    private TextView textStatus, textUsername;
    private FloatingActionButton fabOption1, fabOption2;
    private ProgressBar progressBar;
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
    //Gallery request
    private static final int GALLERY_REQUEST_NUMBER = 1;
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
        textStatus     = findViewById(R.id.settingsUserStatus);
        textUsername    = findViewById(R.id.settingsUsername);
        fabOption1      = findViewById(R.id.fabOption1);
        fabOption2      = findViewById(R.id.fabOption2);
        progressBar = findViewById(R.id.settingsProgressBar);
    }

    /**
     * Method in charge of event when fab buttons are clicked
     */
    private void fabButtonClicked(){
        fabOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        fabOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentStatus = textStatus.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, StatusChangeActivity.class);
                intent.putExtra("currentStatus", currentStatus);
                startActivity(intent);
            }
        });
    }

    /**
     * this method opens gallery to select the image
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"),  GALLERY_REQUEST_NUMBER);
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
        //init database reference and we aim to the users data by passing "userID" as child.
        mRef = database.getReference(DATABASE_NODE).child(userID);
        Log.i(TAG, "initFirebase: userid: " + userID);
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
            //if there is no pic uploaded to database we set default img
            if (image.equals("image")){
                imageProfile.setImageResource(R.drawable.imgdefault);
            } else{
                //here we set image from database into imageView
                Glide.with(this).load(image).into(imageProfile);
            }

            Log.i(TAG, "infoFetched: name: " + name);
            Log.i(TAG, "infoFetched: status: " + status);
            Log.i(TAG, "infoFetched: image: " + image);
            Log.i(TAG, "infoFetched: imgThumbnail: " + imageThumbnail);
            Log.i(TAG, "infoFetched: email: " + email);
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_NUMBER && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //if everything goes well we change image
                changeProfilePic(result);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    /**
     * this method has the logic of changing the saving the image from storage into the database
     * @param result
     */
    private void changeProfilePic(CropImage.ActivityResult result) {
        ProgressBarHelper.showProgressBar(progressBar);

        Uri resultUri = result.getUri();
        final StorageReference filepath = storageRef.child("profile_images/").child(userID + ".jpg");

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
                    Uri downloadUri = task.getResult();

                    if (downloadUri != null) {

                        String imgUri = downloadUri.toString(); //here is the image URL cast into String
                        Log.i(TAG, "onComplete: imgUri: " + imgUri);

                        //lets save image from storage into database
                        HashMap<String , Object> imgMap = new HashMap<>();
                        imgMap.put("image", imgUri);
                        mRef.updateChildren(imgMap);

                        ProgressBarHelper.hideProgressBar(progressBar);

                    }

                } else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
