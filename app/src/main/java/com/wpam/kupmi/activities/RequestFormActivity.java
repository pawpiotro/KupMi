package com.wpam.kupmi.activities;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wpam.kupmi.R;

import java.util.Calendar;

import static com.wpam.kupmi.lib.PermissionsClassLib.LOCATION_ACCESS_PERMISSIONS_CODE;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.services.FetchAddressIntentService;

public class RequestFormActivity extends AppCompatActivity {

    private static final String TAG = "REQUEST_FORM_ACTIVITY";
    private FusedLocationProviderClient fusedLocationClient;

    private static final int UPDATE_INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 1000;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationResult locationResult;

    private Location location;
    private AddressResultReceiver resultReceiver;

    private TextView coordsText;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }
            coordsText.setText(resultData.getString(Constants.RESULT_DATA_KEY));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_form);
        coordsText = (TextView) findViewById(R.id.coordinates);
        resultReceiver = new AddressResultReceiver(new Handler());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.i(TAG, "On create");
        //version with location updates
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                location = locationResult.getLastLocation();
                Double lat = locationResult.getLastLocation().getLatitude();
                Double lon = locationResult.getLastLocation().getLongitude();
                coordsText.setText(lat + ", " + lon);
                //once
                fusedLocationClient.removeLocationUpdates(locationCallback);
                startIntentService();

            }
        };
        final TextView timeEditText = (TextView)findViewById(R.id.time);
        timeEditText.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" +
                Calendar.getInstance().get(Calendar.MINUTE));
        //timeEditText.setInputType(InputType.TYPE_NULL);
        timeEditText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(RequestFormActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeEditText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                timePicker.setTitle("Select Time");
                timePicker.show();

            }
        });
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

        //version without location updates
        /*
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.i(TAG, "Got location");
                            TextView coords = (TextView) findViewById(R.id.coordinates);
                            coords.setText(location.toString());
                        }
                    }
                });
        */
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
}
