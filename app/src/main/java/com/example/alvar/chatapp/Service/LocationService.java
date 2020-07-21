package com.example.alvar.chatapp.Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.alvar.chatapp.Model.User;
import com.example.alvar.chatapp.Model.UserLocation;
import com.example.alvar.chatapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    private String currentUserID;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userLocationRef;
    //Firebase
    private FirebaseAuth mAtuh;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: called");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        showNotification();
    }

    /**
     * this one shows the push notification
     */
    private void showNotification() {

        if (Build.VERSION.SDK_INT >= 26) {

            Log.d(TAG, "showNotification: notification shown");

            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.sharing_current_location))
                    .setContentText(getString(R.string.updating_location))
                    .setSmallIcon(R.drawable.icon_location_notification)
                    .setColor(getColor(R.color.color_blue_light))
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    /**
     * method retrieves user's location every 4 secs
     */
    private void getLocation() {

        Log.d(TAG, "getLocation: entered in here ");

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");
                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            retrieveUserLocation(location);
                        }
                    }
                },

        Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    /**
     * here we fetch user's info from the users doc in firestore to be later updated in users
     * location doc
     * @param location
     */
    private void retrieveUserLocation(final Location location) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            currentUserID = FirebaseAuth.getInstance().getUid();

            Log.d(TAG, "retrieveUserLocation: current user ID: " + currentUserID);

            if (currentUserID != null) {
                initFirebase();

                mDb = FirebaseFirestore.getInstance();
                userLocationRef = mDb.collection(getString(R.string.users_ref)).document(currentUserID);

                userLocationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        UserLocation userLocation = new UserLocation(user, geoPoint, null);
                        Log.d(TAG, "onSuccess: geopoint info: lat: " + location.getLatitude());
                        Log.d(TAG, "onSuccess: geopoint info: lon: " + location.getLongitude());

                        checkLocationState(userLocation);

                    }
                });
            }
        } else {
            //if user logged out stop service
            stopSelf();
        }


    }

    /**
     * saved location updated every 4 secs in firestore
     * @param userLocation
     */
    private void saveUserLocation(final UserLocation userLocation) {

        try {
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_user_location))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: \n inserted user location into database." +
                                "\n latitude: " + userLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + userLocation.getGeo_point().getLongitude());
                    }
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: " + e.getMessage());
            stopSelf();
        }

    }


    //--------------- testing------------- //

    /**
     * method updates in real time if user is in fragment MAp therefore wants to share current location
     * with other user
     */
    private void initFirebase() {
        mAtuh = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child("Users").child(currentUserID).child("userState");
    }


    /**
     * this method stops saving user's location in db when user decides to stop sharing location
     * @param userLocation
     */
    private void checkLocationState(final UserLocation userLocation) {

        Log.d(TAG, "checkLocationState: entered here");

        dbUsersNodeRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String sharingLocationState = dataSnapshot.getValue().toString();
                    Log.d(TAG, "onDataChange: sharingLocationState: " + sharingLocationState);

                    if (sharingLocationState.equals("On")) {
                        Log.d(TAG, "onSuccess: sharingLocationState called: it's on ");
                        saveUserLocation(userLocation);
                    } else {
                        Log.d(TAG, "onSuccess: sharingLocationState called: it's off");
                        stopSelf();
                        Thread.interrupted();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}