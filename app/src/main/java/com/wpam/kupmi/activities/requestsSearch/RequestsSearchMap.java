package com.wpam.kupmi.activities.requestsSearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.wpam.kupmi.R;
import com.wpam.kupmi.activities.singleRequest.SingleRequestActivity;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.services.GetAddressCoordsIntentService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.wpam.kupmi.lib.Constants.DEF_RADIUS;
import static com.wpam.kupmi.lib.Constants.MAP_ZOOM;
import static com.wpam.kupmi.lib.Constants.MAX_RADIUS;
import static com.wpam.kupmi.lib.Constants.MIN_RADIUS;
import static com.wpam.kupmi.utils.CoordinatesUtils.getCoordsPair;
import static com.wpam.kupmi.utils.CoordinatesUtils.getLatLng;

public class RequestsSearchMap extends Fragment implements OnMapReadyCallback {

    // Private fields
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
    private GeoQueryEventListener locationRequestsQueryListener;
    private HashMap<String, LatLng> locationRequestsIds = new HashMap<>();

    private HashMap<RequestTag, List<Pair<String, String>>> tagsRequestsIds = new HashMap<>();

    private HashMap<String, Marker> requestsMapMarkers = new HashMap<>();

    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "OnCreateView");
        currentLatLng = new LatLng(getArguments().getDouble(Constants.MAP_LAT), getArguments().getDouble(Constants.MAP_LON));
        return inflater.inflate(R.layout.fragment_requests_search_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = (RequestsSearchActivity) getActivity();
        resultReceiver = new LocationResultReceiver(new Handler());

        MapView mapView = getView().findViewById(R.id.requests_search_map_view);
        tagsSpinner = getView().findViewById(R.id.requests_search_map_tag_selection);
        seekBar = getView().findViewById(R.id.requests_search_map_seekbar);
        searchView = getView().findViewById(R.id.requests_search_map_searchview);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentActivity,
                R.array.tags_filter_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(adapter);
        Object selectedItem = tagsSpinner.getSelectedItem();
        if (selectedItem != null)
            updateCurrentTag(RequestTag.getInstance(selectedItem.toString()));

        tagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, String.format("onItemSelected - pos: %d, id: %d.", position, id));
                RequestTag newTag = RequestTag.getInstance(
                        tagsSpinner.getSelectedItem().toString());
                updateCurrentTag(newTag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tagsSpinner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputMethodManager = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(tagsSpinner.getWindowToken(), 0);
                return false;
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        DatabaseManager dbManager = DatabaseManager.getInstance();

        if (locationRequestsQuery == null) {
            locationRequestsQueryListener = new LocationRequestsListener();
            locationRequestsQuery = dbManager.getLocationRequestsQuery(
                    getCoordsPair(currentLatLng), currentRadius / 1000, REQUEST_STATE,
                    locationRequestsQueryListener);
        } else {
            dbManager.addLocationRequestsListener(locationRequestsQuery,
                    locationRequestsQueryListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (locationRequestsQuery != null) {
            DatabaseManager.getInstance().removeLocationRequestsListeners(locationRequestsQuery);
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
                updateCircle(false);
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
                updateCircle(false);

                DatabaseManager.getInstance().updateGeoQueryLocation(locationRequestsQuery,
                        getCoordsPair(currentLatLng));
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String[] tmp = marker.getTitle().split(";");
                if(tmp.length < 3){
                    Toast.makeText(parentActivity, "Invalid request data.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                String requestUID = tmp[0];
                String tag = tmp[1];
                String requesterUID = tmp[2];

                if (requestUID != null && tag != null && requesterUID != null) {
                    Request request = new Request(requestUID);
                    request.setState(RequestState.ACTIVE);
                    request.setRequesterUID(requesterUID);
                    request.setTag(RequestTag.getInstance(tag));

                    Intent intent = new Intent(parentActivity, SingleRequestActivity.class);
                    intent.putExtra(Constants.REQUEST, request);
                    intent.putExtra(Constants.REQUEST_PARTIAL_DATA_FLAG, false);
                    intent.putExtra(Constants.USER_KIND_PARAM, RequestUserKind.SUPPLIER);
                    startActivity(intent);
                } else
                    Toast.makeText(parentActivity, "Request does not exist anymore", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    // Private methods
    private void updateCircle(boolean moveCamera) {
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
        if (moveCamera)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, MAP_ZOOM));
    }

    private Pair<String, String> passTagFilter(String requestUID) {
        Pair<String, String> result = null;

        for (Map.Entry<RequestTag, List<Pair<String, String>>> tagRequests : tagsRequestsIds.entrySet()) {
            List<Pair<String, String>> tagRequestsIds = tagRequests.getValue();
            if (tagRequestsIds != null) {
                for (Pair<String, String> tagRequestId : tagRequestsIds) {
                    if (tagRequestId.first.equals(requestUID)) {
                        result = new Pair<>(tagRequests.getKey().lowerCaseName(), tagRequestId.second);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private boolean passLocationFilter(String requestUID) {
        return locationRequestsIds.containsKey(requestUID);
    }

    private void putMarkerOnMap(String requestUid, String tag, String requesterUid, LatLng location) {
        if (requestUid != null && location != null && !requestsMapMarkers.containsKey(requestUid)) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(location));
            String markerInfo = requestUid + ";" + tag + ";" + requesterUid;
            marker.setTitle(markerInfo);
            requestsMapMarkers.put(requestUid, marker);
        }
    }

    private void updateMarkerOnMap(String requestUid, LatLng location) {
        if (requestUid != null && location != null && requestsMapMarkers.containsKey(requestUid)) {
            Marker requestMarker = requestsMapMarkers.get(requestUid);
            if (requestMarker != null)
                requestMarker.setPosition(location);
        }
    }

    private void removeMarkerFromMap(String requestUid) {
        if (requestUid != null && requestsMapMarkers.containsKey(requestUid)) {
            Marker requestMarker = requestsMapMarkers.get(requestUid);
            if (requestMarker != null)
                requestMarker.remove();
            requestsMapMarkers.remove(requestUid);
        }
    }

    private void removeTagMarkersFromMap(RequestTag tag) {
        if (tag != RequestTag.ALL) {
            if (tagsRequestsIds.containsKey(tag)) {
                List<Pair<String, String>> tagRequests = tagsRequestsIds.get(tag);

                if (tagRequests != null) {
                    for (Pair<String, String> request : tagRequests) {
                        removeMarkerFromMap(request.first);
                    }
                }
            }
        }
    }

    private void updateCurrentTag(RequestTag newTag) {
        if (currentTag == null || currentTag != newTag) {
            HashMap<RequestTag, ValueEventListener> tagsListeners = new HashMap<>();
            if (newTag == RequestTag.ALL) {
                for (RequestTag tag : RequestTag.ALL_TAGS) {
                    if (!tagsRequestsIds.containsKey(tag))
                        tagsListeners.put(tag, new TagsRequestsListener());
                }

                DatabaseManager.getInstance().getTagsRequestsQuery(tagsListeners, REQUEST_STATE);
            } else {
                if (currentTag == RequestTag.ALL) {
                    List<RequestTag> otherTags = new ArrayList<>(Arrays.asList(RequestTag.ALL_TAGS));
                    otherTags.remove(newTag);
                    for (RequestTag otherTag : otherTags) {
                        removeTagMarkersFromMap(otherTag);
                        tagsRequestsIds.remove(otherTag);
                    }
                } else {
                    if (currentTag != null) {
                        removeTagMarkersFromMap(currentTag);
                        tagsRequestsIds.remove(currentTag);
                    }

                    tagsListeners.put(newTag, new TagsRequestsListener());
                    DatabaseManager.getInstance().getTagsRequestsQuery(tagsListeners, REQUEST_STATE);
                }
            }
            currentTag = newTag;
        }
    }

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
            updateCircle(true);

            DatabaseManager.getInstance().updateGeoQueryLocation(locationRequestsQuery,
                    getCoordsPair(currentLatLng));
        }
    }

    private class LocationRequestsListener implements GeoQueryEventListener {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if (map != null) {
                LatLng latLng = getLatLng(location);
                if (!locationRequestsIds.containsKey(key))
                    locationRequestsIds.put(key, getLatLng(location));
                Pair<String, String> tmp = passTagFilter(key);
                if (tmp != null)
                    putMarkerOnMap(key, tmp.first, tmp.second,latLng);
            }
        }

        @Override
        public void onKeyExited(String key) {
            if (map != null) {
                locationRequestsIds.remove(key);
                removeMarkerFromMap(key);
            }
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            if (map != null) {
                LatLng latLng = getLatLng(location);
                locationRequestsIds.put(key, getLatLng(location));
                updateMarkerOnMap(key, latLng);
            }
        }

        @Override
        public void onGeoQueryReady() {
            Log.i(TAG, "LocationRequestsListener - onGeoQueryReady");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.e(TAG, "LocationRequestsListener - DatabaseError: " + error);
        }
    }

    private class TagsRequestsListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String key = Objects.requireNonNull(dataSnapshot.getRef().getParent()).getKey();
            if (key != null) {
                RequestTag tag = RequestTag.getInstance(key);
                if (tag != RequestTag.ALL) {
                    List<Pair<String, String>> tagRequests = new LinkedList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String requestUID = child.getKey();
                        String requesterUID = child.getValue(String.class);

                        if (requestUID != null && requesterUID != null &&
                                !Objects.equals(requesterUID, parentActivity.getUser().getUserUID())) {
                            tagRequests.add(Pair.create(requestUID, requesterUID));
                            if (passLocationFilter(requestUID))
                                putMarkerOnMap(requestUID, tag.lowerCaseName(), requesterUID ,locationRequestsIds.get(requestUID));
                        }
                    }

                    tagsRequestsIds.put(tag, tagRequests);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "TagsRequestsListener - onCancelled");
        }
    }

}
