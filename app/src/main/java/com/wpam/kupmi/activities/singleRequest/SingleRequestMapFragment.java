package com.wpam.kupmi.activities.singleRequest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.model.RequestState;

import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;

public class SingleRequestMapFragment extends Fragment implements OnMapReadyCallback {

    // Private fields
    private static final String TAG = "SINGLE_REQUEST_MAP_FRAGMENT";
    private GoogleMap map;
    private SingleRequestActivity parentActivity;
    private LatLng currentLatLng;

    private ProgressBar bar;

    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "OnCreateView");
        return inflater.inflate(R.layout.fragment_single_request_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = (SingleRequestActivity) getActivity();


        bar = getView().findViewById(R.id.single_request_map_progress_bar);
        setBarVisible(true);
        MapView mapView = getView().findViewById(R.id.single_request_map_view);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }


    // Override OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");
        setBarVisible(false);
        map = googleMap;

        DatabaseManager dbManager = DatabaseManager.getInstance();
        String requestUID = parentActivity.getRequest().getRequestUID();
        RequestState state = parentActivity.getRequest().getState();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if(location != null) {
                    currentLatLng = new LatLng(location.latitude, location.longitude);

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));
                    map.addMarker((new MarkerOptions().position(currentLatLng)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO:
            }
        };


        if(state != null && requestUID != null){
            dbManager.getRequestLocationQuery(requestUID, state, locationCallback);
        }


    }


    private void setBarVisible(boolean b) {
        if (bar == null)
            return;
        if (b) {
            bar.bringToFront();
            bar.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
        }
    }
}
