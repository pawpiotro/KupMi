package com.wpam.kupmi.activities.requestsSearch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.wpam.kupmi.R;
import com.wpam.kupmi.activities.requestForm.RequestFormActivity;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.services.GetAddressCoordsIntentService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.wpam.kupmi.lib.Constants.DEF_RADIUS;
import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;
import static com.wpam.kupmi.lib.Constants.MAX_RADIUS;
import static com.wpam.kupmi.lib.Constants.MIN_RADIUS;
import static com.wpam.kupmi.utils.CoordinatesUtils.getCoordsPair;
import static com.wpam.kupmi.utils.CoordinatesUtils.getLatLng;

public class RequestsSearchMap extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "REQUESTS_SEARCH_MAP_FRAGMENT";
    private static final int strokeColor = Color.RED;
    private static final int fillColor = Color.TRANSPARENT;

    private RequestsSearchActivity parentActivity;
    private LocationResultReceiver resultReceiver;

    private TextView coords;
    private SeekBar seekBar;
    private SearchView searchView;
    private GoogleMap map;

    private LatLng currentLatLng;
    private Circle currentCircle;
    private double currentRadius = DEF_RADIUS;

    // Do testów
    private List<String> currentTags = Arrays.asList("lol1", "lol2");

    private GeoQuery requestsLocationsQuery;
    private List<String> requestsLocationsIds = new ArrayList<>();

    private Query requestsTagsQuery;
    private List<String> requestsTagsIds = new ArrayList<>();

    private HashMap<String, Marker> requestsMapMarkers = new HashMap<>();


    class LocationResultReceiver extends ResultReceiver {
        LocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            parentActivity.setBarVisible(false);

            if (resultData == null)
                return;

            if(resultCode == Constants.FAILURE_RESULT){
                Log.w(TAG, "Location not found");
                return;
            }

            double lat = resultData.getDouble(Constants.GET_ADDRESS_RESULT_DATA_KEY_LAT);
            double lon = resultData.getDouble(Constants.GET_ADDRESS_RESULT_DATA_KEY_LON);

            Log.i(TAG, Double.toString(lat));
            Log.i(TAG, Double.toString(lon));

            currentLatLng = new LatLng(lat, lon);
            updateCircle();
        }
    }

    private class RequestsLocationsListener implements GeoQueryEventListener {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if (map != null)
            {
                requestsMapMarkers.put(key, map.addMarker(new MarkerOptions()
                    .position(getLatLng(location))));
            }
        }

        @Override
        public void onKeyExited(String key) {
            if (map != null)
            {
                if (requestsMapMarkers.containsKey(key))
                {
                    Marker requestMarker = requestsMapMarkers.get(key);
                    if (requestMarker != null)
                        requestMarker.remove();
                }
            }
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            if (map != null)
            {
                if (requestsMapMarkers.containsKey(key))
                {
                    Marker requestMarker = requestsMapMarkers.get(key);
                    if (requestMarker != null)
                        requestMarker.setPosition(getLatLng(location));
                }
            }
        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.e(TAG, "DatabaseError: " + error);
        }
    }

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
        parentActivity.setBarVisible(false);
        resultReceiver = new LocationResultReceiver(new Handler());

        MapView mapView = (MapView) getView().findViewById(R.id.requests_search_map_view);
        coords = (TextView) getView().findViewById(R.id.requests_search_map_coords_textview);
        seekBar = (SeekBar) getView().findViewById(R.id.requests_search_map_seekbar);
        searchView = (SearchView) getView().findViewById(R.id.requests_search_map_searchview);

        String tmp = currentLatLng.latitude + ", " + currentLatLng.longitude;
        coords.setText(tmp);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        requestsLocationsQuery = DatabaseManager.getInstance().getRequestsLocationsQuery(
                getCoordsPair(currentLatLng), currentRadius / 1000,
                new RequestsLocationsListener());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (requestsLocationsQuery != null)
        {
            DatabaseManager.getInstance().removeRequestsLocationsListeners(requestsLocationsQuery);
            requestsLocationsQuery = null;
        }
    }

    // Override OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");
        map = googleMap;
        if(currentLatLng != null)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                currentRadius = progressValue * (MAX_RADIUS - MIN_RADIUS) / 100 + MIN_RADIUS;
                Log.i(TAG, Double.toString(currentRadius));
                updateCircle();
                DatabaseManager.getInstance().updateRadiusRequestsLocationsQuery(requestsLocationsQuery,
                       currentRadius / 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                parentActivity.setBarVisible(true);
                startIntentService(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                currentLatLng = latLng;

                Log.i(TAG, currentLatLng.toString());
                String tmp = latLng.latitude + ", " + latLng.longitude;
                coords.setText(tmp);

                updateCircle();

                DatabaseManager.getInstance().updateLocationRequestsLocationsQuery(requestsLocationsQuery,
                        getCoordsPair(currentLatLng));
            }
        });
    }

    private void updateCircle(){
        if(map == null)
            return;

        if (currentCircle == null) {
            seekBar.setVisibility(View.VISIBLE);
            seekBar.setProgress((int)((100 * DEF_RADIUS + MIN_RADIUS) / (MAX_RADIUS - MIN_RADIUS)));
        }

        if (currentCircle != null)
            currentCircle.remove();
        currentCircle = map.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(currentRadius)
                .strokeColor(strokeColor)
                .fillColor(fillColor));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));
    }

    protected void startIntentService(String strAddress) {
        Log.i(TAG, strAddress);
        Intent intent = new Intent(parentActivity, GetAddressCoordsIntentService.class);
        intent.putExtra(Constants.GET_ADDRESS_RECEIVER, resultReceiver);
        intent.putExtra(Constants.ADDRESS_DATA_EXTRA, strAddress);
        parentActivity.startService(intent);
    }

}
