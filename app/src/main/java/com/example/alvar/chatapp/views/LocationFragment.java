package com.example.alvar.chatapp.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.example.alvar.chatapp.Service.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import static com.example.alvar.chatapp.Utils.Constant.CONTACT_ID;
import static com.example.alvar.chatapp.Utils.Constant.CONTACT_NAME;
import static com.example.alvar.chatapp.Utils.Constant.LOCATION_CONTACT_LAT;
import static com.example.alvar.chatapp.Utils.Constant.LOCATION_CONTACT_LON;
import static com.example.alvar.chatapp.Utils.Constant.LOCATION_USER_LAT;
import static com.example.alvar.chatapp.Utils.Constant.LOCATION_USER_LON;
import static com.example.alvar.chatapp.Utils.Constant.MAPVIEW_BUNDLE_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "LocationFragment";
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;
    //Firestore
    private FirebaseFirestore mDb;
    private DocumentReference userLocationRef ;
    //ui
    private MapView mMapView;
    private ImageButton closeMapBtn;
    private TextView user1, user2;
    private View viewLayout;
    private ViewGroup viewGroup;
    //vars
    private String currentUserID, contactID, contactName;
    private double lat1, lon1, lat2, lon2;
    private GoogleMap gMaps;
    private LatLng userCoordinates, contactCoordinates;
    private MarkerOptions markerUser, markerContact;
    private Marker marker;


    public static LocationFragment newInstance(){
        return  new LocationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called");


        if (getArguments() != null){
            lat1 = getArguments().getDouble(LOCATION_USER_LAT);
            lon1 = getArguments().getDouble(LOCATION_USER_LON);
            lat2 = getArguments().getDouble(LOCATION_CONTACT_LAT);
            lon2 = getArguments().getDouble(LOCATION_CONTACT_LON);
            contactID = getArguments().getString(CONTACT_ID);
            contactName = getArguments().getString(CONTACT_NAME);
            Log.d(TAG, "onCreate: location user: " + lat1 + " , " + lon1);
            Log.d(TAG, "onCreate: location contact: " + lat2 + " , " + lon2);
            Log.d(TAG, "onCreateView: contactID: " + contactID);
        } else {
            Log.d(TAG, "onCreate: info null");
        }

        initFirebase();
        initFirestore();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        viewGroup = container;
         return  inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called");

        viewLayout = view;
        UI(view);
        user2.setText(contactName);
        closeMapBtn.setOnClickListener(this);
        initGoogleMap(savedInstanceState);
        locationState("On");
    }

    private void UI(View view) {
        mMapView = view.findViewById(R.id.user_list_map);
        closeMapBtn = view.findViewById(R.id.btn_close_map);
        user1 = view.findViewById(R.id.text1);
        user2 = view.findViewById(R.id.text2);
    }

    private void initFirebase(){
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
    }

    private void initFirestore(){
        //db
        mDb = FirebaseFirestore.getInstance();
        //docs ref
        userLocationRef = mDb.collection(getString(R.string.collection_user_location)).document(contactID);
    }

    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
        gMaps = map;
        setCameraView();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /**
     * here we set the camera view as soon as location windows pops up
     */
    private void setCameraView(){

        userCoordinates = new LatLng( lat1, lon1);
        CameraPosition camera = new CameraPosition.Builder()
                .target(userCoordinates)
                .zoom(13)           // zoom (max value = 21)
                .bearing(360)       //view angle horizontal (360ºc maximum)
                .tilt(0)            //view angle vertically
                .build();

        gMaps.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

        showMarkersOnMap();
        //gMaps.addMarker(markerUser());
        gMaps.addMarker(markerContact());

    }

    /**
     * method in charge of showing other user marker when using location fragment only
     */
    private void showMarkersOnMap() {


        dbUsersNodeRef.child(contactID).child((getString(R.string.user_state_db)))
                .child("location").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    try {
                        String locationState = dataSnapshot.getValue().toString();
                        if (locationState.equals("On")){
                            //this line only fetches location when location windows pops up
                            marker = gMaps.addMarker(markerContact());
                            updateMarkerLocation(marker);
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                        getString(R.string.other_user_location_off), Toast.LENGTH_SHORT).show();
                            gMaps.clear(); //remove marker from other user if is not in the location room
                        }
                    }catch (NullPointerException e) {
                        Log.d(TAG, "onDataChange: exception: " + e.getMessage());
                    }catch (IllegalStateException e){
                        Log.d(TAG, "onDataChange: exception: " + e.getMessage());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * this method updates in real time contact marker location
     * @param marker
     */
    private void updateMarkerLocation(final Marker marker) {

        userLocationRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){

                    GeoPoint value = documentSnapshot.getGeoPoint("geo_point");
                    double latContact = value.getLatitude();
                    double lonContact = value.getLongitude();
                    marker.setPosition(new LatLng(latContact, lonContact));
                    Log.d(TAG, "onEvent: location update in real time: " + value);

                }
            }
        });


    }

    /**
     * this method creates a custom marker for user authenticated
     * (I wont use it but wont delete it in case later I want to replace the default blue dot with a
     * custom marker)
     * @return
     */
    private MarkerOptions markerUser(){

            userCoordinates = new LatLng(lat1, lon1);
            markerUser = new MarkerOptions();
            markerUser.position(userCoordinates);
            markerUser.title("This is you");
            markerUser.draggable(false);
            markerUser.snippet("");
            markerUser.icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_icon1));

        return markerUser;
    }

    /**
     * this method creates a custom marker for contact in chat room
     * @return
     */
    private MarkerOptions markerContact(){

        contactCoordinates = new LatLng(lat2, lon2);
        markerContact = new MarkerOptions();
        markerContact.position(contactCoordinates);
        markerContact.title("Contact");
        markerContact.draggable(false);
        markerContact.snippet("Set route?");
        markerContact.icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_icon2));

        return markerContact;
    }

    /**
     * method updates in real time if user is in fragment MAp therefore wants to share current location
     * with other user
     */
    private void locationState(String locationState){

        HashMap<String, Object> userState = new HashMap<>();
        userState.put("location", locationState);

        dbUsersNodeRef.child(currentUserID).child("userState").updateChildren(userState);
    }

    /**
     * alert notifies user is about to stop sharing location when bin image is pressed in mapView
     */
    private void launchAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.close_location));
        builder.setIcon(R.drawable.ic_location);
        //options to be shown in the Alert Dialog
        builder.setMessage(getContext().getString(R.string.close_map_message));
        builder.setNegativeButton(getContext().getString(R.string.no), null);
        builder.setPositiveButton(getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    getActivity().onBackPressed();
                    stopService();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        builder.show();
    }

    /**
     * method stops service in charge of sharing user's location when app is not in the foreground
     */
    private void stopService(){
        Intent serviceIntent = new Intent(getContext(), LocationService.class);
        try {
            locationState("Off");
            getActivity().stopService(serviceIntent);
            Log.d(TAG, "onClick: service stop");
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View v) {
       //this handles the click to close the map window (unfinished)
        if (v.getId() == R.id.btn_close_map){
            Log.d(TAG, "onClick: close window button pressed");
            launchAlertDialog();
        }


    }
}
