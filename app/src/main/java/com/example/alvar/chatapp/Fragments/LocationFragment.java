package com.example.alvar.chatapp.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alvar.chatapp.R;
import com.google.android.gms.maps.MapView;

import static com.example.alvar.chatapp.Constant.LOCATION_CONTACT;
import static com.example.alvar.chatapp.Constant.LOCATION_USER;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment {

    private static final String TAG = "LocationFragment";



    //vars
    private String location1Fetched, location2Fetched;

    MapView mMapView;


    public static LocationFragment newInstance(String location1, String location2){
        return  new LocationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            location1Fetched = getArguments().getString(LOCATION_USER);
            location2Fetched = getArguments().getString(LOCATION_CONTACT);

            Log.d(TAG, "onCreateView: location user: " + location1Fetched);
            Log.d(TAG, "onCreateView: location contact: " + location2Fetched);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_location, container, false);

        mMapView = v.findViewById(R.id.user_list_map);



         return v;
    }
}
