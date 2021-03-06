package com.wpam.kupmi.activities.requestsSearch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.wpam.kupmi.R;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.utils.ActivityUtils;
import java.util.Objects;

import static com.wpam.kupmi.lib.Constants.FASTEST_INTERVAL;
import static com.wpam.kupmi.lib.Constants.UPDATE_INTERVAL;
import static com.wpam.kupmi.lib.PermissionsClassLib.LOCATION_ACCESS_PERMISSIONS_CODE;
import static com.wpam.kupmi.utils.DialogUtils.showOKDialog;

public class RequestsSearchActivity extends AppCompatActivity {

    // Private fields
    private static final String TAG = "REQUESTS_SEARCH_ACTIVITY";

    private FusedLocationProviderClient fusedLocationClient;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location location;

    private User user;

    private ProgressBar bar;

    private FragmentManager fragmentManager;
    private RequestsSearchMap requestsSearchMap = new RequestsSearchMap();

    private boolean mapNotCreated = true;
    private Context context;

    // Override AppCompatActivity
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_search);
        context = getApplicationContext();

        user = (User) Objects.requireNonNull(getIntent().getExtras()).getSerializable(Constants.USER);
        if (user == null)
        {
            Toast.makeText(this,
                    R.string.authorize_user_error, Toast.LENGTH_SHORT).show();
            ActivityUtils.returnToMainActivity(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fragmentManager = getSupportFragmentManager();

        bar = (ProgressBar) findViewById(R.id.request_search_progress_bar);
        setBarVisible(true);

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location = locationResult.getLastLocation();
                if(location == null){
                    Toast.makeText(context, "Localization doesn't work", Toast.LENGTH_SHORT).show();
                    setBarVisible(false);
                    finish();
                }
                // we want only one result
                fusedLocationClient.removeLocationUpdates(locationCallback);
                goToMap();
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(!locationAvailability.isLocationAvailable()) {
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    Toast.makeText(context, "Localization doesn't work", Toast.LENGTH_SHORT).show();
                    setBarVisible(false);
                    finish();
                }
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
        if(mapNotCreated) {
            Log.i(TAG, "Waiting for location");
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            mapNotCreated = false;
        }
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
                    Toast.makeText(this, "App couldn't work properly without this permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    // Public methods
    public void goToMap() {
        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.MAP_LAT, location.getLatitude());
        bundle.putDouble(Constants.MAP_LON, location.getLongitude());

        requestsSearchMap.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.requests_search_main_layout, requestsSearchMap);
        transaction.commit();
    }

    public void setBarVisible(boolean b) {
        if (bar == null)
            return;
        if (b) {
            bar.bringToFront();
            bar.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
        }
    }

    public User getUser()
    {
        return user;
    }

    // Private / protected methods
}
