package com.example.alvar.chatapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.MessageAdapter;
import com.example.alvar.chatapp.Dialogs.ChatOptionsDialog;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.UserClient;
import com.example.alvar.chatapp.Model.UserLocation;
import com.example.alvar.chatapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import java.util.concurrent.BlockingDeque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivityPage";

    //Const for options intent in Alert Dialog (image, pdf, word)
    public static final String IMAGE_OPTION = "image/*";
    public static final String SELECT_IMAGE = "SELECT IMAGE";
    public static final String PDF_OPTION = "application/pdf";
    public static final String SELECT_PDF = "SELECT PDF FILE";
    public static final String WORD_DOCUMENT_OPTION = "application/msword";
    public static final String SELECT_WORD_DOCUMENT = "SELECT WORD DOCUMENT";
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 555;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9004;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    // File request const
    private static final int CHAT_IMAGE_MENU_REQUEST = 1;
    private static final int CHAT_PDF_MENU_REQUEST = 2;
    private static final int CHAT_DOCX_MENU_REQUEST = 3;
    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbChatsRef, messagePushID, dbUsersNodeRef;
    private UploadTask uploadTask;
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
    //vars
    private String contactID, currentUserID;
    private String contactName, contactImage;
    private String messageText;
    private String optionSelected = "";
    private MessageAdapter adapter;
    private List<Messages> messagesList;
    private Uri file;
    private boolean locationPermissionGranted = false;
    private UserLocation userLocation;
    private FusedLocationProviderClient locationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fetchInfoIntent();
        initFirebase();
        initFirestore();
        setToolbar("", true);
        UIElements();
        initRecycleView();
        sendButtonPressed();
        editTextStatus();
        otherUserState();
        toolbarPressed();
        attachFileButtonPressed();
        initLocationProvider();


    }

    private void initLocationProvider() {
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
    }

    private void UIElements() {
        chatEditText = findViewById(R.id.chatEditText);
        buttonSend = findViewById(R.id.buttonChat);
        buttonAttachFile = findViewById(R.id.buttonAttachFile);
    }

    /**
     * this method receives de bundles from "ContactsActivity"
     */
    private void fetchInfoIntent() {
        contactID = getIntent().getStringExtra("contactID");
        contactName = getIntent().getStringExtra("contactName");
        contactImage = getIntent().getStringExtra("contactImage");
    }

    /**
     * init firebase services
     */
    private void initFirebase() {

        auth = FirebaseAuth.getInstance();
        //we get current user ID
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users");
        dbChatsRef = database.getReference().child("Chats").child("Messages");
    }


    private void initFirestore() {

        String userIdFirestore = FirebaseAuth.getInstance().getUid();

        mDb = FirebaseFirestore.getInstance();
        userLocationRef = mDb.collection(getString(R.string.collection_user_location)).document(userIdFirestore);
        userDocRef = mDb.collection("user").document(userIdFirestore);

    }

    /**
     * Create toolbar and inflate the custom bar chat bar layout
     */
    private void setToolbar(String title, Boolean backOption) {

        toolbarChat = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbarChat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(backOption);
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
        usernameToolbarChat.setText(contactName);
        if (contactImage.equals("imgThumbnail")) {
            imageProfile.setImageResource(R.drawable.profile_image);
        } else {
            Glide.with(getApplicationContext()).load(contactImage).into(imageProfile);
        }


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
                    String saveLastSeenDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    String saveLastSeenTime = dataSnapshot.child("userState").child("time").getValue().toString();
                    String saveSate = dataSnapshot.child("userState").child("state").getValue().toString();
                    //retrieving other user's typing state
                    String typingState = dataSnapshot.child("userState").child("typing").getValue().toString();

                    //if typing state in db is yes we should in toolbar that other user is typing
                    if (typingState.equals("yes")) {

                        lastSeenToolbarChat.setText(R.string.typing);

                    } else {
                        //if user is online but not typing we show online on the toolbar
                        if (saveSate.equals("Online")) {
                            lastSeenToolbarChat.setText(R.string.activeNow);
                            onlineIcon.setVisibility(View.VISIBLE);

                            //if user is not typing nor "online" we show "offline" on the toolbar.
                        } else if (saveSate.equals("Offline")) {
                            lastSeenToolbarChat.setText(getString(R.string.lastSeen) + " " + saveLastSeenDate + " " + saveLastSeenTime);
                            onlineIcon.setVisibility(View.INVISIBLE);
                        }

                    }


                } else {

                    Toast.makeText(ChatActivity.this, "Error with the network", Toast.LENGTH_SHORT).show();
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

                //we get message written by the user
                messageText = chatEditText.getText().toString();

                //if field is empty
                if (messageText.equals("")) {
                    //show toast to the user
                    Toast.makeText(ChatActivity.this,
                            getString(R.string.noEmptyFieldAllowed), Toast.LENGTH_SHORT).show();
                } else {
                    //otherwise we send the message
                    //sendMessage();
                    uploadMessageToDb(messageText, "text");

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

        updateDateTime("Online");
        retrieveMessages();

    }

    /**
     * fetch messages in the chat room.
     */
    private void retrieveMessages() {
        dbChatsRef.child(currentUserID).child(contactID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        messagesList.clear();

                        for (DataSnapshot info : dataSnapshot.getChildren()) {

                            Messages messages = info.getValue(Messages.class);

                            messagesList.add(messages);

                            adapter.notifyDataSetChanged();

                            recyclerViewChat.smoothScrollToPosition(recyclerViewChat.getAdapter().getItemCount());

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
        updateDateTime("Offline");
        typingState("no");
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
        userState.put("state", state);
        userState.put("date", currentDate);
        userState.put("time", currentTime);

        dbUsersNodeRef.child(currentUserID).child("userState").updateChildren(userState);

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
                    typingState("no");
                }
                //if edit text is not empty we set typing state as "yes"
                else {
                    typingState("yes");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * method oni charge of taking the user to other user's profile when toolbar pressed in the chat room
     */
    private void toolbarPressed() {

        toolbarChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentOtherUserProf = new Intent(ChatActivity.this, OtherUserProfileActivity.class);
                //we send user id through an intent
                intentOtherUserProf.putExtra("otherUserId", contactID);
                startActivity(intentOtherUserProf);
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
        typingStateMap.put("typing", typingState);

        dbUsersNodeRef.child(currentUserID).child("userState").updateChildren(typingStateMap);

    }

    /**
     * when attach file button is pressed
     */
    private void attachFileButtonPressed() {
        buttonAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertDialog();
                // showChatOptions();  work in rogress


            }
        });
    }

    //pending to be done, layout is not ready yet
    private void showChatOptions() {
        ChatOptionsDialog dialog = new ChatOptionsDialog();
        dialog.show(getSupportFragmentManager(), "showOptionsChat");
    }

    /**
     * here we show the option for the user to choose.
     */
    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(R.string.Choose_file);
        builder.setIcon(R.drawable.ic_add_circle);
        //options to be shown in the Alert Dialog
        CharSequence menuOptions[] = new CharSequence[]{getString(R.string.photo), getString(R.string.PDF), getString(R.string.Word_Document), "Share Location"};
        // we set the options
        builder.setItems(menuOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {

                switch (option) {
                    case 0: //if user selected photo option in pop up window
                        optionSelected = "photo";
                        openOption(IMAGE_OPTION, SELECT_IMAGE, CHAT_IMAGE_MENU_REQUEST);
                        break;
                    case 1: //if user selected pdf option in pop up window
                        optionSelected = "pdf file";
                        openOption(PDF_OPTION, SELECT_PDF, CHAT_PDF_MENU_REQUEST);
                        break;
                    case 2: //if user selected word option in pop up window
                        optionSelected = "word document";
                        openOption(WORD_DOCUMENT_OPTION, SELECT_WORD_DOCUMENT, CHAT_DOCX_MENU_REQUEST);
                        break;
                    case 3:
                        optionSelected = "share location";
                        Log.i(TAG, "onClick: Share location option pressed ");
                        openMaps();
                        break;
                    default:
                        Toast.makeText(ChatActivity.this, "You didn't select any option", Toast.LENGTH_SHORT).show();
                }


            }
        });

        builder.show();
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
                requestCode == CHAT_DOCX_MENU_REQUEST || requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {

            if (resultCode == RESULT_OK) {

                try {
                    //we store the file (image, pdf, word) selected in this var of URI type.
                    file = data.getData();
                } catch (NullPointerException e) {
                    Log.i(TAG, "onActivityResult: data is empty: " + e.getMessage());
                }

                switch (requestCode) {
                    case CHAT_IMAGE_MENU_REQUEST:
                        savePhotoInStorage(file);
                        Log.i(TAG, "onActivityResult: photo selected ready to upload in to firebase storage");
                        break;
                    case CHAT_PDF_MENU_REQUEST:
                        savePDFInStorage(file);
                        Log.i(TAG, "onActivityResult: pdf file selected ready to upload in to firebase storage");
                        break;
                    case CHAT_DOCX_MENU_REQUEST:
                        saveWordInStorage(file);
                        Log.i(TAG, "onActivityResult: word document selected ready to upload in to firebase storage");
                        break;
                    case PERMISSIONS_REQUEST_ENABLE_GPS:
                        if (locationPermissionGranted) {
                            //getUserDetails();  commented out for the time being while we solve the activity result issue (not retrieving the result)
                            Log.i(TAG, "onActivityResult: share location");
                            Intent i = new Intent(this, MainActivity.class);
                            startActivity(i);
                        }
                        Log.d(TAG, "onActivityResult: location not granted");
                        break;
                    default:
                        Log.i(TAG, "onActivityResult: nothing selected, something impossible happened");
                }
            }

        }

    }

    /**
     * method in charge of uploading pdf file selected by user into firebase storage
     *
     * @param file
     */
    private void savePDFInStorage(Uri file) {

        // We create an Android storage instance called "photo_for_chat" in order to save the photos there.
        StorageReference storageFolderRef = FirebaseStorage.getInstance().getReference().child("pdf_for_chat");

        messagePushID = dbChatsRef.child(currentUserID).child(contactID).push();

        String messagePushKey = messagePushID.getKey();

        //we store file inside "pdf_for_chat" folder and add extension ".pdf" to convert it into an pdf file.
        final StorageReference fileLocation = storageFolderRef.child(messagePushKey + ".pdf");
        // we upload file to the firebase storage using UploadTask
        uploadTask = fileLocation.putFile(file);

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
                    //we parse it to String type.
                    String fileURLInFirebase = fileUri.toString();
                    Log.i(TAG, "onComplete: image url: " + fileURLInFirebase);
                    //send message here
                    uploadMessageToDb(fileURLInFirebase, "pdf");

                } else {
                    String error = task.getException().toString();
                    Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * method in charge of uploading word file selected by user into firebase storage
     *
     * @param file
     */
    private void saveWordInStorage(Uri file) {

        // We create an Android storage instance called "photo_for_chat" in order to save the photos there.
        StorageReference storageFolderRef = FirebaseStorage.getInstance().getReference().child("word_docs_for_chat");

        messagePushID = dbChatsRef.child(currentUserID).child(contactID).push();

        String messagePushKey = messagePushID.getKey();

        //we store file inside "pdf_for_chat" folder and add extension ".pdf" to convert it into an pdf file.
        final StorageReference fileLocation = storageFolderRef.child(messagePushKey + ".docx");
        // we upload file to the firebase storage using UploadTask
        uploadTask = fileLocation.putFile(file);

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
                    //we parse it to String type.
                    String fileURLInFirebase = fileUri.toString();
                    Log.i(TAG, "onComplete: image url: " + fileURLInFirebase);
                    //send message here
                    uploadMessageToDb(fileURLInFirebase, "docx");

                } else {
                    String error = task.getException().toString();
                    Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * file in charge of uploading photo from device to firebase
     *
     * @param file
     */
    private void savePhotoInStorage(Uri file) {

        // We create an Android storage instance called "photo_for_chat" in order to save the photos there.
        StorageReference storageFolderRef = FirebaseStorage.getInstance().getReference().child("photo_for_chat");

        messagePushID = dbChatsRef.child(currentUserID).child(contactID).push();

        String messagePushKey = messagePushID.getKey();

        //we store picture inside "photo_for_chat" folder and add extension ".jpg" to convert it into an image file.
        final StorageReference fileLocation = storageFolderRef.child(messagePushKey + ".jpg");
        // we upload file to the firebase storage using UploadTask
        uploadTask = fileLocation.putFile(file);

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
                    Uri imageUri = task.getResult();
                    //we parse it to String type.
                    String imageURLInFirebase = imageUri.toString();
                    Log.i(TAG, "onComplete: image url: " + imageURLInFirebase);
                    //send message here
                    uploadMessageToDb(imageURLInFirebase, "image");

                } else {
                    String error = task.getException().toString();
                    Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: error");
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

        //first we create a ref for sender and receiver to be later saved in the db
        String messageSenderRef = currentUserID + "/" + contactID;
        String messageReceiverRef = contactID + "/" + currentUserID;

        String lastMessageTime, lastMessageDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy");
        lastMessageDate = date.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        lastMessageTime = time.format(calendar.getTime());

        messagePushID = dbChatsRef.child(currentUserID).child(contactID).push();

        //save unique message id
        String messagePushKey = messagePushID.getKey();

        //this map is for saving the details of the messages
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("message", messageInfo);
        messageDetails.put("type", messageType);
        messageDetails.put("senderID", currentUserID);
        messageDetails.put("receiverID", contactID);
        messageDetails.put("messageDate", lastMessageDate);
        messageDetails.put("messageTime", lastMessageTime);
        messageDetails.put("messageID", messagePushKey);
        messageDetails.put("seen", false);

        //this map is for the info shown in the "Messages" node
        Map<String, Object> chatUsersInfo = new HashMap<>();
        chatUsersInfo.put(messageSenderRef + "/" + messagePushKey, messageDetails);
        chatUsersInfo.put(messageReceiverRef + "/" + messagePushKey, messageDetails);

        dbChatsRef.updateChildren(chatUsersInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: successfully");
                } else {
                    String error = task.getException().toString();
                    Toast.makeText(ChatActivity.this, "error: " + error, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: error: " + error);
                }

            }
        });

        //we remove any text enter by the user once it's been sent
        chatEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


    // ---------------------------------------------- Maps permissions ------------------------------

    private void openMaps() {
        if (!isGPSEnabled()) {
            buildAlertMessageNoGps();
        } else {
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
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
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
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.i(TAG, "getLocationPermission: apps location permission granted");
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            Log.i(TAG, "getLocationPermission: apps location permission granted");
        }
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
                        //  ((UserClient)(getApplicationContext())).setUser(user);
                        getUserLastKnowLocation();

                    }
                }
            });
        } else {
            getUserLastKnowLocation();
        }
    }

    /**
     * method in charge of fetching user location (lat/long coordinates)
     */
    private void getUserLastKnowLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permissions not granted", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getUserLastKnowLocation: permissions not granted");
            buildAlertMessageNoGps();
            return;
        }
        Log.i(TAG, "getUserLastKnowLocation: called");
        locationProvider.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.i(TAG, "onComplete: latitude: " + location.getLatitude());
                    Log.i(TAG, "onComplete: longitude: " + location.getLongitude());
                    userLocation.setGeo_point(geoPoint);
                    userLocation.setTimeStamp(null);
                    saveUserLocation();

                }

            }
        });
    }

    /**
     * here we save users coordinates in firestore db.
     */
    private void saveUserLocation() {

        Log.i(TAG, "saveUserLocation: saveUserLocation called.");
        if (userLocation != null) {

            userLocationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onComplete: location inserted in the db (latitude): " + userLocation.getGeo_point().getLatitude());
                        Log.i(TAG, "onComplete: location inserted in the db (latitude): " + userLocation.getGeo_point().getLongitude());
                        Log.d(TAG, "onComplete name: " + userLocation.getUser().getName());
                        Log.d(TAG, "onComplete: email: " + userLocation.getUser().getEmail());
                        Log.d(TAG, "onComplete: password: " + userLocation.getUser().getPassword());
                    }

                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        locationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }

        }

    }



    /*

     */
/**
 * here we put in motion maps permissions check
 *
 * @return
 *//*

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                getUserDetails();
                return true;
            }
        }
        Log.i(TAG, "checkMapServices: maps is enabled");
        return false;
    }

    */
/**
 * method checks if google services is available in the phone to be able to use maps
 *
 * @return
 *//*

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ChatActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(ChatActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    */
/**
 * method in charge of checking if gps is enabled on the device
 *
 * @return
 *//*

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Log.i(TAG, "isMapsEnabled: gps it is enabled");

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "isMapsEnabled: gps not enabled");
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }
*/

}
