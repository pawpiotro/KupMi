package com.wpam.kupmi.activities.requestsSearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.wpam.kupmi.R;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.services.FetchAddressIntentService;

import static com.wpam.kupmi.lib.Constants.FASTEST_INTERVAL;
import static com.wpam.kupmi.lib.Constants.UPDATE_INTERVAL;
import static com.wpam.kupmi.lib.PermissionsClassLib.LOCATION_ACCESS_PERMISSIONS_CODE;

public class RequestsSearchActivity extends AppCompatActivity {

    private static final String TAG = "REQUESTS_SEARCH_ACTIVITY";

    private FusedLocationProviderClient fusedLocationClient;
    private AddressResultReceiver resultReceiver;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location location;

    private FragmentManager fragmentManager;
    private RequestsSearchMap requestsSearchMap = new RequestsSearchMap();
    private RequestsSearchResultList requestsSearchResultList = new RequestsSearchResultList();

    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null)
                return;

            Log.i(TAG, resultData.getString(Constants.RESULT_DATA_KEY));
        }
    }

    // Override AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_search);

        resultReceiver = new AddressResultReceiver(new Handler());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fragmentManager = getSupportFragmentManager();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location = locationResult.getLastLocation();
                // we want only one result
                fusedLocationClient.removeLocationUpdates(locationCallback);
                goToMap();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_ACCESS_PERMISSIONS_CODE);
            return;
        }
        Log.i(TAG, "Waiting for location");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_ACCESS_PERMISSIONS_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.recreate();
                } else {
                    //TODO: quit
                }
                return;
            }
        }
    }

    // Public methods
    public void setLocation(double lat, double lon) {
        location.setLatitude(lat);
        location.setLongitude(lon);
        Log.i(TAG, lat + ", " + lon);

        startIntentService();
    }

    public void goToMap() {
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", location.getLatitude());
        bundle.putDouble("lon", location.getLongitude());

        requestsSearchMap.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.requests_search_main_layout, requestsSearchMap);
        transaction.commit();
    }

    public void goToResultList() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.requests_search_result_list, requestsSearchResultList);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Private / protected methods
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }
}
