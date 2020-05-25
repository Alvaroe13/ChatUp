package com.example.alvar.chatapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alvar.chatapp.Adapter.MessageAdapter;
import com.example.alvar.chatapp.Fragments.LocationFragment;
import com.example.alvar.chatapp.Model.Messages;
import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.Model.UserLocation;
import com.example.alvar.chatapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.alvar.chatapp.Constant.CHAT_DOCX_MENU_REQUEST;
import static com.example.alvar.chatapp.Constant.CHAT_IMAGE_MENU_REQUEST;
import static com.example.alvar.chatapp.Constant.CHAT_PDF_MENU_REQUEST;
import static com.example.alvar.chatapp.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Constant.CONTACT_IMAGE;
import static com.example.alvar.chatapp.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Constant.IMAGE_OPTION;
import static com.example.alvar.chatapp.Constant.LOCATION_CONTACT;
import static com.example.alvar.chatapp.Constant.LOCATION_USER;
import static com.example.alvar.chatapp.Constant.PDF_OPTION;
import static com.example.alvar.chatapp.Constant.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.alvar.chatapp.Constant.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.alvar.chatapp.Constant.SELECT_IMAGE;
import static com.example.alvar.chatapp.Constant.SELECT_PDF;
import static com.example.alvar.chatapp.Constant.SELECT_WORD_DOCUMENT;
import static com.example.alvar.chatapp.Constant.WORD_DOCUMENT_OPTION;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivityPage";

    //firebase services
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbChatsNodeRef, messagePushID, dbUsersNodeRef;
    private UploadTask uploadTask;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userLocationRef, userDocRef ;
    //UI elements
    private Toolbar toolbarChat;
    private RecyclerView recyclerViewChat;
    private EditText chatEditText;
    private ImageButton buttonSend, buttonAttachFile;
    private CircleImageView imageProfile, onlineIcon;
    private TextView usernameToolbarChat, lastSeenToolbarChat;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar chatProgresBar;
    //vars
    private String contactID, currentUserID;
    private String contactName, contactImage;
    private String messageText;
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


        initLocationProvider();

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

    }

    private void initLocationProvider() {
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
    }

    private void UIElements() {
        chatEditText = findViewById(R.id.chatEditText);
        buttonSend = findViewById(R.id.buttonChat);
        buttonAttachFile = findViewById(R.id.buttonAttachFile);
        chatProgresBar = findViewById(R.id.progressBarChat);
    }

    /**
     * this method receives de bundles from "ContactsActivity"
     */
    private void fetchInfoIntent() {

        if (getIntent() != null){
            contactID = getIntent().getStringExtra(CONTACT_ID);
            contactName = getIntent().getStringExtra(CONTACT_NAME);
            contactImage = getIntent().getStringExtra(CONTACT_IMAGE);
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
    }


    private void initFirestore() {

        //db
        mDb = FirebaseFirestore.getInstance();
        //docs ref
        userLocationRef = mDb.collection(getString(R.string.collection_user_location)).document(currentUserID);
        userDocRef = mDb.collection(getString(R.string.users_ref)).document(currentUserID);
    }


    /**
     * method will be the one fetching users location fro the DB
     * @param contactID
     */
    private void retrieveUsersLocationFromDB(final String contactID) {


        DocumentReference locationRefUser1 = mDb.collection(getString(R.string.collection_user_location))
                .document(currentUserID);

        locationRefUser1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                final  String locationUser1 = documentSnapshot.get("geo_point").toString();

                    Log.d(TAG, "onSuccess: location current user1 (user authenticated): " + locationUser1 );
                    retrieveOtherUserLocation( locationUser1 ,contactID);

                }else {
                    Log.d(TAG, "onSuccess: doc doesn't exist");
                }
            }

        });
    }

    /**
     * this methos retrieves contact's location
     * @param locationUser1
     * @param contactID
     */
    private void retrieveOtherUserLocation(final String locationUser1, final String contactID) {

        DocumentReference locationRefUser2 = mDb.collection(getString(R.string.collection_user_location))
                .document(contactID);

        locationRefUser2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                    final  String locationUser2 = documentSnapshot.get("geo_point").toString();
                    Log.d(TAG, "onSuccess: location current user2 (contact user in chat room): " + locationUser2 );

                    inflateLocationFragment(locationUser1, locationUser2);

                } else {
                    Log.d(TAG, "onSuccess: doc doesn't exist");
                }
            }
        });
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

        updateDateTime(getString(R.string.online_db));
        retrieveMessages();

    }

    /**
     * fetch messages in the chat room.
     */
    private void retrieveMessages() {
        dbChatsNodeRef.child(currentUserID).child(contactID)
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
        updateDateTime(getString(R.string.offline_db));
        typingState((getString(R.string.no_db)));
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
                    typingState( (getString(R.string.no_db) ) ) ;
                }
                //if edit text is not empty we set typing state as "yes"
                else {
                    typingState((getString(R.string.yes_db) ));
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
        typingStateMap.put(getString(R.string.typing_db), typingState);

        dbUsersNodeRef.child(currentUserID).child(getString(R.string.users_ref)).updateChildren(typingStateMap);

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
        CharSequence menuOptions[] = new CharSequence[]{getString(R.string.photo), getString(R.string.PDF), getString(R.string.Word_Document), getString(R.string.share_location)};
        // we set the options
        builder.setItems(menuOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {

                switch (option) {
                    case 0: //if user selected photo option in pop up window
                        // optionSelected = "photo";
                        openOption(IMAGE_OPTION, SELECT_IMAGE, CHAT_IMAGE_MENU_REQUEST);
                        break;
                    case 1: //if user selected pdf option in pop up window
                        // optionSelected = "pdf file";
                        openOption(PDF_OPTION, SELECT_PDF, CHAT_PDF_MENU_REQUEST);
                        break;
                    case 2: //if user selected word option in pop up window
                        // optionSelected = "word document";
                        openOption(WORD_DOCUMENT_OPTION, SELECT_WORD_DOCUMENT, CHAT_DOCX_MENU_REQUEST);
                        break;
                    case 3:
                        //optionSelected = "share location";
                        Log.i(TAG, "onClick: Share location option pressed ");
                        openMaps();
                        break;
                    default:
                        Log.d(TAG, "onClick: You didn't select any option");
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
                requestCode == CHAT_DOCX_MENU_REQUEST && resultCode == RESULT_OK && data != null) {

            //we store the file (image, pdf, word) selected in this var of URI type.
            try{
                file = data.getData();

            switch (requestCode) {
                case CHAT_IMAGE_MENU_REQUEST:
                    chatProgresBar.setVisibility(View.VISIBLE);
                    savePhotoInStorage(file);
                    Log.i(TAG, "onActivityResult: photo selected ready to upload in to firebase storage");
                    break;
                case CHAT_PDF_MENU_REQUEST:
                    chatProgresBar.setVisibility(View.VISIBLE);
                    savePDFInStorage(file);
                    Log.i(TAG, "onActivityResult: pdf file selected ready to upload in to firebase storage");
                    break;
                case CHAT_DOCX_MENU_REQUEST:
                    chatProgresBar.setVisibility(View.VISIBLE);
                    saveWordInStorage(file);
                    Log.i(TAG, "onActivityResult: word document selected ready to upload in to firebase storage");
                    break;
                default:
                    Log.i(TAG, "onActivityResult: nothing selected, something impossible happened");
            }

            }catch (NullPointerException e){
                e.printStackTrace();
                Log.d(TAG, "onActivityResult: error: " + e.getMessage());
            }
        }       //this switch is for image and doc options

        //this request code is for enabling the gps on the device
        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
                Log.i(TAG, "onActivityResult: share location");
                Toast.makeText(this, "testing", Toast.LENGTH_SHORT).show();
                getUserDetails();   //as soon as gps is enabled on the device we retrieve user's details
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

        messagePushID = dbChatsNodeRef.child(currentUserID).child(contactID).push();

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

        messagePushID = dbChatsNodeRef.child(currentUserID).child(contactID).push();

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

        messagePushID = dbChatsNodeRef.child(currentUserID).child(contactID).push();

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
                    uploadMessageToDb(imageURLInFirebase, getString(R.string.image_db));

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
        chatUsersInfo.put(messageSenderRef + "/" + messagePushKey, messageDetails);
        chatUsersInfo.put(messageReceiverRef + "/" + messagePushKey, messageDetails);

        dbChatsNodeRef.updateChildren(chatUsersInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
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

        chatProgresBar.setVisibility(View.INVISIBLE);
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


    // ---------------------------------------------- Maps permissions ------------------------------  //

    /**
     * first we check if GPS is enabled on device and if app has location permission granted.
     */
    private void openMaps() {
        //if gps is off
        if (!isGPSEnabled()) {
            buildAlertMessageNoGps();   //take user to settings
        } else {
            getLocationPermission();    //ig gps is on ask location permission for the app
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
            Log.d(TAG, "getLocationPermission: apps location permission granted");
            getUserDetails();
        } else {
            Log.d(TAG, "getLocationPermission: gps permission for app pops pup ");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                        getUserLastKnowLocation();

                    }
                }
            });
        } else {
            getUserLastKnowLocation();
        }
    }

    /**
     * method in charge of fetching user location (lat/long coordinates) using GPS on phone device
     * to later be saved in Firestore db.
     */
    private void getUserLastKnowLocation() {

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
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.i(TAG, "onComplete: saving in db latitude: " + location.getLatitude());
                    Log.i(TAG, "onComplete: saving in db  longitude: " + location.getLongitude());
                    userLocation.setGeo_point(geoPoint);
                    userLocation.setTimeStamp(null);
                    saveUserLocation();

                } else {
                    Log.d(TAG, "onComplete: error: " );
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

                        retrieveUsersLocationFromDB(contactID);


                    }

                }
            });
        }

    }

    private void inflateLocationFragment(String location1, String location2) {

        Log.d(TAG, "inflateLocationFragment: called");

       LocationFragment fragment =  LocationFragment.newInstance(location1, location2);
        Bundle data = new Bundle();
        data.putString(LOCATION_USER, location1 );
        data.putString(LOCATION_CONTACT, location2 );
        fragment.setArguments(data);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.layoutFrameID, fragment);
        transaction.commit();


    }

    /**
     * this is for the local permission request (NOT our dialog alert, this one from android)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        locationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                Log.d(TAG, "onRequestPermissionsResult: permission granted Manually ");
            } else {
                Toast.makeText(this, getString(R.string.location_permission_requiered), Toast.LENGTH_LONG).show();
            }
        }
    }


}
