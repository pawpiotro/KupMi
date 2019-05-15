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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.services.GetAddressCoordsIntentService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wpam.kupmi.lib.Constants.DEF_RADIUS;
import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;
import static com.wpam.kupmi.lib.Constants.MAX_RADIUS;
import static com.wpam.kupmi.lib.Constants.MIN_RADIUS;
import static com.wpam.kupmi.utils.CoordinatesUtils.getCoordsPair;
import static com.wpam.kupmi.utils.CoordinatesUtils.getLatLng;

public class RequestsSearchMap extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "REQUESTS_SEARCH_MAP_FRAGMENT";
    private static final int STROKE_COLOR = Color.RED;
    private static final int FILL_COLOR = Color.TRANSPARENT;
    private static RequestState REQUEST_STATE = RequestState.ACTIVE;

    private RequestsSearchActivity parentActivity;
    private LocationResultReceiver resultReceiver;

    private Spinner tagsSpinner;
    private SeekBar seekBar;
    private SearchView searchView;
    private GoogleMap map;

    private LatLng currentLatLng;
    private double currentRadius = DEF_RADIUS;
    private Circle currentCircle;
    private RequestTag currentTag;

    private GeoQuery locationRequestsQuery;
    private HashMap<String, LatLng> locationRequestsIds = new HashMap<>();

    private List<Query> tagsRequestsQueries = new ArrayList<>();
    private HashMap<RequestTag, List<String>> tagsRequestsIds = new HashMap<>();

    private HashMap<String, Marker> requestsMapMarkers = new HashMap<>();

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

    private class LocationRequestsListener implements GeoQueryEventListener {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if (map != null)
            {
                LatLng latLng = getLatLng(location);
                locationRequestsIds.put(key, getLatLng(location));
                if (passTagFilter(key))
                    putMarkerOnMap(key, latLng);
            }
        }

        @Override
        public void onKeyExited(String key) {
            if (map != null)
            {
                locationRequestsIds.remove(key);
                removeMarkerFromMap(key);
            }
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            if (map != null)
            {
                LatLng latLng = getLatLng(location);
                locationRequestsIds.put(key, getLatLng(location));
                updateMarkerOnMap(key, latLng);
            }
        }

        @Override
        public void onGeoQueryReady() {
            Log.e(TAG, "LocationRequestsListener - onGeoQueryReady");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.e(TAG, "LocationRequestsListener - DatabaseError: " + error);
        }
    }

    private class TagsRequestsListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "Datasnapshot ref - " + dataSnapshot.getRef().getKey());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "TagsRequestsListener - onCancelled");
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
        resultReceiver = new LocationResultReceiver(new Handler());

        MapView mapView = (MapView) getView().findViewById(R.id.requests_search_map_view);
        tagsSpinner = (Spinner) getView().findViewById(R.id.requests_search_map_tag_selection);
        seekBar = (SeekBar) getView().findViewById(R.id.requests_search_map_seekbar);
        searchView = (SearchView) getView().findViewById(R.id.requests_search_map_searchview);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentActivity,
                R.array.tags_filter_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(adapter);
        Object selectedItem = tagsSpinner.getSelectedItem();
        if (selectedItem != null)
            currentTag = RequestTag.getInstance(selectedItem.toString());

        tagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                /*RequestTag newTag = RequestTag.getInstance(
                        tagsSpinner.getItemAtPosition(position).toString());
                if (currentTag != newTag)
                {
                    HashMap<RequestTag, ValueEventListener> tags = new HashMap<>();
                    DatabaseManager.getInstance().getTagsRequestsQuery()
                    currentTag = newTag;
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        locationRequestsQuery = DatabaseManager.getInstance().getLocationRequestsQuery(
                getCoordsPair(currentLatLng), currentRadius / 1000,
                new LocationRequestsListener());
    }

    @Override
    public void onStop() {
        super.onStop();

        if (locationRequestsQuery != null) {
            DatabaseManager.getInstance().removeLocationRequestsListener(locationRequestsQuery);
            locationRequestsQuery = null;
        }
    }

    // Override OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map ready");
        parentActivity.setBarVisible(false);
        map = googleMap;
        if (currentLatLng != null)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                currentRadius = progressValue * (MAX_RADIUS - MIN_RADIUS) / 100 + MIN_RADIUS;
                Log.i(TAG, Double.toString(currentRadius));
                updateCircle();
                DatabaseManager.getInstance().updateGeoQueryRadius(locationRequestsQuery,
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
                updateCircle();

                DatabaseManager.getInstance().updateGeoQueryLocation(locationRequestsQuery,
                        getCoordsPair(currentLatLng));
            }
        });
    }

    private void updateCircle() {
        if (map == null)
            return;

        if (currentCircle == null) {
            seekBar.setVisibility(View.VISIBLE);
            seekBar.setProgress((int) ((100 * DEF_RADIUS + MIN_RADIUS) / (MAX_RADIUS - MIN_RADIUS)));
        }

        if (currentCircle != null)
            currentCircle.remove();
        currentCircle = map.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(currentRadius)
                .strokeColor(STROKE_COLOR)
                .fillColor(FILL_COLOR));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));
    }

    private boolean passTagFilter(String requestUid)
    {
        boolean result = false;

        for (Map.Entry<RequestTag, List<String>> tagRequests: tagsRequestsIds.entrySet())
        {
            List<String> tagRequestsIds = tagRequests.getValue();
            if (tagRequestsIds != null && tagRequestsIds.contains(requestUid))
            {
                result = true;
                break;
            }
        }

        return result;
    }

    private void putMarkerOnMap(String requestUid, LatLng location)
    {
        if (requestUid != null && location != null)
            requestsMapMarkers.put(requestUid, map.addMarker(new MarkerOptions()
                .position(location)));
    }

    private void updateMarkerOnMap(String requestUid, LatLng location)
    {
        if (requestUid != null && location != null && requestsMapMarkers.containsKey(requestUid))
        {
            Marker requestMarker = requestsMapMarkers.get(requestUid);
            if (requestMarker != null)
                requestMarker.setPosition(location);
        }
    }

    private void removeMarkerFromMap(String requestUid)
    {
        if (requestUid != null && requestsMapMarkers.containsKey(requestUid))
        {
            Marker requestMarker = requestsMapMarkers.get(requestUid);
            if (requestMarker != null)
                requestMarker.remove();
        }
    }

    protected void startIntentService(String strAddress) {
        Log.i(TAG, strAddress);
        Intent intent = new Intent(parentActivity, GetAddressCoordsIntentService.class);
        intent.putExtra(Constants.GET_ADDRESS_RECEIVER, resultReceiver);
        intent.putExtra(Constants.ADDRESS_DATA_EXTRA, strAddress);
        parentActivity.startService(intent);
    }

}
