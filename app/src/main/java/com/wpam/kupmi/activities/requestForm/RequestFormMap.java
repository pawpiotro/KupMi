package com.wpam.kupmi.activities.requestForm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wpam.kupmi.R;
import com.wpam.kupmi.lib.Constants;

import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;

public class RequestFormMap extends Fragment implements OnMapReadyCallback {

    // Private fields
    private static final String TAG = "REQUEST_FORM_MAP_FRAGMENT";

    private TextView coords;
    private GoogleMap map;

    private LatLng currentLatLng;

    private RequestFormActivity parentActivity;

    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "OnCreateView");
        currentLatLng = new LatLng(getArguments().getDouble(Constants.MAP_LAT), getArguments().getDouble(Constants.MAP_LON));
        return inflater.inflate(R.layout.fragment_request_form_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = (RequestFormActivity) getActivity();

        MapView mapView = (MapView) getView().findViewById(R.id.request_form_map_view);
        coords = (TextView) getView().findViewById(R.id.request_form_map_coords_textview);
        ImageButton next = (ImageButton) getView().findViewById(R.id.request_form_map_next_button);

        String tmp = currentLatLng.latitude + ", " + currentLatLng.longitude;
        coords.setText(tmp);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.setLocation(currentLatLng.latitude, currentLatLng.longitude);
                parentActivity.goToClock();
            }
        });
    }

    // Override OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");
        parentActivity.setBarVisible(false);
        map = googleMap;
        map.addMarker(new MarkerOptions().position(currentLatLng).title("Current location"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i(TAG, latLng.toString());
                currentLatLng = latLng;
                String tmp = latLng.latitude + ", " + latLng.longitude;
                coords.setText(tmp);
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng).title("Current location"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
            }
        });
    }
}
