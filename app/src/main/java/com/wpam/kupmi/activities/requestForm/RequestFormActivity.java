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
import android.util.Log;
import android.util.Pair;
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
import com.wpam.kupmi.activities.MainActivity;
import com.wpam.kupmi.firebase.auth.AuthManager;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.services.FetchAddressIntentService;
import java.util.Calendar;
import java.util.Objects;

import static com.wpam.kupmi.lib.Constants.FASTEST_INTERVAL;
import static com.wpam.kupmi.lib.Constants.UPDATE_INTERVAL;
import static com.wpam.kupmi.lib.PermissionsClassLib.LOCATION_ACCESS_PERMISSIONS_CODE;
import static com.wpam.kupmi.utils.DialogUtils.showOKDialog;

public class RequestFormActivity extends FragmentActivity {

    // Private fields
    private static final String TAG = "REQUEST_FORM_ACTIVITY";
    private FusedLocationProviderClient fusedLocationClient;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Location location;
    private Request request = new Request();

    private User user;

    private AddressResultReceiver resultReceiver;

    private ProgressBar bar;

    private FragmentManager fragmentManager;
    private RequestFormMap requestFormMap = new RequestFormMap();
    private RequestFormClock requestFormClock = new RequestFormClock();
    private RequestFormDesc requestFormDesc = new RequestFormDesc();
    private RequestFormSummary requestFormSummary = new RequestFormSummary();

    private boolean mapNotCreated = true;
    private Context context;

    // Override AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_form);
        context = getApplicationContext();

        user = (User) Objects.requireNonNull(getIntent().getExtras()).getSerializable(Constants.USER);
        if (user == null)
        {
            showOKDialog(this, R.string.error_title, R.string.authorize_user_error,
                    android.R.drawable.ic_dialog_alert);
            returnToMainActivity();
        }

        resultReceiver = new AddressResultReceiver(new Handler());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.i(TAG, "On create");

        fragmentManager = getSupportFragmentManager();

        bar = (ProgressBar) findViewById(R.id.request_form_progress_bar);
        setBarVisible(true);

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location = locationResult.getLastLocation();
                Log.i(TAG,"LOCATION RESULT!!!!");
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
    public void insertIntoDB() {
        request.setRequesterUID(user.getUserUID());
        request.setState(RequestState.ACTIVE);

        Log.i(TAG, "INSERTING INTO DATABASE:");
        Log.i(TAG, request.toString(this));

        DatabaseManager.getInstance().addRequest(request);
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

    public void setTitle(String title)
    {
        request.setTitle(title);
    }

    public void setDescription(String description) {
        request.setDescription(description);
    }

    public void setTag(RequestTag tag) {
        request.setTag(tag);
    }

    public Request getRequest() {
        return request;
    }

    public void goToMap() {
        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.MAP_LAT, location.getLatitude());
        bundle.putDouble(Constants.MAP_LON, location.getLongitude());

        requestFormMap.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormMap);
        transaction.commit();
        Log.i(TAG, Integer.toString(fragmentManager.getBackStackEntryCount()));
    }

    public void goToClock() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormClock);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.i(TAG, Integer.toString(fragmentManager.getBackStackEntryCount()));
    }

    public void goToDesc() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormDesc);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.i(TAG, Integer.toString(fragmentManager.getBackStackEntryCount()));
    }

    public void goToSummary() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.request_form_main_layout, requestFormSummary);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.i(TAG, Integer.toString(fragmentManager.getBackStackEntryCount()));
    }

    // Private methods
    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private void returnToMainActivity()
    {
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    // Private classes
    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            if (resultCode == Constants.FAILURE_RESULT) {
                Log.w(TAG, "Address not found");
                Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show();
                return;
            }


            Log.i(TAG, resultData.getString(Constants.RESULT_DATA_KEY));
            request.setLocationAddress(resultData.getString(Constants.RESULT_DATA_KEY));
        }
    }
}
