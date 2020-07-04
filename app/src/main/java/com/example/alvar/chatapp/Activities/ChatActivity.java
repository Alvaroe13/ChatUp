package com.example.alvar.chatapp.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.alvar.chatapp.Adapter.MessageAdapter;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.Model.UserLocation;
import com.example.alvar.chatapp.Notifications.Data;
import com.example.alvar.chatapp.Notifications.NotificationAPI;
import com.example.alvar.chatapp.Notifications.PushNotification;
import com.example.alvar.chatapp.Notifications.ResponseFCM;
import com.example.alvar.chatapp.Notifications.RetrofitClient;
import com.example.alvar.chatapp.Notifications.Token;
import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Service.LocationService;
import com.example.alvar.chatapp.views.OtherUserFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.alvar.chatapp.Utils.Constant.CHAT_DOCX_MENU_REQUEST;
import static com.example.alvar.chatapp.Utils.Constant.CHAT_IMAGE_MENU_REQUEST;
import static com.example.alvar.chatapp.Utils.Constant.CHAT_PDF_MENU_REQUEST;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Utils.Constant.IMAGE_OPTION;
import static com.example.alvar.chatapp.Utils.Constant.PDF_FILE_EXTENSION;
import static com.example.alvar.chatapp.Utils.Constant.PDF_FOLDER_REF;
import static com.example.alvar.chatapp.Utils.Constant.PDF_MESSAGE_TYPE;
import static com.example.alvar.chatapp.Utils.Constant.PDF_OPTION;
import static com.example.alvar.chatapp.Utils.Constant.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.alvar.chatapp.Utils.Constant.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.alvar.chatapp.Utils.Constant.PHOTO_FILE_EXTENSION;
import static com.example.alvar.chatapp.Utils.Constant.PHOTO_FOLDER_REF;
import static com.example.alvar.chatapp.Utils.Constant.PHOTO_MESSAGE_TYPE;
import static com.example.alvar.chatapp.Utils.Constant.READ_EXTERNAL_STORAGE_REQUEST_CODE;
import static com.example.alvar.chatapp.Utils.Constant.SELECT_IMAGE;
import static com.example.alvar.chatapp.Utils.Constant.SELECT_PDF;
import static com.example.alvar.chatapp.Utils.Constant.SELECT_WORD_DOCUMENT;
import static com.example.alvar.chatapp.Utils.Constant.WORD_DOCUMENT_OPTION;
import static com.example.alvar.chatapp.Utils.Constant.WORD_DOC_FILE_EXTENSION;
import static com.example.alvar.chatapp.Utils.Constant.WORD_DOC_FOLDER_REF;
import static com.example.alvar.chatapp.Utils.Constant.WORD_DOC_MESSAGE_TYPE;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivityPage";
    private static final int OPEN_CAMERA_REQUEST_CODE = 55;
    private static final int CAMERA_PERMISSION_REQUEST = 56;

    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbChatsNodeRef, messagePushID, dbUsersNodeRef, dbTokensNodeRef, dbChatList;
    private UploadTask uploadTask;
    private ValueEventListener seenListener;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userLocationRef, userDocRef;
    //UI elements
    private Toolbar toolbarChat;
    private RecyclerView recyclerViewChat;
    private EditText chatEditText;
    private ImageButton buttonSend, buttonAttachFile;
    private CircleImageView imageProfile, onlineIcon;
    private TextView usernameToolbarChat, lastSeenToolbarChat;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar chatProgressBar;
    //vars
    private String contactID, currentUserID;
    private String contactName, contactImage;
    private String messageText, optionSelected;
    private MessageAdapter adapter;
    private List<Messages> messagesList;
    private Uri file;
    private boolean locationPermissionGranted = false;
    private UserLocation userLocation;
    private FusedLocationProviderClient locationProvider;
    private NotificationAPI apiService;
    private boolean notify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initFirebase();
        initFirestore();
        initLocationProvider();
        UIElements();
        getIncomingIntent();
        initRecycleView();
        sendButtonPressed();
        editTextStatus();
        otherUserState();
        toolbarPressed();
        attachFileButtonPressed();
        retrofit();
        seenMessage();


    }



    private void initLocationProvider() {
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
    }

    private void UIElements() {
        chatEditText = findViewById(R.id.chatEditText);
        buttonSend = findViewById(R.id.buttonChat);
        buttonAttachFile = findViewById(R.id.buttonAttachFile);
        chatProgressBar = findViewById(R.id.progressBarChat);
    }

    /**
     * this method receives de bundles from "ContactsFragment"
     */
    private void getIncomingIntent() {

        if (getIntent() != null) {
            contactID = getIntent().getStringExtra(CONTACT_ID);
            contactName = getIntent().getStringExtra(CONTACT_NAME);
            contactImage = getIntent().getStringExtra(CONTACT_IMAGE);
            Log.d(TAG, "getIncomingIntent: other user id: " + contactID);
            Log.d(TAG, "getIncomingIntent: contact name: " + contactName);
            Log.d(TAG, "getIncomingIntent: contact Image: " + contactImage);
            setToolbar( contactName, contactImage);
        }

    }

    /**
     * init firebase services
     */
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        //we get current user ID
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
        dbChatsNodeRef = database.getReference().child(getString(R.string.chats_ref)).child(getString(R.string.messages_ref));
        dbTokensNodeRef = database.getReference().child("Tokens");
        dbChatList = database.getReference().child("ChatList");

    }

    private void initFirestore() {
        //db
        mDb = FirebaseFirestore.getInstance();
        //docs ref
        userLocationRef = mDb.collection(getString(R.string.collection_user_location)).document(currentUserID);
        userDocRef = mDb.collection(getString(R.string.users_ref)).document(currentUserID);
    }

    /**
     * Create toolbar and inflate the custom bar chat bar layout
     */
    private void setToolbar(final String contactUsername, final String contactProfPic) {

        toolbarChat = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbarChat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewCustomBar = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(viewCustomBar);

        //UI elements from custom toolbar
        imageProfile = findViewById(R.id.imageToolbarChat);
        usernameToolbarChat = findViewById(R.id.usernameToolbarChat);
        lastSeenToolbarChat = findViewById(R.id.lastSeenChatToolbar);
        onlineIcon = findViewById(R.id.onlineIcon);

        //here we set info from bundles into the ui elements in custom toolbar
        usernameToolbarChat.setText(contactUsername);

        //GLIDE
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.profile_image);

        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(options)
                .load(contactProfPic)
                .into(imageProfile);

        //here we handle toolbar back button action
        toolbarChat.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMain();
            }
        });


    }

    /**
     * Here in this method we read the current state of the other user in real time to show it
     * in the toolbar.
     */
    private void otherUserState() {

        dbUsersNodeRef.child(contactID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    //here we get the other user's current state and we store it in each var
                    String saveLastSeenDate = dataSnapshot.child(getString(R.string.user_state_db)).child(getString(R.string.date_db)).getValue().toString();
                    String saveLastSeenTime = dataSnapshot.child(getString(R.string.user_state_db)).child(getString(R.string.time_db)).getValue().toString();
                    String saveSate = dataSnapshot.child(getString(R.string.user_state_db)).child(getString(R.string.state_db)).getValue().toString();
                    //retrieving other user's typing state
                    String typingState = dataSnapshot.child(getString(R.string.user_state_db)).child((getString(R.string.typing_db))).getValue().toString();

                    //if typing state in db is yes we should in toolbar that other user is typing
                    if (typingState.equals(getString(R.string.yes_db))) {
                        lastSeenToolbarChat.setText(R.string.typing);
                    } else {
                        //if user is online but not typing we show online on the toolbar
                        if (saveSate.equals(getString(R.string.online_db))) {
                            lastSeenToolbarChat.setText(R.string.activeNow);
                            onlineIcon.setVisibility(View.VISIBLE);

                            //if user is not typing nor "online" we show "offline" on the toolbar.
                        } else if (saveSate.equals(getString(R.string.offline_db))) {
                            lastSeenToolbarChat.setText(getString(R.string.lastSeen) + " " + saveLastSeenDate + " " + saveLastSeenTime);
                            onlineIcon.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * init recyclerView
     */
    private void initRecycleView() {

        //instance of arrayList of messages
        messagesList = new ArrayList<>();

        recyclerViewChat = findViewById(R.id.recyclerChat);
        recyclerViewChat.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);

        adapter = new MessageAdapter(ChatActivity.this, messagesList);
        recyclerViewChat.setAdapter(adapter);

    }

    /**
     * this method handle the click event when send button is pressed
     */
    private void sendButtonPressed() {
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;

                //we get message written by the user
                messageText = chatEditText.getText().toString().trim();

                //if field is empty
                if (messageText.equals("")) {
                    //show toast to the user
                    Toast.makeText(ChatActivity.this,
                            getString(R.string.noEmptyFieldAllowed), Toast.LENGTH_SHORT).show();
                } else {
                    //otherwise we send the message
                    //createChatListDB();
                    uploadMessageToDb(messageText, "text");
                    //we remove any text enter by the user once it's been sent
                    chatEditText.setText("");


                }

            }
        });
    }

    /**
     * we use onStart to update and display all the message every time a user send messages
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart: called");
        updateDateTime(getString(R.string.online_db));
        retrieveMessages();
        checkLocationStatus();

    }

    /**
     * fetch messages in the chat room.
     */
    private void retrieveMessages() {

        dbChatsNodeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        messagesList.clear();

                        if (dataSnapshot.exists()) {

                            for (DataSnapshot info : dataSnapshot.getChildren()) {

                                Messages messages = info.getValue(Messages.class);

                                try {

                                    if(messages.getSenderID().equals(currentUserID) && messages.getReceiverID().equals(contactID) ||
                                            messages.getSenderID().equals(contactID) && messages.getReceiverID().equals(currentUserID) ){

                                        messagesList.add(messages);
                                    }
                                }catch (Exception e){
                                    Log.e(TAG, "onDataChange: error: " + e.getMessage() );
                                }

                                adapter.notifyDataSetChanged();
                                recyclerViewChat.smoothScrollToPosition(recyclerViewChat.getAdapter().getItemCount());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * method in charge of setting true in seen field when receiver has seen the message.
     */
    private void seenMessage() {

        seenListener =  dbChatsNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Messages messages = snapshot.getValue(Messages.class);
                        try {
                            if (messages.getReceiverID().equals(currentUserID) && messages.getSenderID().equals(contactID)){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("seen", true);
                                snapshot.getRef().updateChildren(hashMap);
                            }
                        }catch (Exception e){
                            Log.e(TAG, "onDataChange: exception: " + e.getMessage() );
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        //in case the other close the chat activity the state changes to "offline"
        updateDateTime(getString(R.string.offline_db));
        typingState((getString(R.string.no_db)));
        dbChatsNodeRef.removeEventListener(seenListener);
    }

    /**
     * method in charge of getting the user's current state, time and Date to update in db
     */
    private void updateDateTime(String state) {

        String currentTime, currentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MMM/yyyy");
        currentDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm aaa");
        currentTime = time.format(calendar.getTime());

        //lets save all this info in a map to uploaded to the Firebase database.
        //NOTE: we use HashMap instead of an Object because the database doesn't accept a Java Object
        // when the database will be updated when using "updateChildren" whereas when using setValue you can use a Java Object.
        HashMap<String, Object> userState = new HashMap<>();
        userState.put((getString(R.string.state_db)), state);
        userState.put((getString(R.string.date_db)), currentDate);
        userState.put((getString(R.string.time_db)), currentTime);
        userState.put("location", "Off");

        dbUsersNodeRef.child(currentUserID).child((getString(R.string.user_state_db))).updateChildren(userState);

    }

    /**
     * method in charge of checking if edit txt is empty or not
     */
    private void editTextStatus() {

        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = s.toString();

                //in edit text is empty we set typing state as "no"
                if (text.isEmpty()) {
                    typingState((getString(R.string.no_db)));
                }
                //if edit text is not empty we set typing state as "yes"
                else {
                    typingState((getString(R.string.yes_db)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    /**
     * by passing the fragment we want to launch as param it'll inflate the view
     * @param fragment
     */
    private void launchFragment(Fragment fragment, String contactID){

        if (contactID != null){
            Bundle bundle = new Bundle();
            bundle.putString("contactID", contactID);
            fragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.layoutFrameID, fragment );
        transaction.addToBackStack(null);
        transaction.commit();


    }


    /**
     * method oni charge of taking the user to other user's profile when toolbar pressed in the chat room
     */
    private void toolbarPressed() {



        toolbarChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                launchFragment(new OtherUserFragment(), contactID);

            }
        });
    }

    /**
     * method in charge of updating the other user's typing state in the db in real time
     *
     * @param typingState
     */
    private void typingState(String typingState) {

        HashMap<String, Object> typingStateMap = new HashMap<>();
        typingStateMap.put(getString(R.string.typing_db), typingState);

        dbUsersNodeRef.child(currentUserID).child(getString(R.string.user_state_db)).updateChildren(typingStateMap);

    }

    /**
     * when attach file button is pressed
     */
    private void attachFileButtonPressed() {
        buttonAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });


    }

    /**
     * here we show the option for the user to choose.
     */
    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(R.string.Choose_file);
        builder.setIcon(R.drawable.ic_add_circle);
        //options to be shown in the Alert Dialog
        CharSequence menuOptions[] = new CharSequence[]{getString(R.string.photo), getString(R.string.PDF), getString(R.string.Word_Document), getString(R.string.share_location), getString(R.string.open_camera)};
        // we set the options
        builder.setItems(menuOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {

                switch (option) {
                    case 0: //if user selected photo option in pop up window
                        optionSelected = "photo";
                        if (checkPermissions()) {
                            openOption(IMAGE_OPTION, SELECT_IMAGE, CHAT_IMAGE_MENU_REQUEST);
                        }
                        break;
                    case 1: //if user selected pdf option in pop up window
                        optionSelected = "pdf file";
                        if (checkPermissions()) {
                            openOption(PDF_OPTION, SELECT_PDF, CHAT_PDF_MENU_REQUEST);
                        }
                        break;
                    case 2: //if user selected word option in pop up window
                        optionSelected = "word document";
                        if (checkPermissions()) {
                            openOption(WORD_DOCUMENT_OPTION, SELECT_WORD_DOCUMENT, CHAT_DOCX_MENU_REQUEST);
                        }
                        break;
                    case 3:
                        Log.d(TAG, "onClick: share location option pressed");
                        if (getLocationPermission()) {
                            shareLocationPressed();
                        }
                        break;
                    case 4:
                        Log.d(TAG, "onClick: take photo option selected");
                        if (checkCameraPermission()) {
                            openCamera();
                        }
                        break;
                }


            }
        });

        builder.show();
    }

    /**
     * check permission for reading internal docs and media only so far
     *
     * @return
     */
    private Boolean checkPermissions() {
        Log.d(TAG, "checkPermissions: called");

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this, permissions, READ_EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        }
    }

    /**
     * this method check if users camera and create files in device are granted or not, if not we ask for them
     *
     * @return
     */
    private Boolean checkCameraPermission() {

        String[] cameraPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                cameraPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        cameraPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this, cameraPermissions, CAMERA_PERMISSION_REQUEST);
            return false;
        }

    }

    /**
     * open camera
     */
    private void openCamera() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCamera, OPEN_CAMERA_REQUEST_CODE);
    }

    /**
     * this method opens the windows for the user to choose either to send "image", "pdf" or "word doc"
     */
    private void openOption(String fileType, String title, int codeRequest) {
        Intent intent = new Intent();
        intent.setType(fileType);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, title), codeRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHAT_IMAGE_MENU_REQUEST || requestCode == CHAT_PDF_MENU_REQUEST ||
                requestCode == CHAT_DOCX_MENU_REQUEST || requestCode == OPEN_CAMERA_REQUEST_CODE  ) {

            if (resultCode == RESULT_OK) {
                //we store the file (image, pdf, word) selected in this var of URI type.

                try {
                    file = data.getData();

                    switch (requestCode) {
                        case CHAT_IMAGE_MENU_REQUEST:
                            Log.i(TAG, "onActivityResult: photo selected ready to upload in to firebase storage");
                            chatProgressBar.setVisibility(View.VISIBLE);
                            saveFileInStorage(file, PHOTO_FOLDER_REF , PHOTO_FILE_EXTENSION , PHOTO_MESSAGE_TYPE);
                            break;
                        case CHAT_PDF_MENU_REQUEST:
                            Log.i(TAG, "onActivityResult: pdf file selected ready to upload in to firebase storage");
                            chatProgressBar.setVisibility(View.VISIBLE);
                            saveFileInStorage(file, PDF_FOLDER_REF , PDF_FILE_EXTENSION , PDF_MESSAGE_TYPE );
                            break;
                        case CHAT_DOCX_MENU_REQUEST:
                            Log.i(TAG, "onActivityResult: word document selected ready to upload in to firebase storage");
                            chatProgressBar.setVisibility(View.VISIBLE);
                            saveFileInStorage(file, WORD_DOC_FOLDER_REF , WORD_DOC_FILE_EXTENSION , WORD_DOC_MESSAGE_TYPE);
                            break;
                        case OPEN_CAMERA_REQUEST_CODE:
                            Log.d(TAG, "onActivityResult: photo taken, now we should redirect user to other fragment");
                            //TODO pending to finnish: send photo in chat
                            break;
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onActivityResult: error: " + e.getMessage());
                }

            }


        }


    }

    /**
     * method in charge of saving file ( photo, pdf, document) in firebase storage
     * @param fileToUpload
     * @param folderRef
     * @param fileExtension
     * @param messageType
     */
    private void saveFileInStorage(final Uri fileToUpload, final String folderRef, final String fileExtension, final String messageType ){

        Log.d(TAG, "saveFileInStorage: TRIGGERED!!!!!!!!!!!!!");
        // We create an Android storage instance called "photo_for_chat" in order to save the photos there.
        StorageReference storageFolderRef = FirebaseStorage.getInstance().getReference().child(folderRef);

        messagePushID = dbChatsNodeRef.child(currentUserID).child(contactID).push();

        String messagePushKey = messagePushID.getKey();

        //we store file inside "pdf_for_chat" folder and add extension ".pdf" to convert it into an pdf file.
        final StorageReference fileLocation = storageFolderRef.child(messagePushKey + fileExtension );
        // we upload file to the firebase storage using UploadTask
        uploadTask = fileLocation.putFile(fileToUpload);

        //lets check if image was uploaded correctly in the firebase storage service
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileLocation.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    // here we get the final image URI from storage
                    Uri fileUri = task.getResult();
                    String fileURLInFirebase = fileUri.toString();
                    Log.i(TAG, "onComplete: image url: " + fileURLInFirebase);
                    //this is for notification purposes
                    notify = true;
                    //upload message in db
                    //createChatListDB();
                    uploadMessageToDb(fileURLInFirebase, messageType );

                } else {
                    Toast.makeText(ChatActivity.this, "Error: " + task.getException().toString() , Toast.LENGTH_SHORT).show();
                }

            }
        });

    }



    /**
     * method in charge of uploading message (either text, image, or file type) in database
     *
     * @param messageInfo
     * @param messageType
     */
    private void uploadMessageToDb(String messageInfo, String messageType) {

        String lastMessageTime, lastMessageDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy");
        lastMessageDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        lastMessageTime = time.format(calendar.getTime());

        messagePushID = dbChatsNodeRef.child(currentUserID).child(contactID).push();

        //save unique message id
        String messagePushKey = messagePushID.getKey();

        //this map is for saving the details of the messages
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put(getString(R.string.message_db), messageInfo);
        messageDetails.put(getString(R.string.type_db), messageType);
        messageDetails.put(getString(R.string.sende_id_db), currentUserID);
        messageDetails.put(getString(R.string.receiver_id_db), contactID);
        messageDetails.put(getString(R.string.message_date_db), lastMessageDate);
        messageDetails.put(getString(R.string.message_time_db), lastMessageTime);
        messageDetails.put(getString(R.string.message_id_db), messagePushKey);
        messageDetails.put(getString(R.string.seen_db), false);

        //this map is for the info shown in the "Messages" node
        Map<String, Object> chatUsersInfo = new HashMap<>();
        chatUsersInfo.put(messagePushKey, messageDetails);

        dbChatsNodeRef.updateChildren(chatUsersInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: successfully");
                } else {
                    String error = task.getException().toString();
                    Log.i(TAG, "onComplete: error: " + error);
                }

            }
        });

        chatProgressBar.setVisibility(View.INVISIBLE);

        createChatListDB(currentUserID, contactID);
        sendNotification(messageInfo, messageType, messagePushKey);

    }

    /**
     * method creates chatroom in db to be retrieved in chatFragment and show as a list.
     */
    private void createChatListDB(final String currentUserID, final String contactID) {

        Log.d(TAG, "createChatListDB: TRIGEREEEEEEEEEEDDDDDDDDD create chat room in db");

        dbChatList.child(currentUserID).child(contactID).child("id")
                            .setValue(contactID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    dbChatList.child(contactID).child(currentUserID).child("id").setValue(currentUserID);

                }
            }
        });

    }

    private void retrofit() {
        apiService = RetrofitClient.getRetrofit().create(NotificationAPI.class);
    }


    /**
     * method is the one right before pushing the notification to the server
     * (here we fetch user information and set the message text to be shown in the recipient's notification)
     *
     * @param messageInfo
     * @param messageType
     */
    private void sendNotification(String messageInfo, String messageType, final String messageID) {

        //first we set the text message to be delivered depending on the type of message the user sends

        switch (messageType) {
            case "map":
                messageInfo = "Location";
                break;
            case "docx":
                messageInfo = "Document";
                break;
            case "pdf":
                messageInfo = "PDF";
                break;
            case "image":
                messageInfo = "Photo";
                break;
        }

        //here we have the message to be sent
        final String msg = messageInfo;

        dbUsersNodeRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    User user = dataSnapshot.getValue(User.class);
                    if (notify) {

                        pushNotificationToServer(contactID, user.getName(), msg, messageID, user.getImageThumbnail());
                        Log.d(TAG, "onDataChange SEND_NOTIFICATION: NOTIFICATION  SENT username " + user.getName());
                        Log.d(TAG, "onDataChange SEND_NOTIFICATION: NOTIFICATION  SENT contactID " + contactID);
                        Log.d(TAG, "onDataChange SEND_NOTIFICATION: NOTIFICATION  SENT message " + msg);
                    }
                    notify = false;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void pushNotificationToServer(final String contactID, final String name, final String msg,
                                            final String messageID, final String senderPhoto) {

        dbTokensNodeRef.child(contactID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Token deviceToken = dataSnapshot.getValue(Token.class);
                    Log.d(TAG, "onDataChange PUSH_NOTIFICATION_TO_SERVER: token retrieved from firebase: " + deviceToken.getToken());

                    Data data = new Data(contactID, msg, name, currentUserID, messageID, senderPhoto);
                    Log.d(TAG, "onDataChange PUSH_NOTIFICATION_TO_SERVER: message to be sent: " + data.getMessage());

                    PushNotification pushNotification = new PushNotification(data, deviceToken.getToken());

                    apiService.sendNotification(pushNotification)
                            .enqueue(new Callback<ResponseFCM>() {
                                @Override
                                public void onResponse(Call<ResponseFCM> call, Response<ResponseFCM> response) {
                                    if (response.code() == 200) {
                                        Log.d(TAG, "onResponse: RETROFIT notification  sent ");
                                    }

                                }

                                @Override
                                public void onFailure(Call<ResponseFCM> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        //  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    // ---------------------------------------------- Maps related ------------------------------  //

    /**
     * if GPS is enabled as soon as we open the chat room we call method getUsers()
     * IMPORTANT NOTE :this method contains getUserLastKnowLocation() which will check if
     * location permission for the app is granted, if not app wont get the user's
     * location as soon as we open the chat room, if location permission for the app is granted
     * the app will fetch the user's location as soon as we open the chat room.
     */
    private void checkLocationStatus() {

        Log.d(TAG, "checkLocationStatus: called as soon as the chat room is open");
        if (isGPSEnabled()) {
            Log.d(TAG, "checkLocationStatus: get details as soon as chat room is open");
            getUserDetails();
        }

    }

    private void shareLocationPressed() {
        Log.d(TAG, "shareLocationPressed: called when share location pressed in alert dialog");
        if (!isGPSEnabled()) {
            buildAlertMessageNoGps();
        } else if (locationPermissionGranted) {
            Log.d(TAG, "onClick: both GPS is enabled and location permission for the app granted ");
            chatProgressBar.setVisibility(View.INVISIBLE);
            notify = true;
            getUserDetails();
            uploadMessageToDb(getString(R.string.sharing_location), "map");

        } else {
            //if the app doesn't have the user permission we ask for it
            getLocationPermission();
        }

    }

    /**
     * method checks if gps is enabled on the device only
     *
     * @return
     */
    private boolean isGPSEnabled() {

        try {
            int gpsSignal = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (gpsSignal == 0) {
                Log.i(TAG, "openMapsOption: gps is OFF1");
                return false;
            } else {
                Log.i(TAG, "openMapsOption: gps is ON");
                return true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "openMapsOption: gps is OFF2");
            return false;
        }

    }

    /**
     * method pops up option to take the user to settings to turn gps on and off
     */
    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_location))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * location permission for the app
     */
    private boolean getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.d(TAG, "getLocationPermission: apps location permission granted");
            getUserDetails();
            return true;
        } else {
            Log.d(TAG, "getLocationPermission: gps permission for app pops pup ");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        return false;
    }

    /**
     * here we pass information from the user's document to the user's location document (firestore)
     */
    private void getUserDetails() {

        if (userLocation == null) {
            userLocation = new UserLocation();

            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        User user = task.getResult().toObject(User.class);
                        Log.d(TAG, "onComplete: user: " + user.getName());
                        userLocation.setUser(user);
                        getUserLastKnownLocation();

                    }
                }
            });
        } else {
            getUserLastKnownLocation();
        }
    }

    /**
     * method in charge of fetching user location (lat/long coordinates) using GPS on phone device
     * to later be saved in Firestore db.
     */
    private void getUserLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getUserLastKnowLocation: permissions not granted");
            return;
        }

        Log.i(TAG, "getUserLastKnowLocation: called");

        locationProvider.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    Log.d(TAG, "onComplete: location retrieved: " + location);
                    if (location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.i(TAG, "onComplete: saving in db latitude: " + location.getLatitude());
                        Log.i(TAG, "onComplete: saving in db  longitude: " + location.getLongitude());
                        userLocation.setGeo_point(geoPoint);
                        userLocation.setTimeStamp(null);
                        saveUserLocation();
                        startLocationService();

                    } else {
                        //  Toast.makeText(ChatActivity.this, "location retrieving null", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: db retrieving null again");
                    }
                }

            }
        });


    }

    /**
     * here we save user's coordinates in firestore db.
     */
    private void saveUserLocation() {

        Log.i(TAG, "saveUserLocation: saveUserLocation called.");
        if (userLocation != null) {

            userLocationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Log.d(TAG, "onComplete: done successfully");
                    }

                }
            });
        }

    }


    /**
     * this is for the local permission request (NOT our dialog alert, this one from android)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        locationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted Manually ");
                    getUserDetails();
                    //  uploadMessageToDb(getString(R.string.sharing_location), "map");
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_requiered), Toast.LENGTH_LONG).show();
                }
                break;
            //here we catch the result of reading external storage permission
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");

                    //here in this switch we manage to redirect the user to gallery or files depending on what the user requested
                    switch (optionSelected) {
                        case "photo":
                            Log.d(TAG, "onRequestPermissionsResult: option selected photo");
                            openOption(IMAGE_OPTION, SELECT_IMAGE, CHAT_IMAGE_MENU_REQUEST);
                            break;
                        case "pdf file":
                            Log.d(TAG, "onRequestPermissionsResult: option selected pdf file");
                            openOption(PDF_OPTION, SELECT_PDF, CHAT_PDF_MENU_REQUEST);
                            break;
                        case "word document":
                            Log.d(TAG, "onRequestPermissionsResult: option selected word document");
                            openOption(WORD_DOCUMENT_OPTION, SELECT_WORD_DOCUMENT, CHAT_DOCX_MENU_REQUEST);
                            break;
                    }
                }
                //if permission is rejected by the user
                else {
                    Toast.makeText(ChatActivity.this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show();
                }
                break;
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: something went wrong with the permissions");
                    Toast.makeText(this, "permission required", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }

    //  -------------------- init Location Service ----------------------------

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                ChatActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.alvar.chatapp.Service.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }



}

