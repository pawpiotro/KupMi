package com.wpam.kupmi.activities.requestForm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.auth.AuthManager;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.services.FetchAddressIntentService;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.wpam.kupmi.lib.PermissionsClassLib.LOCATION_ACCESS_PERMISSIONS_CODE;

public class RequestFormActivity extends FragmentActivity {

    private static final String TAG = "REQUEST_FORM_ACTIVITY";
    private FusedLocationProviderClient fusedLocationClient;

    private static final int UPDATE_INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 1000;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationResult locationResult;

    private Location location;
    private Request request = new Request();

    private AddressResultReceiver resultReceiver;

    private ProgressBar bar;

    private FragmentManager fragmentManager;
    private RequestFormMap requestFormMap = new RequestFormMap();
    private RequestFormClock requestFormClock = new RequestFormClock();
    private RequestFormDesc requestFormDesc = new RequestFormDesc();
    private RequestFormSummary requestFormSummary = new RequestFormSummary();

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }
            Log.i(TAG,resultData.getString(Constants.RESULT_DATA_KEY));
            request.setLocationAddress(resultData.getString(Constants.RESULT_DATA_KEY));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_form);
        resultReceiver = new AddressResultReceiver(new Handler());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.i(TAG, "On create");

        fragmentManager = getSupportFragmentManager();

        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

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

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    public void insertIntoDB()
    {
        request.setRequesterUID(AuthManager.getInstance().getCurrentUserUid());
        request.setState(RequestState.NEW);

        Log.i(TAG, "INSERTING INTO DATABASE:");
        Log.i(TAG, request.toString(this));

        DatabaseManager.getInstance().addRequest(request);
    }

    public void setBarVisible(boolean b) {
        if (bar == null)
            return;
        if (b)
            bar.setVisibility(View.VISIBLE);
        else
            bar.setVisibility(View.GONE);
    }

    public void setLocation(double lat, double lon) {
        location.setLatitude(lat);
        location.setLongitude(lon);
        request.setLocation(Pair.create(lat, lon));
        Log.i(TAG, lat + ", " + lon);

        startIntentService();
    }

    public void setDeadline(Calendar deadline) {
        request.setDeadline(deadline);
    }

    public void setDescription(String description) {
        request.setDescription(description);
    }

    public void setTags(List<String> tags) {
        request.setTags(tags);
    }

    public Request getRequest() {
        return request;
    }

    public void goToMap() {
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", location.getLatitude());
        bundle.putDouble("lon", location.getLongitude());

        requestFormMap.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormMap);
        transaction.commit();
    }

    public void goToClock() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormClock);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void goToDesc() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormDesc);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void goToSummary() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormSummary);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
