package com.wpam.kupmi.activities.requestsSearch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wpam.kupmi.R;

import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;

public class RequestsSearchMap extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "REQUESTS_SEARCH_MAP_FRAGMENT";
    private static final double DEF_RADIUS = 100.0;
    private static final double MAX_RADIUS = 1000.0;
    private static final double MIN_RADIUS = 10.0;


    private RequestsSearchActivity parentActivity;

    private TextView coords;
    private SeekBar seekBar;
    private GoogleMap map;

    private LatLng currentLatLng;
    private Circle currentCircle;
    private double currentRadius;

    // Override Fragment
    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "OnCreateView");
        currentLatLng = new LatLng(getArguments().getDouble("lat"), getArguments().getDouble("lon"));
        return inflater.inflate(R.layout.fragment_requests_search_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = (RequestsSearchActivity) getActivity();

        MapView mapView = (MapView) getView().findViewById(R.id.requests_search_map_view);
        coords = (TextView) getView().findViewById(R.id.requests_search_map_coords_textview);
        seekBar = (SeekBar) getView().findViewById(R.id.requests_search_seekbar);

        String tmp = currentLatLng.latitude + ", " + currentLatLng.longitude;
        coords.setText(tmp);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    // Override OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");
        map = googleMap;
        currentRadius = DEF_RADIUS;
        final int strokeColor = Color.RED;
        final int fillColor = Color.TRANSPARENT;//valueOf(1.0f, 0.0f, 0.0f, 0.5f);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                currentRadius = progressValue*(MAX_RADIUS-MIN_RADIUS)/100 + MIN_RADIUS;
                Log.i(TAG, Double.toString(currentRadius));

                Log.i(TAG, "DRAWING CIRCLE");
                map.clear();
                currentCircle = map.addCircle(new CircleOptions()
                        .center(currentLatLng)
                        .radius(currentRadius)
                        .strokeColor(strokeColor)
                        .fillColor(fillColor));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(currentCircle == null){
                    seekBar.setVisibility(View.VISIBLE);
                    seekBar.setProgress((int)((100*DEF_RADIUS + MIN_RADIUS) / (MAX_RADIUS - MIN_RADIUS)));
                }

                currentLatLng = latLng;

                Log.i(TAG, currentLatLng.toString());
                String tmp = latLng.latitude + ", " + latLng.longitude;
                coords.setText(tmp);

                map.clear();
                currentCircle = map.addCircle(new CircleOptions()
                        .center(currentLatLng)
                        .radius(currentRadius)
                        .strokeColor(strokeColor)
                        .fillColor(fillColor));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
            }
        });
    }

}
