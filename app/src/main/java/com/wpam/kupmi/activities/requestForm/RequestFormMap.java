package com.wpam.kupmi.activities.requestForm;

import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wpam.kupmi.R;
import com.wpam.kupmi.activities.requestsSearch.RequestsSearchMap;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.services.FetchAddressIntentService;
import com.wpam.kupmi.services.GetAddressCoordsIntentService;

import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;
import static com.wpam.kupmi.utils.CoordinatesUtils.getCoordsPair;

public class RequestFormMap extends Fragment implements OnMapReadyCallback {

    // Private fields
    private static final String TAG = "REQUEST_FORM_MAP_FRAGMENT";

    private SearchView searchView;
    private GoogleMap map;

    private LatLng currentLatLng;

    private RequestFormActivity parentActivity;
    private LocationResultReceiver resultReceiver;

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
        resultReceiver = new LocationResultReceiver(new Handler());

        MapView mapView = (MapView) getView().findViewById(R.id.request_form_map_view);
        searchView = (SearchView) getView().findViewById(R.id.requests_form_map_searchview);
        ImageButton next = (ImageButton) getView().findViewById(R.id.request_form_map_next_button);

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
        updateMarkerOnMap(true);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i(TAG, latLng.toString());
                currentLatLng = latLng;
                updateMarkerOnMap(false);
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
    }

    private void updateMarkerOnMap(boolean moveCamera){
        map.clear();
        map.addMarker(new MarkerOptions().position(currentLatLng));
        if(moveCamera)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));
    }

    // Private methods
    private void startIntentService(String strAddress) {
        Log.i(TAG, strAddress);
        Intent intent = new Intent(parentActivity, GetAddressCoordsIntentService.class);
        intent.putExtra(Constants.GET_ADDRESS_RECEIVER, resultReceiver);
        intent.putExtra(Constants.ADDRESS_DATA_EXTRA, strAddress);
        parentActivity.startService(intent);
    }

    // Private classes
    private class LocationResultReceiver extends ResultReceiver {
        LocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            parentActivity.setBarVisible(false);

            if (resultData == null)
                return;

            if (resultCode == Constants.FAILURE_RESULT) {
                Log.w(TAG, "Location not found");
                Toast.makeText(parentActivity, "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }

            double lat = resultData.getDouble(Constants.GET_ADDRESS_RESULT_DATA_KEY_LAT);
            double lon = resultData.getDouble(Constants.GET_ADDRESS_RESULT_DATA_KEY_LON);

            Log.i(TAG, Double.toString(lat));
            Log.i(TAG, Double.toString(lon));

            currentLatLng = new LatLng(lat, lon);
            updateMarkerOnMap(true);
        }
    }
}
