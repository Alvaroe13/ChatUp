package com.example.alvar.chatapp.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.alvar.chatapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import static com.example.alvar.chatapp.Constant.LOCATION_CONTACT_LAT;
import static com.example.alvar.chatapp.Constant.LOCATION_CONTACT_LON;
import static com.example.alvar.chatapp.Constant.LOCATION_USER_LAT;
import static com.example.alvar.chatapp.Constant.LOCATION_USER_LON;
import static com.example.alvar.chatapp.Constant.MAPVIEW_BUNDLE_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "LocationFragment";
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbUsersNodeRef;

    //ui
    private MapView mMapView;
    //vars
    private String currentUserID, contactID ;
    private double lat1, lon1, lat2, lon2;
    private GoogleMap gMaps;
    private LatLng userCoordinates, contactCoordinates;
    private MarkerOptions markerUser, markerContact;


    public static LocationFragment newInstance(){
        return  new LocationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            lat1 = getArguments().getDouble(LOCATION_USER_LAT);
            lon1 = getArguments().getDouble(LOCATION_USER_LON);
            lat2 = getArguments().getDouble(LOCATION_CONTACT_LAT);
            lon2 = getArguments().getDouble(LOCATION_CONTACT_LON);
            contactID = getArguments().getString("contactID");
            Log.d(TAG, "onCreate: location user: " + lat1 + " , " + lon1);
            Log.d(TAG, "onCreate: location contact: " + lat2 + " , " + lon2);
            Log.d(TAG, "onCreateView: contactID: " + contactID);
        } else {
            Toast.makeText(getActivity(), "info null", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_location, container, false);
        mMapView = layout.findViewById(R.id.user_list_map);

        initGoogleMap(savedInstanceState);
        initFirebase();
        locationState();

         return layout;
    }

    private void initFirebase(){
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbUsersNodeRef = database.getReference().child(getString(R.string.users_ref));
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
                .zoom(14)           // zoom (max value = 21)
                .bearing(360)       //view angle horizontal (360ºc maximum)
                .tilt(0)            //view angle vertically
                .build();

        gMaps.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

        showMarkersOnMap();
      //gMaps.addMarker(markerUser());
      //gMaps.addMarker(markerContact());

    }

    /**
     * method in charge of showing other user marker when using location fragment only
     */
    private void showMarkersOnMap() {
        dbUsersNodeRef.child(contactID).child((getString(R.string.user_state_db)))
                .child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    try {
                        String locationState = dataSnapshot.getValue().toString();
                        if (locationState.equals("On")){
                            gMaps.addMarker(markerContact());
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
     * this method creates a custom marker for user authenticated
     * @return
     */
    private MarkerOptions markerUser(){

            userCoordinates = new LatLng(lat1, lon1);
            markerUser = new MarkerOptions();
            markerUser.position(userCoordinates);
            markerUser.title("this is you");
            markerUser.draggable(false);
            markerUser.snippet("");
            markerUser.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation));

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
        markerContact.snippet("Set route to contact?");
        markerContact.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.star_on));

        return markerContact;
    }

    /**
     * method updates in real time if user is in fragment MAp therefore wants to share current location
     * with other user
     */
    private void locationState(){

        HashMap<String, Object> userState = new HashMap<>();
        userState.put("location", "On");

        dbUsersNodeRef.child(currentUserID).child((getString(R.string.user_state_db))).updateChildren(userState);
    }





}
