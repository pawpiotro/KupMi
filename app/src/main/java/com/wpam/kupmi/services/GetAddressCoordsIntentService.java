package com.wpam.kupmi.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.wpam.kupmi.lib.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetAddressCoordsIntentService extends IntentService {


    private static final String TAG = "GET_ADDRESS_COORDS_INTENT_SERVICE";

    private ResultReceiver receiver;

    public GetAddressCoordsIntentService() {
        super("GetAddressCoordsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }

        receiver = intent.getParcelableExtra(Constants.GET_ADDRESS_RECEIVER);
        if (receiver == null) {
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        String strAddress = intent.getStringExtra(Constants.ADDRESS_DATA_EXTRA);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(
                    strAddress,
                    1);
        } catch (IOException ioException) {
            Log.e(TAG, "Service not available", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, "Invalid coordinates", illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            Log.e(TAG, "No address was found");
            deliverResultToReceiver(Constants.FAILURE_RESULT, null, null);
        } else {
            Address location = addresses.get(0);
            Log.i(TAG, "Location found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    location.getLatitude(),location.getLongitude());

        }
    }

    private void deliverResultToReceiver(int resultCode, Double lat, Double lon) {

        Bundle bundle = new Bundle();
        if(lat != null && lon != null) {
            bundle.putDouble(Constants.GET_ADDRESS_RESULT_DATA_KEY_LAT, lat);
            bundle.putDouble(Constants.GET_ADDRESS_RESULT_DATA_KEY_LON, lon);
        }
        receiver.send(resultCode, bundle);
    }
}
