package com.example.alvar.chatapp.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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

    //ui
    private MapView mMapView;
    //vars
    private double lat1, lon1, lat2, lon2;
    private GoogleMap gMaps;
    private LatLng userCoordinates;


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
            Log.d(TAG, "onCreateView: location user: " + lat1 + " , " + lon1);
            Log.d(TAG, "onCreateView: location contact: " + lat2 + " , " + lon2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_location, container, false);
        mMapView = layout.findViewById(R.id.user_list_map);

        initGoogleMap(savedInstanceState);

         return layout;
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

        userCoordinates = new LatLng( lat1, lon2);
        CameraPosition camera = new CameraPosition.Builder()
                .target(userCoordinates)
                .zoom(12)           // zoom (max value = 21)
                .bearing(360)       //view angle horizontal (360ºc maximum)
                .tilt(0)            //view angle vertically
                .build();

        gMaps.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

    }


}
