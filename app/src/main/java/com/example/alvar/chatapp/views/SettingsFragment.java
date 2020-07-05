package com.example.alvar.chatapp.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Dialogs.AlertDialogStatus;
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
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.example.alvar.chatapp.Utils.Constant.GALLERY_REQUEST_NUMBER;
import static com.example.alvar.chatapp.Utils.Constant.IMAGE_OPTION;
import static com.example.alvar.chatapp.Utils.NavHelper.navigateWithStack;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

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
    private View viewLayout;
    //Vars
    private String currentUserID, image;
    private Bitmap thumbnailImage = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewLayout = view;

        bindUI(view);
        retrieveDataFromDb();

        fabImage.setOnClickListener(this);
        fabStatus.setOnClickListener(this);
        imageProfile.setEnabled(true);
        imageProfile.setOnClickListener(this);
    }

    /**
     * UI elements
     */
    private void bindUI(View layout) {
        imageProfile = layout.findViewById(R.id.settingImgProfile);
        textStatus = layout.findViewById(R.id.settingsUserStatus);
        textUsername = layout.findViewById(R.id.settingsUsername);
        fabImage = layout.findViewById(R.id.fabImage);
        fabStatus = layout.findViewById(R.id.fabStatus);
        progressBar = layout.findViewById(R.id.settingsProgressBar);


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
     * method in charge of init "ImageProfileShow" dialog class saved in "Dialogs" folder
     */
    private void showChangeStatusDialog() {
        AlertDialogStatus dialog = new AlertDialogStatus();
        dialog.show(getActivity().getSupportFragmentManager(), "showChangeStatus");
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

                if (dataSnapshot.exists()) {

                    infoFetched(dataSnapshot);
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

        String name = dataSnapshot.child("name").getValue().toString();
        String status = dataSnapshot.child("status").getValue().toString();
        final String imageThumbnail = dataSnapshot.child("imageThumbnail").getValue().toString();
        //this image is global to be able to send it as a bundle
        image = dataSnapshot.child("image").getValue().toString();

        textUsername.setText(name);
        textStatus.setText(status);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        try {
            //here we set image from database into imageView
            Glide.with(getContext())
                    .setDefaultRequestOptions(options)
                    .load(imageThumbnail)
                    .into(imageProfile);

        } catch (NullPointerException e) {
            Log.e(TAG, "infoFetched: error loading image");
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_NUMBER && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(getActivity());
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //if everything goes well we change image

                if (result != null) {

                    changeProfilePic(result);

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
            thumbnailImage = new Compressor(getContext())
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
                    Toast.makeText(getContext(), getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * this method downloads the info from the image (without resized) to later be save from storage to database
     *
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
                    Toast.makeText(getContext(), getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    /**
     * method in charge of init "ImageProfileShow" dialog class saved in "Dialogs" folder
     */
    private void showAlertDialogImage() {
        /*ImageProfileShow imageDialog = new ImageProfileShow();
        imageDialog.show(getActivity().getSupportFragmentManager(), "showImageProfile");*/

        Bundle bundle = new Bundle();
        bundle.putString("messageContent", image);
        Log.d(TAG, "showAlertDialogImage: image: " + image);
        navigateWithStack(viewLayout, R.id.imageLargeFragment, bundle);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fabImage:
                openGallery();
                break;
            case R.id.fabStatus:
                showChangeStatusDialog();
                break;
            case R.id.settingImgProfile:
                showAlertDialogImage();
                break;
        }
    }
}
